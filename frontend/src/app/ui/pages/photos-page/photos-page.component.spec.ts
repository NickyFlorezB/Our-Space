import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { ImageService } from '../../../core/images/services/image.service';
import { RoomService } from '../../../core/rooms/services/room.service';
import { PhotosPageComponent } from './photos-page.component';

describe('PhotosPageComponent', () => {
  let component: PhotosPageComponent;
  let fixture: ComponentFixture<PhotosPageComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PhotosPageComponent],
      providers: [
        provideRouter([]),
        { provide: ImageService, useValue: { findByRoomId: () => of([]) } },
        { provide: RoomService, useValue: { getMyRooms: () => of([]) } },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(PhotosPageComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
