import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { BookService } from '../../../core/books/services/book.service';
import { ImageService } from '../../../core/images/services/image.service';
import { MovieService } from '../../../core/movies/services/movie.service';
import { PushNotificationService } from '../../../core/notifications/services/push-notification.service';
import { ToastService } from '../../../core/notifications/toast.service';
import { RoomService } from '../../../core/rooms/services/room.service';
import { SongService } from '../../../core/songs/services/song.service';
import NewsPageComponent from './news-page.component';

describe('NewsPageComponent', () => {
  let component: NewsPageComponent;
  let fixture: ComponentFixture<NewsPageComponent>;
  const room = { id: 'room-1', name: 'Casa' };
  const roomService = {
    getMyRooms: vi.fn(() => of([room])),
    getMembers: vi.fn(() => of([{ roomId: room.id, userId: 'user-1', role: 'OWNER' }])),
  };
  const imageService = { findByRoomId: vi.fn(() => of([{}, {}])) };
  const bookService = { findByRoomId: vi.fn(() => of([{}])) };
  const movieService = { findByRoomId: vi.fn(() => of([])) };
  const songService = { findByRoomId: vi.fn(() => of([{}, {}, {}])) };
  const pushService = { isSupported: vi.fn(() => false) };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NewsPageComponent],
      providers: [
        provideRouter([]),
        { provide: RoomService, useValue: roomService },
        { provide: ImageService, useValue: imageService },
        { provide: BookService, useValue: bookService },
        { provide: MovieService, useValue: movieService },
        { provide: SongService, useValue: songService },
        { provide: PushNotificationService, useValue: pushService },
        { provide: ToastService, useValue: { showError: vi.fn() } },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(NewsPageComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('shows counts based on the selected room content', () => {
    expect(component.stats()).toEqual({
      photos: 2,
      books: 1,
      movies: 0,
      songs: 3,
      members: 1,
    });
    expect(fixture.nativeElement.textContent).toContain('Casa');
    expect(imageService.findByRoomId).toHaveBeenCalledWith('room-1');
    const booksLink = fixture.nativeElement.querySelector(
      '.dashboard__stat--books',
    ) as HTMLAnchorElement;
    expect(booksLink.href).toContain('roomId=room-1');
  });
});
