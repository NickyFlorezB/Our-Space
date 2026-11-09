import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { BookService } from '../../../core/books/services/book.service';
import { ToastService } from '../../../core/notifications/toast.service';
import { RoomService } from '../../../core/rooms/services/room.service';
import { BooksPageComponent } from './books-page.component';

describe('BooksPageComponent', () => {
  let fixture: ComponentFixture<BooksPageComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BooksPageComponent],
      providers: [
        provideRouter([]),
        {
          provide: BookService,
          useValue: {
            findByRoomId: () =>
              of([
                {
                  id: 'book-1',
                  roomId: 'room-1',
                  isbn_13: '9781234567890',
                  title: 'El libro compartido',
                  weight: '0',
                  fullTitle: null,
                  latestRevision: null,
                  createdAt: null,
                  publishers: ['Editorial'],
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

    fixture = TestBed.createComponent(BooksPageComponent);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();
  });

  it('shows the books in the selected room', () => {
    expect(fixture.nativeElement.textContent).toContain('El libro compartido');
    expect(fixture.nativeElement.textContent).toContain('9781234567890');
  });
});
