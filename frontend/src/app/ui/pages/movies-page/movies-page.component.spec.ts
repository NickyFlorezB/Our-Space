import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { MovieService } from '../../../core/movies/services/movie.service';
import { ToastService } from '../../../core/notifications/toast.service';
import { RoomService } from '../../../core/rooms/services/room.service';
import { MoviesPageComponent } from './movies-page.component';

describe('MoviesPageComponent', () => {
  let fixture: ComponentFixture<MoviesPageComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MoviesPageComponent],
      providers: [
        provideRouter([]),
        {
          provide: MovieService,
          useValue: {
            findByRoomId: () =>
              of([
                {
                  id: 'movie-1',
                  roomId: 'room-1',
                  name: 'Una película',
                  comment: 'Para el fin de semana',
                  url: 'https://example.com/movie',
                  imageUrl: null,
                },
              ]),
          },
        },
        {
          provide: RoomService,
          useValue: { getMyRooms: () => of([{ id: 'room-1', name: 'Nuestro espacio' }]) },
        },
        { provide: ToastService, useValue: { showError: () => undefined } },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(MoviesPageComponent);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();
  });

  it('shows the room movie and its safe external link', () => {
    const link = fixture.nativeElement.querySelector('.movie-card__link') as HTMLAnchorElement;
    expect(fixture.nativeElement.textContent).toContain('Una película');
    expect(link.href).toBe('https://example.com/movie');
    expect(link.rel).toContain('noopener');
  });

  it('rejects unsafe external link protocols', () => {
    expect(fixture.componentInstance.safeLink('javascript:alert(1)')).toBeNull();
  });
});
