import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { Book, CreateBookRequest } from '../models/book.model';

@Injectable({ providedIn: 'root' })
export class BookService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiUrl;

  findByRoomId(roomId: string): Observable<Book[]> {
    return this.http.get<Book[]>(`${this.baseUrl}/room/${roomId}/books`);
  }

  create(roomId: string, book: CreateBookRequest, cover?: File): Observable<Book> {
    if (!cover) {
      return this.http.post<Book>(`${this.baseUrl}/room/${roomId}/books`, book);
    }

    const formData = new FormData();
    formData.append('book', new Blob([JSON.stringify(book)], { type: 'application/json' }));
    formData.append('file', cover);
    return this.http.post<Book>(`${this.baseUrl}/room/${roomId}/books`, formData);
  }

  getCover(roomId: string, bookId: string): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/room/${roomId}/books/${bookId}/cover`, {
      responseType: 'blob',
    });
  }

  delete(roomId: string, bookId: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/room/${roomId}/books/${bookId}`);
  }
}
