import { Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Subscription, filter, forkJoin, map, switchMap, tap, throwError } from 'rxjs';
import { Image } from '../../../core/images/models/image.model';
import { ImageService } from '../../../core/images/services/image.service';
import { ToastService } from '../../../core/notifications/toast.service';
import { RoomService } from '../../../core/rooms/services/room.service';

@Component({
  imports: [RouterLink],
  selector: 'app-photo-detail-page',
  styleUrl: './photo-detail-page.component.scss',
  templateUrl: './photo-detail-page.component.html',
})
export class PhotoDetailPageComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly imageService = inject(ImageService);
  private readonly roomService = inject(RoomService);
  private readonly toastService = inject(ToastService);
  private readonly destroyRef = inject(DestroyRef);
  private imageObjectUrl?: string;
  private currentImageId?: string;
  private currentRoomId?: string;
  private contentSubscription?: Subscription;

  readonly imageUrl = signal<string | null>(null);
  readonly title = signal<string | null>(null);
  readonly comment = signal<string | null>(null);
  readonly roomName = signal<string | null>(null);
  readonly imagePosition = signal(0);
  readonly imageCount = signal(0);
  readonly isLoading = signal(true);
  private roomImages: Image[] = [];

  constructor() {
    this.destroyRef.onDestroy(() => this.releaseImageUrl());
  }

  ngOnInit(): void {
    this.route.paramMap
      .pipe(
        tap(() => {
          this.isLoading.set(true);
          this.releaseImageUrl();
          this.imageUrl.set(null);
        }),
        map((params) => params.get('imageId')),
        filter((imageId) => imageId !== this.currentImageId),
        switchMap((imageId) => {
          if (!imageId) {
            return throwError(() => new Error('No se encontró la foto solicitada.'));
          }

          return this.roomService.getMyRooms().pipe(
            switchMap((rooms) => {
              if (rooms.length === 0) {
                return throwError(() => new Error('No tienes acceso a esta foto.'));
              }

              return forkJoin(
                rooms.map((room) =>
                  this.imageService.findByRoomId(room.id).pipe(map((images) => ({ room, images }))),
                ),
              ).pipe(
                map((roomImages) => roomImages.find(({ images }) =>
                  images.some((image) => image.id === imageId),
                )),
                switchMap((result) => {
                  if (!result) {
                    return throwError(() => new Error('No se encontró la foto solicitada.'));
                  }

                  const position = result.images.findIndex((image) => image.id === imageId);
                  const image = result.images[position];
                  return this.imageService
                    .getContent(result.room.id, image.id)
                    .pipe(
                      map((content) => ({
                        image,
                        content,
                        position,
                        images: result.images,
                        roomName: result.room.name,
                      })),
                    );
                }),
              );
            }),
          );
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: ({ image, content, position, images, roomName }) => {
          this.releaseImageUrl();
          this.imageObjectUrl = URL.createObjectURL(content);
          this.imageUrl.set(this.imageObjectUrl);
          this.title.set(image.title);
          this.comment.set(image.comment);
          this.roomName.set(roomName);
          this.imagePosition.set(position + 1);
          this.imageCount.set(images.length);
          this.roomImages = images;
          this.currentImageId = image.id;
          this.currentRoomId = image.roomId;
          this.isLoading.set(false);
        },
        error: (error: unknown) => {
          this.isLoading.set(false);
          this.toastService.showError(
            error instanceof Error ? error.message : 'No fue posible cargar la foto.',
          );
        },
      });
  }

  showPrevious(): void {
    this.navigateToImage(this.imagePosition() - 2);
  }

  showNext(): void {
    this.navigateToImage(this.imagePosition());
  }

  private navigateToImage(index: number): void {
    const image = this.roomImages[index];
    const roomId = this.currentRoomId;
    if (!image || !roomId || image.id === this.currentImageId) {
      return;
    }

    this.contentSubscription?.unsubscribe();
    this.isLoading.set(true);
    this.contentSubscription = this.imageService
      .getContent(roomId, image.id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (content) => {
          this.releaseImageUrl();
          this.imageObjectUrl = URL.createObjectURL(content);
          this.imageUrl.set(this.imageObjectUrl);
          this.title.set(image.title);
          this.comment.set(image.comment);
          this.imagePosition.set(index + 1);
          this.currentImageId = image.id;
          this.isLoading.set(false);
          void this.router.navigate(['/photos', image.id]);
        },
        error: () => {
          this.isLoading.set(false);
          this.toastService.showError('No fue posible cargar la foto siguiente.');
        },
      });
  }

  private releaseImageUrl(): void {
    if (this.imageObjectUrl) {
      URL.revokeObjectURL(this.imageObjectUrl);
      this.imageObjectUrl = undefined;
    }
  }
}

export default PhotoDetailPageComponent;
