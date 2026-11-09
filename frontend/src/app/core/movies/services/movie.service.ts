import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { CreateMovieRequest, Movie } from '../models/movie.model';

@Injectable({ providedIn: 'root' })
export class MovieService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiUrl;

  findByRoomId(roomId: string): Observable<Movie[]> {
    return this.http.get<Movie[]>(`${this.baseUrl}/room/${roomId}/movies`);
  }

  create(roomId: string, movie: CreateMovieRequest, cover?: File): Observable<Movie> {
    if (!cover) {
      return this.http.post<Movie>(`${this.baseUrl}/room/${roomId}/movies`, movie);
    }

    const formData = new FormData();
    formData.append('movie', new Blob([JSON.stringify(movie)], { type: 'application/json' }));
    formData.append('file', cover);
    return this.http.post<Movie>(`${this.baseUrl}/room/${roomId}/movies`, formData);
  }

  getCover(roomId: string, movieId: string): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/room/${roomId}/movies/${movieId}/cover`, {
      responseType: 'blob',
    });
  }

  delete(roomId: string, movieId: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/room/${roomId}/movies/${movieId}`);
  }
}
