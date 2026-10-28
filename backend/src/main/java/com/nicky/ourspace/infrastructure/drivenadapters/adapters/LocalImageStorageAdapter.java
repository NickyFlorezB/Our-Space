package com.nicky.ourspace.infrastructure.drivenadapters.adapters;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.springframework.stereotype.Service;

import com.nicky.ourspace.domain.model.ImageUpload;
import com.nicky.ourspace.domain.model.StoredImage;
import com.nicky.ourspace.domain.port.ImageStoragePort;
import com.nicky.ourspace.infrastructure.drivenadapters.storage.ImageStorageProperties;

import net.coobird.thumbnailator.Thumbnails;

@Service
public class LocalImageStorageAdapter implements ImageStoragePort {

    private static final long MAX_FILE_SIZE = 5L * 1024 * 1024;
    private static final long MAX_PIXELS = 20_000_000L;
    private static final int MAX_DIMENSION = 1200;

    private final Path storageRoot;

    public LocalImageStorageAdapter(ImageStorageProperties properties) {
        this.storageRoot = Objects.requireNonNull(properties.root(), "app.storage.root must be configured")
                .toAbsolutePath()
                .normalize();
        try {
            Files.createDirectories(storageRoot);
        } catch (IOException exception) {
            throw new UncheckedIOException(
                    "Cannot initialize image storage at " + storageRoot
                            + ". Ensure the application user has write permission or set IMAGE_STORAGE_PATH.",
                    exception);
        }
    }

    @Override
    public StoredImage saveRoomImage(UUID roomId, UUID imageId, ImageUpload upload) {
        validateUpload(upload);

        Path directory = roomDirectory(roomId);
        Path destination = imagePath(roomId, imageId);
        Path temporaryFile = null;

        try {
            Files.createDirectories(directory);
            BufferedImage original = readAndValidate(upload.content());
            if (original.getColorModel().hasAlpha()) {
                throw new IllegalArgumentException("Transparent images are not supported");
            }

            temporaryFile = Files.createTempFile(directory, "upload-", ".tmp");
            try (OutputStream output = Files.newOutputStream(
                    temporaryFile,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING)) {
                Thumbnails.of(original)
                        .size(MAX_DIMENSION, MAX_DIMENSION)
                        .outputFormat("jpg")
                        .outputQuality(0.85)
                        .toOutputStream(output);
            }

            long compressedSize = Files.size(temporaryFile);
            if (compressedSize == 0 || ImageIO.read(temporaryFile.toFile()) == null) {
                throw new IOException("Thumbnailator produced an empty or invalid JPEG image");
            }

            moveIntoPlace(temporaryFile, destination);
            temporaryFile = null;
            if (Files.size(destination) != compressedSize) {
                Files.deleteIfExists(destination);
                throw new IOException("Stored image size does not match the compressed image");
            }

            return new StoredImage("image/jpeg", compressedSize);
        } catch (IOException exception) {
            cleanupTemporaryFile(temporaryFile, exception);
            throw new UncheckedIOException("Could not store image", exception);
        } catch (RuntimeException exception) {
            cleanupTemporaryFile(temporaryFile, exception);
            throw exception;
        }
    }

    @Override
    public InputStream readRoomImage(UUID roomId, UUID imageId) {
        try {
            return Files.newInputStream(imagePath(roomId, imageId));
        } catch (IOException exception) {
            throw new UncheckedIOException("Could not read image", exception);
        }
    }

    @Override
    public void deleteRoomImage(UUID roomId, UUID imageId) {
        try {
            Files.deleteIfExists(imagePath(roomId, imageId));
        } catch (IOException exception) {
            throw new UncheckedIOException("Could not delete image", exception);
        }
    }

    private void validateUpload(ImageUpload upload) {
        if (upload == null || upload.content() == null
                || upload.size() <= 0 || upload.size() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Image must be between 1 byte and 5 MB");
        }
    }

    private BufferedImage readAndValidate(InputStream content) throws IOException {
        try (ImageInputStream imageInput = ImageIO.createImageInputStream(content)) {
            if (imageInput == null) {
                throw new IllegalArgumentException("Invalid image");
            }

            Iterator<ImageReader> readers = ImageIO.getImageReaders(imageInput);
            if (!readers.hasNext()) {
                throw new IllegalArgumentException("Unsupported image");
            }

            ImageReader reader = readers.next();
            try {
                reader.setInput(imageInput);
                String format = reader.getFormatName().toLowerCase(Locale.ROOT);
                if (!format.equals("jpeg") && !format.equals("png")) {
                    throw new IllegalArgumentException("Only JPEG and PNG images are allowed");
                }

                long width = reader.getWidth(0);
                long height = reader.getHeight(0);
                if (width <= 0 || height <= 0 || width * height > MAX_PIXELS) {
                    throw new IllegalArgumentException("Image dimensions are not allowed");
                }

                BufferedImage image = reader.read(0);
                if (image == null) {
                    throw new IllegalArgumentException("Invalid image content");
                }
                return image;
            } finally {
                reader.dispose();
            }
        }
    }

    private void moveIntoPlace(Path temporaryFile, Path destination) throws IOException {
        try {
            Files.move(temporaryFile, destination, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException exception) {
            Files.move(temporaryFile, destination);
        }
    }

    private Path roomDirectory(UUID roomId) {
        return storageRoot.resolve(roomId.toString()).normalize();
    }

    private Path imagePath(UUID roomId, UUID imageId) {
        Path roomDirectory = roomDirectory(roomId);
        Path imagePath = roomDirectory.resolve(imageId + ".jpg").normalize();
        if (!imagePath.startsWith(storageRoot)) {
            throw new IllegalArgumentException("Invalid image path");
        }
        return imagePath;
    }

    private void cleanupTemporaryFile(Path temporaryFile, Exception primaryException) {
        if (temporaryFile == null) {
            return;
        }
        try {
            Files.deleteIfExists(temporaryFile);
        } catch (IOException cleanupException) {
            primaryException.addSuppressed(cleanupException);
        }
    }
}
