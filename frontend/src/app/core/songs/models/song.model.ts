export interface Song {
  id: string;
  roomId: string;
  name: string;
  comment: string;
  url: string;
}

export interface CreateSongRequest {
  name: string;
  comment: string;
  url: string;
}
