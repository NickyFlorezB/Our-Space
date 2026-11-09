export interface Book {
  id: string;
  roomId: string;
  isbn_13: string;
  title: string;
  weight: string;
  fullTitle: string | null;
  latestRevision: number | null;
  createdAt: string | null;
  publishers: string[] | null;
  imageUrl: string | null;
  coverSrc?: string;
}

export interface CreateBookRequest {
  isbn_13: string;
  title: string;
  weight: string;
  fullTitle: string | null;
  latestRevision: number | null;
  createdAt: string | null;
  publishers: string[];
}
