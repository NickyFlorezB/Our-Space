import { Component, DestroyRef, OnInit, computed, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute } from '@angular/router';
import { Subscription, catchError, forkJoin, map, of, switchMap } from 'rxjs';
import {
  LucideBookOpen,
  LucideExternalLink,
  LucidePlus,
  LucideSearch,
  LucideTrash2,
  LucideX,
} from '@lucide/angular';
import { Book } from '../../../core/books/models/book.model';
import { BookService } from '../../../core/books/services/book.service';
import { ToastService } from '../../../core/notifications/toast.service';
import { Room } from '../../../core/rooms/models/room.model';
import { RoomService } from '../../../core/rooms/services/room.service';

@Component({
  imports: [
    ReactiveFormsModule,
    LucideBookOpen,
    LucideExternalLink,
    LucidePlus,
    LucideSearch,
    LucideTrash2,
    LucideX,
  ],
  selector: 'app-books-page',
  styleUrl: './books-page.component.scss',
  templateUrl: './books-page.component.html',
})
export class BooksPageComponent implements OnInit {
  private readonly bookService = inject(BookService);
  private readonly roomService = inject(RoomService);
  private readonly route = inject(ActivatedRoute);
  private readonly toastService = inject(ToastService);
  private readonly formBuilder = inject(FormBuilder);
  private readonly destroyRef = inject(DestroyRef);
  private readonly coverUrls = new Set<string>();
  private booksSubscription?: Subscription;

  readonly rooms = signal<Room[]>([]);
  readonly selectedRoomId = signal<string | null>(null);
  readonly books = signal<Book[]>([]);
  readonly searchQuery = signal('');
  readonly isLoading = signal(true);
  readonly isSaving = signal(false);
  readonly isDialogOpen = signal(false);
  readonly pendingDeleteId = signal<string | null>(null);
  readonly selectedCover = signal<File | null>(null);
  readonly visibleBooks = computed(() => {
    const query = this.searchQuery().trim().toLocaleLowerCase();
    if (!query) {
      return this.books();
    }

    return this.books().filter((book) =>
      [book.title, book.fullTitle, book.isbn_13, ...(book.publishers ?? [])]
        .join(' ')
        .toLocaleLowerCase()
        .includes(query),
    );
  });
  readonly bookForm = this.formBuilder.nonNullable.group({
    title: ['', [Validators.required, Validators.maxLength(255), Validators.pattern(/\S/)]],
    isbn: ['', [Validators.required, Validators.pattern(/^\d{13}$/)]],
    weight: ['0', [Validators.required, Validators.pattern(/^\d+(\.\d+)?$/)]],
    fullTitle: [''],
    revision: [null as number | null, Validators.min(1)],
    published: [''],
    publishers: [''],
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
    this.bookForm.reset({
      title: '',
      isbn: '',
      weight: '0',
      fullTitle: '',
      revision: null,
      published: '',
      publishers: '',
    });
  }

  saveBook(): void {
    const roomId = this.selectedRoomId();
    if (!roomId || this.bookForm.invalid || this.isSaving()) {
      this.bookForm.markAllAsTouched();
      return;
    }

    const value = this.bookForm.getRawValue();
    this.isSaving.set(true);
    this.bookService
      .create(
        roomId,
        {
          isbn_13: value.isbn.trim(),
          title: value.title.trim(),
          weight: value.weight.trim(),
          fullTitle: value.fullTitle.trim() || null,
          latestRevision: value.revision,
          createdAt: value.published || null,
          publishers: value.publishers
            .split(',')
            .map((publisher) => publisher.trim())
            .filter(Boolean),
        },
        this.selectedCover() ?? undefined,
      )
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.isSaving.set(false);
          this.closeDialog();
          this.loadBooks(roomId);
        },
        error: (error: unknown) => {
          this.toastService.showError(this.errorMessage(error, 'No fue posible guardar el libro.'));
          this.isSaving.set(false);
        },
      });
  }

  confirmDelete(book: Book): void {
    if (this.pendingDeleteId() !== book.id) {
      this.pendingDeleteId.set(book.id);
      return;
    }

    const roomId = this.selectedRoomId();
    if (!roomId) {
      return;
    }
    this.bookService
      .delete(roomId, book.id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.pendingDeleteId.set(null);
          this.loadBooks(roomId);
        },
        error: (error: unknown) => {
          this.toastService.showError(this.errorMessage(error, 'No fue posible quitar el libro.'));
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
    this.loadBooks(roomId);
  }

  private loadBooks(roomId: string): void {
    this.booksSubscription?.unsubscribe();
    this.isLoading.set(true);
    this.releaseCoverUrls();
    let coverFailureReported = false;
    this.booksSubscription = this.bookService
      .findByRoomId(roomId)
      .pipe(
        switchMap((books) =>
          books.length === 0
            ? of([])
            : forkJoin(
                books.map((book) =>
                  book.imageUrl
                    ? this.bookService.getCover(roomId, book.id).pipe(
                        map((blob) => {
                          const coverSrc = URL.createObjectURL(blob);
                          this.coverUrls.add(coverSrc);
                          return { ...book, coverSrc };
                        }),
                        catchError((error: unknown) => {
                          if (!coverFailureReported) {
                            this.toastService.showError(
                              this.errorMessage(error, 'No fue posible cargar una portada guardada.'),
                            );
                            coverFailureReported = true;
                          }
                          return of(book);
                        }),
                      )
                    : of(book),
                ),
              ),
        ),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (books) => {
          this.books.set(books);
          this.isLoading.set(false);
        },
        error: (error: unknown) => {
          this.books.set([]);
          this.toastService.showError(
            this.errorMessage(error, 'No fue posible cargar los libros.'),
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

export default BooksPageComponent;
