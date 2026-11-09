import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { ToastService } from '../../../core/notifications/toast.service';
import { RoomService } from '../../../core/rooms/services/room.service';
import { SongService } from '../../../core/songs/services/song.service';
import { SongsPageComponent } from './songs-page.component';

describe('SongsPageComponent', () => {
  let fixture: ComponentFixture<SongsPageComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SongsPageComponent],
      providers: [
        provideRouter([]),
        {
          provide: SongService,
          useValue: {
            findByRoomId: () =>
              of([
                {
                  id: 'song-1',
                  roomId: 'room-1',
                  name: 'Nuestra canción',
                  comment: 'Para recordar',
                  url: 'https://open.spotify.com/track/example',
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

    fixture = TestBed.createComponent(SongsPageComponent);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();
  });

  it('shows the song and links out to the music service safely', () => {
    const link = fixture.nativeElement.querySelector('.song-card__play') as HTMLAnchorElement;
    expect(fixture.nativeElement.textContent).toContain('Nuestra canción');
    expect(link.href).toContain('https://open.spotify.com/track/example');
    expect(link.rel).toContain('noopener');
  });

  it('rejects unsafe external link protocols', () => {
    expect(fixture.componentInstance.safeLink('javascript:alert(1)')).toBeNull();
  });
});
