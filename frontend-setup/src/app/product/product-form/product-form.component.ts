import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { ProductApiService } from '../../core/api/product-api.service';
import { ApiError } from '../product.model';

type FieldName = 'code' | 'name' | 'price' | 'stock';

/**
 * Form tạo/sửa sản phẩm. PATTERN MẪU cho các form khác:
 * Reactive Forms, validate giống backend, hiển thị lỗi dưới từng trường và lỗi nghiệp vụ từ API.
 */
@Component({
  selector: 'app-product-form',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './product-form.component.html',
  styleUrl: './product-form.component.css',
})
export class ProductFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly api = inject(ProductApiService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly id = signal<number | null>(null);
  readonly serverError = signal<string | null>(null);
  readonly saving = signal(false);

  readonly form = this.fb.nonNullable.group({
    code: ['', [Validators.required, Validators.maxLength(30), Validators.pattern(/^[A-Z0-9-]+$/)]],
    name: ['', [Validators.required, Validators.maxLength(200)]],
    price: [0, [Validators.required, Validators.min(0)]],
    stock: [0, [Validators.required, Validators.min(0)]],
  });

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      const id = Number(idParam);
      this.id.set(id);
      this.form.controls.code.disable(); // không được đổi mã sản phẩm
      this.api.get(id).subscribe({
        next: (product) => this.form.patchValue(product),
        error: () => this.serverError.set('Không tải được sản phẩm'),
      });
    }
  }

  hasError(field: FieldName): boolean {
    const control = this.form.controls[field];
    return control.invalid && (control.dirty || control.touched);
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.saving.set(true);
    this.serverError.set(null);
    const request = this.form.getRawValue();
    const id = this.id();
    const call = id ? this.api.update(id, request) : this.api.create(request);
    call.subscribe({
      next: () => this.router.navigate(['/products']),
      error: (err: HttpErrorResponse) => {
        const body = err.error as ApiError | null;
        this.serverError.set(body?.message ?? 'Lưu sản phẩm thất bại');
        this.saving.set(false);
      },
    });
  }
}
