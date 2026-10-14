import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { Page, Product, ProductRequest } from '../../product/product.model';

/**
 * Gọi API sản phẩm. Mọi lời gọi API đặt trong core/api, component không gọi HttpClient trực tiếp.
 */
@Injectable({ providedIn: 'root' })
export class ProductApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/products';

  search(keyword = '', page = 0, size = 20): Observable<Page<Product>> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (keyword) {
      params = params.set('keyword', keyword);
    }
    return this.http.get<Page<Product>>(this.baseUrl, { params });
  }

  get(id: number): Observable<Product> {
    return this.http.get<Product>(`${this.baseUrl}/${id}`);
  }

  create(request: ProductRequest): Observable<Product> {
    return this.http.post<Product>(this.baseUrl, request);
  }

  update(id: number, request: ProductRequest): Observable<Product> {
    return this.http.put<Product>(`${this.baseUrl}/${id}`, request);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
