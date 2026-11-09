import { Component, DestroyRef, OnInit, computed, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs';
import {
  LucideExternalLink,
  LucideMusic2,
  LucidePlus,
  LucideSearch,
  LucideTrash2,
  LucideX,
} from '@lucide/angular';
import { Song } from '../../../core/songs/models/song.model';
import { SongService } from '../../../core/songs/services/song.service';
import { ToastService } from '../../../core/notifications/toast.service';
import { Room } from '../../../core/rooms/models/room.model';
import { RoomService } from '../../../core/rooms/services/room.service';

@Component({
  imports: [
    ReactiveFormsModule,
    LucideExternalLink,
    LucideMusic2,
    LucidePlus,
    LucideSearch,
    LucideTrash2,
    LucideX,
  ],
  selector: 'app-songs-page',
  styleUrl: './songs-page.component.scss',
  templateUrl: './songs-page.component.html',
})
export class SongsPageComponent implements OnInit {
  private readonly songService = inject(SongService);
  private readonly roomService = inject(RoomService);
  private readonly route = inject(ActivatedRoute);
  private readonly toastService = inject(ToastService);
  private readonly formBuilder = inject(FormBuilder);
  private readonly destroyRef = inject(DestroyRef);
  private songsSubscription?: Subscription;

  readonly rooms = signal<Room[]>([]);
  readonly selectedRoomId = signal<string | null>(null);
  readonly songs = signal<Song[]>([]);
  readonly searchQuery = signal('');
  readonly isLoading = signal(true);
  readonly isSaving = signal(false);
  readonly isDialogOpen = signal(false);
  readonly pendingDeleteId = signal<string | null>(null);
  readonly visibleSongs = computed(() => {
    const query = this.searchQuery().trim().toLocaleLowerCase();
    return query
      ? this.songs().filter((song) =>
          `${song.name} ${song.comment}`.toLocaleLowerCase().includes(query),
        )
      : this.songs();
  });
  readonly songForm = this.formBuilder.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(255), Validators.pattern(/\S/)]],
    comment: ['', [Validators.maxLength(255)]],
    url: ['', [Validators.required, Validators.maxLength(255)]],
  });

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

  openDialog(): void {
    this.isDialogOpen.set(true);
  }

  closeDialog(): void {
    if (this.isSaving()) {
      return;
    }
    this.isDialogOpen.set(false);
    this.songForm.reset({ name: '', comment: '', url: '' });
  }

  safeLink(value: string): string | null {
    try {
      const url = new URL(value);
      return url.protocol === 'https:' || url.protocol === 'http:' ? url.toString() : null;
    } catch {
      return null;
    }
  }

  saveSong(): void {
    const roomId = this.selectedRoomId();
    if (!roomId || this.songForm.invalid || this.isSaving()) {
      this.songForm.markAllAsTouched();
      return;
    }
    const value = this.songForm.getRawValue();
    const url = this.safeLink(value.url.trim());
    if (!url) {
      this.toastService.showError('Agrega un enlace válido que comience con http:// o https://.');
      return;
    }

    this.isSaving.set(true);
    this.songService
      .create(roomId, {
        name: value.name.trim(),
        comment: value.comment.trim(),
        url,
      })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.isSaving.set(false);
          this.closeDialog();
          this.loadSongs(roomId);
        },
        error: (error: unknown) => {
          this.toastService.showError(
            this.errorMessage(error, 'No fue posible guardar la canción.'),
          );
          this.isSaving.set(false);
        },
      });
  }

  confirmDelete(song: Song): void {
    if (this.pendingDeleteId() !== song.id) {
      this.pendingDeleteId.set(song.id);
      return;
    }
    const roomId = this.selectedRoomId();
    if (!roomId) {
      return;
    }
    this.songService
      .delete(roomId, song.id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.pendingDeleteId.set(null);
          this.loadSongs(roomId);
        },
        error: (error: unknown) => {
          this.toastService.showError(
            this.errorMessage(error, 'No fue posible quitar la canción.'),
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
    this.loadSongs(roomId);
  }

  private loadSongs(roomId: string): void {
    this.songsSubscription?.unsubscribe();
    this.isLoading.set(true);
    this.songsSubscription = this.songService
      .findByRoomId(roomId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (songs) => {
          this.songs.set(songs);
          this.isLoading.set(false);
        },
        error: (error: unknown) => {
          this.songs.set([]);
          this.toastService.showError(
            this.errorMessage(error, 'No fue posible cargar las canciones.'),
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
}

export default SongsPageComponent;
