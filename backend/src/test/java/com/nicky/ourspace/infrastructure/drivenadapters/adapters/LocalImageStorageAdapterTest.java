package com.nicky.ourspace.infrastructure.drivenadapters.adapters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.nicky.ourspace.domain.model.ImageUpload;
import com.nicky.ourspace.infrastructure.drivenadapters.storage.ImageStorageProperties;

class LocalImageStorageAdapterTest {

    @TempDir
    Path storageRoot;

    @Test
    void storesCompressedMovieAndBookCoversInTheirOwnDirectories() throws Exception {
        byte[] sourceImage = createPngImage(1600, 900);
        LocalImageStorageAdapter storage = new LocalImageStorageAdapter(
                new ImageStorageProperties(storageRoot));
        UUID movieId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();

        var movieImage = storage.saveMovieCover(
                movieId, new ImageUpload(new ByteArrayInputStream(sourceImage), sourceImage.length));
        var bookImage = storage.saveBookCover(
                bookId, new ImageUpload(new ByteArrayInputStream(sourceImage), sourceImage.length));

        Path moviePath = storageRoot.resolve("movies").resolve(movieId + ".jpg");
        Path bookPath = storageRoot.resolve("books").resolve(bookId + ".jpg");
        assertTrue(Files.exists(moviePath));
        assertTrue(Files.exists(bookPath));
        assertEquals("image/jpeg", movieImage.contentType());
        assertEquals(Files.size(moviePath), movieImage.size());
        assertEquals("image/jpeg", bookImage.contentType());
        assertEquals(Files.size(bookPath), bookImage.size());
        byte[] storedMovieBytes = Files.readAllBytes(moviePath);
        assertEquals(0xFF, storedMovieBytes[0] & 0xFF);
        assertEquals(0xD8, storedMovieBytes[1] & 0xFF);
        BufferedImage storedMovie;
        BufferedImage storedBook;
        try (InputStream movieContent = storage.readMovieCover(movieId);
                InputStream bookContent = storage.readBookCover(bookId)) {
            storedMovie = ImageIO.read(movieContent);
            storedBook = ImageIO.read(bookContent);
        }
        assertNotNull(storedMovie);
        assertNotNull(storedBook);
        assertTrue(storedMovie.getWidth() <= 1200);
        assertTrue(storedMovie.getHeight() <= 1200);
    }

    private byte[] createPngImage(int width, int height) throws Exception {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, "png", output);
        return output.toByteArray();
    }
}
