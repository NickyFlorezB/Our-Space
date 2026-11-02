import { Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { catchError, forkJoin, map, of, Subscription, switchMap, throwError } from 'rxjs';
import { Image, Photo } from '../../../core/images/models/image.model';
import { ImageService } from '../../../core/images/services/image.service';
import { ToastService } from '../../../core/notifications/toast.service';
import { Room } from '../../../core/rooms/models/room.model';
import { RoomService } from '../../../core/rooms/services/room.service';

@Component({
  imports: [ReactiveFormsModule, RouterLink],
  selector: 'app-photos-page.component',
  styleUrl: './photos-page.component.scss',
  templateUrl: './photos-page.component.html',
})
export class PhotosPageComponent implements OnInit {
  private readonly imageService = inject(ImageService);
  private readonly roomService = inject(RoomService);
  private readonly toastService = inject(ToastService);
  private readonly formBuilder = inject(FormBuilder);
  private readonly destroyRef = inject(DestroyRef);
  private readonly imageObjectUrls = new Set<string>();
  private photosSubscription?: Subscription;

  readonly rooms = signal<Room[]>([]);
  readonly selectedRoomId = signal<string | null>(null);
  readonly photos = signal<Photo[]>([]);
  readonly isLoading = signal(true);
  readonly isUploading = signal(false);
  readonly isUploadDialogOpen = signal(false);
  readonly selectedFile = signal<File | null>(null);
  readonly photoForm = this.formBuilder.nonNullable.group({
    title: ['', [Validators.required, Validators.maxLength(160), Validators.pattern(/\S/)]],
    comment: ['', Validators.maxLength(1000)],
  });

  constructor() {
    this.destroyRef.onDestroy(() => this.releaseImageUrls());
  }

  ngOnInit(): void {
    this.roomService
      .getMyRooms()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (rooms) => {
          this.rooms.set(rooms);
          const firstRoom = rooms[0];
          if (firstRoom) {
            this.selectedRoomId.set(firstRoom.id);
            this.loadPhotos(firstRoom.id);
            return;
          }

          this.isLoading.set(false);
        },
        error: (error: unknown) => {
          this.toastService.showError(
            this.getErrorMessage(error, 'No fue posible cargar tus habitaciones.'),
          );
          this.isLoading.set(false);
        },
      });
  }

  onRoomChange(event: Event): void {
    const roomId = (event.target as HTMLSelectElement).value;
    this.selectedRoomId.set(roomId);
    this.loadPhotos(roomId);
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0] ?? null;
    if (!file) {
      this.selectedFile.set(null);
      return;
    }

    if (!['image/jpeg', 'image/png'].includes(file.type)) {
      this.toastService.showError('El archivo debe ser una imagen JPEG o PNG.');
      this.selectedFile.set(null);
      input.value = '';
      return;
    }

    if (file.size > 5 * 1024 * 1024) {
      this.toastService.showError('La imagen no puede superar los 5 MB.');
      this.selectedFile.set(null);
      input.value = '';
      return;
    }

    this.selectedFile.set(file);
  }

  openUploadDialog(): void {
    this.isUploadDialogOpen.set(true);
  }

  closeUploadDialog(): void {
    if (this.isUploading()) {
      return;
    }

    this.isUploadDialogOpen.set(false);
    this.selectedFile.set(null);
    this.photoForm.reset();
  }

  uploadPhoto(): void {
    const roomId = this.selectedRoomId();
    const file = this.selectedFile();
    if (!roomId || !file || this.photoForm.invalid || this.isUploading()) {
      return;
    }

    this.isUploading.set(true);
    this.imageService
      .upload(
        roomId,
        file,
        this.photoForm.controls.title.value,
        this.photoForm.controls.comment.value,
      )
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.isUploading.set(false);
          this.closeUploadDialog();
          if (this.selectedRoomId() === roomId) {
            this.loadPhotos(roomId);
          }
        },
        error: (error: unknown) => {
          this.toastService.showError(
            this.getErrorMessage(error, 'No fue posible subir la foto. Inténtalo de nuevo.'),
          );
          this.isUploading.set(false);
        },
      });
  }

  private loadPhotos(roomId: string): void {
    this.photosSubscription?.unsubscribe();
    this.isLoading.set(true);
    this.photos.set([]);
    this.releaseImageUrls();

    this.photosSubscription = this.imageService
      .findByRoomId(roomId)
      .pipe(
        switchMap((images) => {
          if (images.length === 0) {
            return of([] as Photo[]);
          }

          const photoRequests = images.map((image) =>
            this.imageService.getContent(roomId, image.id).pipe(
              map((content) => this.toPhoto(image, content)),
              catchError((error: unknown) => {
                if (error instanceof HttpErrorResponse && error.status === 404) {
                  return of(null);
                }
                return throwError(() => error);
              }),
            ),
          );
          return forkJoin(photoRequests).pipe(
            map((results) => results.filter((photo): photo is Photo => photo !== null)),
          );
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (photos) => {
          this.photos.set(photos);
          this.isLoading.set(false);
        },
        error: (error: unknown) => {
          this.photos.set([]);
          this.releaseImageUrls();
          this.toastService.showError(
            this.getErrorMessage(error, 'No fue posible cargar las fotos. Inténtalo de nuevo.'),
          );
          this.isLoading.set(false);
        },
      });
  }

  private getErrorMessage(error: unknown, fallback: string): string {
    if (
      error instanceof HttpErrorResponse &&
      typeof error.error === 'string' &&
      error.error.trim()
    ) {
      return error.error;
    }

    return fallback;
  }

  private toPhoto(image: Image, content: Blob): Photo {
    const src = URL.createObjectURL(content);
    this.imageObjectUrls.add(src);
    return { ...image, src };
  }

  private releaseImageUrls(): void {
    this.imageObjectUrls.forEach((url) => URL.revokeObjectURL(url));
    this.imageObjectUrls.clear();
  }
}

export default PhotosPageComponent;
