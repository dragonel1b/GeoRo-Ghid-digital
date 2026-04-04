export interface Event {
  id: string;
  title: string;
  date: string;
  location: string;
  imageUrl: string;
  description?: string;
  time?: string;
}

export interface Category {
  id: string;
  title: string;
  description: string;
  imageUrl: string;
  icon?: string;
}
