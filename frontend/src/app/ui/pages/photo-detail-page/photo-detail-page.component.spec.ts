import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router, convertToParamMap, provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { ImageService } from '../../../core/images/services/image.service';
import { ToastService } from '../../../core/notifications/toast.service';
import { RoomService } from '../../../core/rooms/services/room.service';
import { PhotoDetailPageComponent } from './photo-detail-page.component';

describe('PhotoDetailPageComponent', () => {
  let fixture: ComponentFixture<PhotoDetailPageComponent>;
  const image = {
    id: 'image-1',
    roomId: 'room-id',
    url: '/room/room-id/images/image-1',
    contentType: 'image/jpeg',
    size: 12,
    title: 'Tarde en el mirador',
    comment: 'Una tarde que queremos recordar.',
  };
  const nextImage = {
    ...image,
    id: 'image-2',
    url: '/room/room-id/images/image-2',
    title: 'Atardecer',
    comment: 'El sol poniéndose.',
  };
  const getContent = vi.fn(() => of(new Blob(['photo'], { type: 'image/jpeg' })));
  const findByRoomId = vi.fn(() => of([image, nextImage]));

  beforeEach(async () => {
    vi.spyOn(URL, 'createObjectURL').mockReturnValue('blob:photo');
    vi.spyOn(URL, 'revokeObjectURL').mockImplementation(() => undefined);
    await TestBed.configureTestingModule({
      imports: [PhotoDetailPageComponent],
      providers: [
        provideRouter([]),
        {
          provide: ActivatedRoute,
          useValue: { paramMap: of(convertToParamMap({ imageId: 'image-1' })) },
        },
        { provide: RoomService, useValue: { getMyRooms: () => of([{ id: 'room-id', name: 'Nuestra habitación' }]) } },
        {
          provide: ImageService,
          useValue: {
            findByRoomId,
            getContent,
          },
        },
        { provide: ToastService, useValue: { showError: vi.fn() } },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(PhotoDetailPageComponent);
    await fixture.whenStable();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('shows the photo title, description, room and position', () => {
    expect(fixture.componentInstance).toBeTruthy();
    expect(fixture.nativeElement.textContent).toContain('Tarde en el mirador');
    expect(fixture.nativeElement.textContent).toContain('Una tarde que queremos recordar.');
    expect(fixture.nativeElement.textContent).toContain('Nuestra habitación');
    expect(fixture.nativeElement.textContent).toContain('1 de 2');
  });

  it('loads the next image and updates the route when the next arrow is clicked', async () => {
    const router = TestBed.inject(Router);
    const navigate = vi.spyOn(router, 'navigate').mockResolvedValue(true);
    const button: HTMLButtonElement = fixture.nativeElement.querySelector(
      'button[aria-label="Foto siguiente"]',
    );

    button.click();
    fixture.detectChanges();
    await fixture.whenStable();

    expect(getContent).toHaveBeenLastCalledWith('room-id', 'image-2');
    expect(fixture.nativeElement.textContent).toContain('Atardecer');
    expect(fixture.nativeElement.textContent).toContain('2 de 2');
    expect(navigate).toHaveBeenCalledWith(['/photos', 'image-2']);

    const previousButton: HTMLButtonElement = fixture.nativeElement.querySelector(
      'button[aria-label="Foto anterior"]',
    );
    previousButton.click();
    fixture.detectChanges();

    expect(getContent).toHaveBeenLastCalledWith('room-id', 'image-1');
    expect(fixture.nativeElement.textContent).toContain('Tarde en el mirador');
    expect(fixture.nativeElement.textContent).toContain('1 de 2');
  });

});
