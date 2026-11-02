export interface Image {
  id: string;
  roomId: string;
  url: string;
  contentType: string;
  size: number;
  title: string | null;
  comment: string | null;
}

export interface Photo extends Image {
  src: string;
}
