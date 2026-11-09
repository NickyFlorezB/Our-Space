export interface Movie {
  id: string;
  roomId: string;
  name: string;
  comment: string;
  url: string;
  imageUrl: string | null;
  coverSrc?: string;
}

export interface CreateMovieRequest {
  name: string;
  comment: string;
  url: string;
}
