import { Component, ViewChild, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';

import { AuthService } from '../../core/services/auth.service';
import { TransferService } from '../../core/services/transfer.service';
import { ErrorResponse, TransferSuccessResponse } from '../../core/models/transfer.model';
import { ToastComponent } from '../../toast/toast.component'; // adjust path if needed

@Component({
  selector: 'app-transfer',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, ToastComponent],
  templateUrl: './transfer.component.html',
  styleUrl: './transfer.component.scss',
})
export class TransferComponent {

  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly transferService = inject(TransferService);
  private readonly router = inject(Router);

  @ViewChild('toast') toast!: ToastComponent;

  fromAccountId = this.auth.getAccountId();

  form = this.fb.nonNullable.group({
    toAccountId: ['', [Validators.required]],
    amount: ['', [Validators.required, Validators.min(0.01)]],
  });

  loading = false;
  lastTransactionId: string | null = null;

  get fromAccountDisplay(): string {
    return this.fromAccountId != null ? `#${this.fromAccountId}` : 'N/A';
  }

  submit(): void {

    if (!this.fromAccountId) {
      this.router.navigate(['/login']);
      return;
    }

    if (this.form.invalid || this.loading) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading = true;
    this.lastTransactionId = null;

    const { toAccountId, amount } = this.form.getRawValue();

    const payload = {
      fromAccountId: this.fromAccountId,
      toAccountId: Number(toAccountId),
      amount: Number(amount),
      idempotencyKey: this.generateIdempotencyKey(),
    };

    this.transferService.transfer(payload).subscribe({

      next: (res: TransferSuccessResponse | ErrorResponse) => {
        this.loading = false;

        if ((res as any).transactionId) {
          const ok = res as TransferSuccessResponse;

          this.lastTransactionId = ok.transactionId;

          this.toast.show(
            ok.message || `Transfer successful (ID: ${ok.transactionId})`,
            'success'
          );

          this.form.reset();
        } else {
          const err = res as ErrorResponse;

          this.toast.show(
            `${err.errorCode} - ${err.message}`,
            'error'
          );
        }
      },

      error: (err) => {
        this.loading = false;

        const apiError = err?.error as ErrorResponse | undefined;

        if (apiError?.errorCode) {
          this.toast.show(
            `${apiError.errorCode} - ${apiError.message}`,
            'error'
          );
        } else {
          this.toast.show(
            'Transfer failed. Please try again.',
            'error'
          );
        }
      },
    });
  }

  cancel(): void {
    this.router.navigate(['/dashboard']);
  }

  private generateIdempotencyKey(): string {
    return `trx-${Date.now()}-${Math.random().toString(16).slice(2)}`;
  }
}