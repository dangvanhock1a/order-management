export interface Product {
  id: number;
  code: string;
  name: string;
  price: number;
  stock: number;
  createdAt: string;
}

export interface ProductRequest {
  code: string;
  name: string;
  price: number;
  stock: number;
}

export interface Page<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

/** Định dạng lỗi backend trả về (ErrorResponse). */
export interface ApiError {
  code: string;
  message: string;
  fieldErrors?: Record<string, string>;
}
