import { DecimalPipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { ProductApiService } from '../../core/api/product-api.service';
import { Product } from '../product.model';

/**
 * Danh sách sản phẩm. PATTERN MẪU cho các màn hình danh sách khác.
 */
@Component({
  selector: 'app-product-list',
  standalone: true,
  imports: [RouterLink, DecimalPipe],
  templateUrl: './product-list.component.html',
  styleUrl: './product-list.component.css',
})
export class ProductListComponent implements OnInit {
  private readonly api = inject(ProductApiService);

  readonly products = signal<Product[]>([]);
  readonly keyword = signal('');
  readonly error = signal<string | null>(null);

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.error.set(null);
    this.api.search(this.keyword()).subscribe({
      next: (page) => this.products.set(page.content),
      error: () => this.error.set('Không tải được danh sách sản phẩm'),
    });
  }

  onSearch(value: string): void {
    this.keyword.set(value.trim());
    this.load();
  }

  remove(product: Product): void {
    if (!confirm(`Xóa sản phẩm ${product.code}?`)) {
      return;
    }
    this.api.delete(product.id).subscribe({
      next: () => this.load(),
      error: () => this.error.set('Xóa sản phẩm thất bại'),
    });
  }
}
