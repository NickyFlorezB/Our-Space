import { Component, DestroyRef, OnInit, computed, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute } from '@angular/router';
import { Subscription, catchError, forkJoin, map, of, switchMap } from 'rxjs';
import {
  LucideClapperboard,
  LucideExternalLink,
  LucidePlay,
  LucidePlus,
  LucideSearch,
  LucideTrash2,
  LucideX,
} from '@lucide/angular';
import { Movie } from '../../../core/movies/models/movie.model';
import { MovieService } from '../../../core/movies/services/movie.service';
import { ToastService } from '../../../core/notifications/toast.service';
import { Room } from '../../../core/rooms/models/room.model';
import { RoomService } from '../../../core/rooms/services/room.service';

@Component({
  imports: [
    ReactiveFormsModule,
    LucideClapperboard,
    LucideExternalLink,
    LucidePlay,
    LucidePlus,
    LucideSearch,
    LucideTrash2,
    LucideX,
  ],
  selector: 'app-movies-page',
  styleUrl: './movies-page.component.scss',
  templateUrl: './movies-page.component.html',
})
export class MoviesPageComponent implements OnInit {
  private readonly movieService = inject(MovieService);
  private readonly roomService = inject(RoomService);
  private readonly route = inject(ActivatedRoute);
  private readonly toastService = inject(ToastService);
  private readonly formBuilder = inject(FormBuilder);
  private readonly destroyRef = inject(DestroyRef);
  private readonly coverUrls = new Set<string>();
  private moviesSubscription?: Subscription;

