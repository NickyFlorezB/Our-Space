import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { Room } from '../models/room.model';
import { RoomMember } from '../models/room-member.model';

@Injectable({
  providedIn: 'root',
})
export class RoomService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiUrl;

  getMyRooms(): Observable<Room[]> {
    return this.http.get<Room[]>(`${this.baseUrl}/room/mine`);
  }

  getMembers(roomId: string): Observable<RoomMember[]> {
    return this.http.get<RoomMember[]>(`${this.baseUrl}/room/${roomId}/members`);
  }
}
