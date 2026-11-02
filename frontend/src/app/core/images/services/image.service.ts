import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, catchError, from, switchMap, throwError } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { Image } from '../models/image.model';

@Injectable({
  providedIn: 'root',
})
export class ImageService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiUrl;

  findByRoomId(roomId: string): Observable<Image[]> {
    return this.http.get<Image[]>(`${this.baseUrl}/room/${roomId}/images`);
  }

  getContent(roomId: string, imageId: string): Observable<Blob> {
    return this.http
      .get(`${this.baseUrl}/room/${roomId}/images/${imageId}/content`, {
        responseType: 'blob',
      })
      .pipe(
        catchError((error: unknown) => {
          if (!(error instanceof HttpErrorResponse) || !(error.error instanceof Blob)) {
            return throwError(() => error);
          }

          return from(error.error.text()).pipe(
            switchMap((message) =>
              throwError(
                () =>
                  new HttpErrorResponse({
                    error: message,
                    status: error.status,
                    statusText: error.statusText,
                    url: error.url ?? undefined,
                  }),
              ),
            ),
          );
        }),
      );
  }

  upload(roomId: string, file: File, title: string, comment: string): Observable<Image> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('title', title.trim());
    formData.append('comment', comment.trim());

    return this.http.post<Image>(`${this.baseUrl}/room/${roomId}/images`, formData);
  }
}