  readonly rooms = signal<Room[]>([]);
  readonly selectedRoomId = signal<string | null>(null);
  readonly movies = signal<Movie[]>([]);
  readonly searchQuery = signal('');
  readonly isLoading = signal(true);
  readonly isSaving = signal(false);
  readonly isDialogOpen = signal(false);
  readonly pendingDeleteId = signal<string | null>(null);
  readonly selectedCover = signal<File | null>(null);
  readonly visibleMovies = computed(() => {
    const query = this.searchQuery().trim().toLocaleLowerCase();
    return query
      ? this.movies().filter((movie) =>
          `${movie.name} ${movie.comment}`.toLocaleLowerCase().includes(query),
        )
      : this.movies();
  });
  readonly movieForm = this.formBuilder.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(255), Validators.pattern(/\S/)]],
    comment: ['', [Validators.required, Validators.maxLength(255), Validators.pattern(/\S/)]],
    url: ['', [Validators.required, Validators.maxLength(255)]],
  });

  constructor() {
    this.destroyRef.onDestroy(() => this.releaseCoverUrls());
  }

  ngOnInit(): void {
    this.roomService
      .getMyRooms()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (rooms) => {
          this.rooms.set(rooms);
          const requestedRoomId = this.route.snapshot.queryParamMap.get('roomId');
          const selectedRoom = rooms.find((room) => room.id === requestedRoomId) ?? rooms[0];
          if (selectedRoom) {
            this.selectRoom(selectedRoom.id);
          } else {
            this.isLoading.set(false);
          }
        },
        error: (error: unknown) => {
          this.toastService.showError(this.errorMessage(error, 'No fue posible cargar tus salas.'));
          this.isLoading.set(false);
        },
      });
  }

  onRoomChange(event: Event): void {
    this.selectRoom((event.target as HTMLSelectElement).value);
  }

  onSearch(event: Event): void {
    this.searchQuery.set((event.target as HTMLInputElement).value);
  }

  onCoverSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0] ?? null;
    if (!file) {
      this.selectedCover.set(null);
      return;
    }

    if (!['image/jpeg', 'image/png'].includes(file.type) || file.size > 5 * 1024 * 1024) {
      this.toastService.showError('Elige una imagen JPEG o PNG de máximo 5 MB.');
      this.selectedCover.set(null);
      input.value = '';
      return;
    }
    this.selectedCover.set(file);
  }

  openDialog(): void {
    this.isDialogOpen.set(true);
  }

  closeDialog(): void {
    if (this.isSaving()) {
      return;
    }
    this.isDialogOpen.set(false);
    this.selectedCover.set(null);
    this.movieForm.reset({ name: '', comment: '', url: '' });
  }

  safeLink(value: string): string | null {
    try {
      const url = new URL(value);
      return url.protocol === 'https:' || url.protocol === 'http:' ? url.toString() : null;
    } catch {
      return null;
    }
  }

  saveMovie(): void {
    const roomId = this.selectedRoomId();
    if (!roomId || this.movieForm.invalid || this.isSaving()) {
      this.movieForm.markAllAsTouched();
      return;
    }
    const value = this.movieForm.getRawValue();
    const url = this.safeLink(value.url.trim());
    if (!url) {
      this.toastService.showError('Agrega un enlace válido que comience con http:// o https://.');
      return;
    }

    this.isSaving.set(true);
    this.movieService
      .create(
        roomId,
        { name: value.name.trim(), comment: value.comment.trim(), url },
        this.selectedCover() ?? undefined,
      )
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.isSaving.set(false);
          this.closeDialog();
          this.loadMovies(roomId);
        },
        error: (error: unknown) => {
          this.toastService.showError(
            this.errorMessage(error, 'No fue posible guardar la película.'),
          );
          this.isSaving.set(false);
        },
      });
  }

  confirmDelete(movie: Movie): void {
    if (this.pendingDeleteId() !== movie.id) {
      this.pendingDeleteId.set(movie.id);
      return;
    }
    const roomId = this.selectedRoomId();
    if (!roomId) {
      return;
    }
    this.movieService
      .delete(roomId, movie.id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.pendingDeleteId.set(null);
          this.loadMovies(roomId);
        },
        error: (error: unknown) => {
          this.toastService.showError(
            this.errorMessage(error, 'No fue posible quitar la película.'),
          );
          this.pendingDeleteId.set(null);
        },
      });
  }

  cancelDelete(): void {
    this.pendingDeleteId.set(null);
  }

  private selectRoom(roomId: string): void {
    this.selectedRoomId.set(roomId);
    this.pendingDeleteId.set(null);
    this.loadMovies(roomId);
  }

  private loadMovies(roomId: string): void {
    this.moviesSubscription?.unsubscribe();
    this.isLoading.set(true);
    this.releaseCoverUrls();
    let coverFailureReported = false;
    this.moviesSubscription = this.movieService
      .findByRoomId(roomId)
      .pipe(
        switchMap((movies) =>
          movies.length === 0
            ? of([])
            : forkJoin(
                movies.map((movie) =>
                  movie.imageUrl
                    ? this.movieService.getCover(roomId, movie.id).pipe(
                        map((blob) => {
                          const coverSrc = URL.createObjectURL(blob);
                          this.coverUrls.add(coverSrc);
                          return { ...movie, coverSrc };
                        }),
                        catchError((error: unknown) => {
                          if (!coverFailureReported) {
                            this.toastService.showError(
                              this.errorMessage(error, 'No fue posible cargar una portada guardada.'),
                            );
                            coverFailureReported = true;
                          }
                          return of(movie);
                        }),
                      )
                    : of(movie),
                ),
              ),
        ),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (movies) => {
          this.movies.set(movies);
          this.isLoading.set(false);
        },
        error: (error: unknown) => {
          this.movies.set([]);
          this.toastService.showError(
            this.errorMessage(error, 'No fue posible cargar las películas.'),
          );
          this.isLoading.set(false);
        },
      });
  }

  private errorMessage(error: unknown, fallback: string): string {
    return error instanceof HttpErrorResponse &&
      typeof error.error === 'string' &&
      error.error.trim()
      ? error.error
      : fallback;
  }

  private releaseCoverUrls(): void {
    this.coverUrls.forEach((url) => URL.revokeObjectURL(url));
    this.coverUrls.clear();
  }
}

export default MoviesPageComponent;
