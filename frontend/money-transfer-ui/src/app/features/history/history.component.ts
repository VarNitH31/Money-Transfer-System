import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule, DatePipe, NgClass } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { AccountService } from '../../core/services/account.service';
import { TransactionLog } from '../../core/models/transaction.model';
import { Location } from '@angular/common';

@Component({
  selector: 'app-history',
  standalone: true,
  imports: [CommonModule, DatePipe, NgClass],
  templateUrl: './history.component.html',
  styleUrl: './history.component.scss',
})
export class HistoryComponent implements OnInit {
  private readonly auth = inject(AuthService);
  private readonly accountService = inject(AccountService);
  private readonly router = inject(Router);
  private location = inject(Location);

  transactions = signal<TransactionLog[]>([]);
  loading = signal<boolean>(true);
  error = signal<string | null>(null);

  ngOnInit(): void {
    const accountId = this.auth.getAccountId();
    if (!accountId) {
      this.router.navigate(['/login']);
      return;
    }

    this.loading.set(true);
    this.accountService.getTransactions(accountId).subscribe({
      next: (tx) => {
        this.transactions.set(tx);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(err?.error?.message || 'Unable to load transaction history.');
        this.loading.set(false);
      },
    });
  }

  asDebit(row: TransactionLog): boolean {
    const accountId = this.auth.getAccountId();
    return row.fromAccountId === accountId;
  }

  goBack() {
  this.location.back();
}
}

