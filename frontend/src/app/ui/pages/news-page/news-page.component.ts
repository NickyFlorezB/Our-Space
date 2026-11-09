import { Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { HttpErrorResponse } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import {
  LucideBookOpen,
  LucideEighthNotes,
  LucideImage,
  LucideClapperboard,
} from '@lucide/angular';
import { forkJoin, Subscription } from 'rxjs';
import { BookService } from '../../../core/books/services/book.service';
import { ImageService } from '../../../core/images/services/image.service';
import { MovieService } from '../../../core/movies/services/movie.service';
import { PushNotificationService } from '../../../core/notifications/services/push-notification.service';
import { ToastService } from '../../../core/notifications/toast.service';
import { Room } from '../../../core/rooms/models/room.model';
import { RoomService } from '../../../core/rooms/services/room.service';
import { SongService } from '../../../core/songs/services/song.service';

@Component({
  imports: [RouterLink, LucideBookOpen, LucideEighthNotes, LucideImage, LucideClapperboard],
  selector: 'app-news-page.component',
  styleUrl: './news-page.component.scss',
  templateUrl: './news-page.component.html',
})
export class NewsPageComponent implements OnInit {
  private readonly roomService = inject(RoomService);
  private readonly imageService = inject(ImageService);
  private readonly bookService = inject(BookService);
  private readonly movieService = inject(MovieService);
  private readonly songService = inject(SongService);
  private readonly pushService = inject(PushNotificationService);
  private readonly toastService = inject(ToastService);
  private readonly destroyRef = inject(DestroyRef);
  private statsSubscription?: Subscription;

  readonly rooms = signal<Room[]>([]);
  readonly selectedRoomId = signal<string | null>(null);
  readonly selectedRoomName = signal('');
  readonly stats = signal<{ photos: number; books: number; movies: number; songs: number; members: number } | null>(
    null,
  );
  readonly isLoadingRooms = signal(true);
  readonly isLoadingStats = signal(false);
  readonly statsError = signal(false);
  readonly isPushSupported = signal(false);
  readonly isPushConfigured = signal(false);
  readonly notificationsEnabled = signal(false);
  readonly isPushLoading = signal(false);
  readonly greeting = this.getGreeting();

  constructor() {
    this.destroyRef.onDestroy(() => this.statsSubscription?.unsubscribe());
  }

  ngOnInit(): void {
    this.isPushSupported.set(this.pushService.isSupported());
    this.roomService
      .getMyRooms()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (rooms) => {
          this.rooms.set(rooms);
          this.isLoadingRooms.set(false);
          const firstRoom = rooms[0];
          if (firstRoom) {
            this.selectRoom(firstRoom.id);
          }
        },
        error: (error: unknown) => {
          this.isLoadingRooms.set(false);
          this.toastService.showError(this.getErrorMessage(error, 'No fue posible cargar tus habitaciones.'));
        },
      });

    if (this.isPushSupported()) {
      this.pushService
        .getSettings()
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: (settings) => {
            this.isPushConfigured.set(settings.enabled);
            this.pushService
              .hasSubscription()
              .pipe(takeUntilDestroyed(this.destroyRef))
              .subscribe((enabled) => this.notificationsEnabled.set(enabled));
          },
          error: (error: unknown) => {
            this.toastService.showError(
              this.getErrorMessage(error, 'No fue posible consultar la configuración de notificaciones.'),
            );
          },
        });
    }
  }

  onRoomChange(event: Event): void {
    const roomId = (event.target as HTMLSelectElement).value;
    this.selectRoom(roomId);
  }

  toggleNotifications(): void {
    if (this.isPushLoading() || !this.isPushConfigured()) {
      return;
    }

    this.isPushLoading.set(true);
    const request = this.notificationsEnabled()
      ? this.pushService.disable()
      : this.pushService.enable();
    request.pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.notificationsEnabled.update((enabled) => !enabled);
        this.isPushLoading.set(false);
      },
      error: (error: unknown) => {
        this.toastService.showError(
          this.getErrorMessage(error, 'No fue posible actualizar las notificaciones.'),
        );
        this.isPushLoading.set(false);
      },
    });
  }

  reloadStats(): void {
    const roomId = this.selectedRoomId();
    if (roomId) {
      this.loadStats(roomId);
    }
  }

  private selectRoom(roomId: string): void {
    const room = this.rooms().find((candidate) => candidate.id === roomId);
    if (!room) {
      return;
    }
    this.selectedRoomId.set(roomId);
    this.selectedRoomName.set(room.name);
    this.loadStats(roomId);
  }

  private loadStats(roomId: string): void {
    this.statsSubscription?.unsubscribe();
    this.stats.set(null);
    this.statsError.set(false);
    this.isLoadingStats.set(true);
    this.statsSubscription = forkJoin({
      photos: this.imageService.findByRoomId(roomId),
      books: this.bookService.findByRoomId(roomId),
      movies: this.movieService.findByRoomId(roomId),
      songs: this.songService.findByRoomId(roomId),
      members: this.roomService.getMembers(roomId),
    })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: ({ photos, books, movies, songs, members }) => {
          this.stats.set({
            photos: photos.length,
            books: books.length,
            movies: movies.length,
            songs: songs.length,
            members: members.length,
          });
          this.isLoadingStats.set(false);
        },
        error: (error: unknown) => {
          this.statsError.set(true);
          this.isLoadingStats.set(false);
          this.toastService.showError(
            this.getErrorMessage(error, 'No fue posible cargar las estadísticas de esta habitación.'),
          );
        },
      });
  }

  private getGreeting(): string {
    const hour = new Date().getHours();
    if (hour < 12) {
      return 'Buenos días';
    }
    return hour < 19 ? 'Buenas tardes' : 'Buenas noches';
  }

  private getErrorMessage(error: unknown, fallback: string): string {
    if (error instanceof HttpErrorResponse && typeof error.error === 'string' && error.error.trim()) {
      return error.error;
    }
    if (error instanceof Error && error.message.trim()) {
      return error.message;
    }
    return fallback;
  }
}

export default NewsPageComponent;
