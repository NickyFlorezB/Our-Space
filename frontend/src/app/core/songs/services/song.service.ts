import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { CreateSongRequest, Song } from '../models/song.model';

@Injectable({ providedIn: 'root' })
export class SongService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiUrl;

  findByRoomId(roomId: string): Observable<Song[]> {
    return this.http.get<Song[]>(`${this.baseUrl}/room/${roomId}/songs`);
  }

  create(roomId: string, song: CreateSongRequest): Observable<Song> {
    return this.http.post<Song>(`${this.baseUrl}/room/${roomId}/songs`, song);
  }

  delete(roomId: string, songId: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/room/${roomId}/songs/${songId}`);
  }
}
