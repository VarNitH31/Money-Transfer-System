import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule, DatePipe, NgClass } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { AccountService } from '../../core/services/account.service';
import { TransactionLog } from '../../core/models/transaction.model';
import { Location } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-history',
  standalone: true,
  imports: [CommonModule, DatePipe, NgClass, FormsModule],
  templateUrl: './history.component.html',
  styleUrls: ['./history.component.scss'],
})
export class HistoryComponent implements OnInit {
  private readonly auth = inject(AuthService);
  private readonly accountService = inject(AccountService);
  private readonly router = inject(Router);
  private location = inject(Location);

  transactions = signal<TransactionLog[]>([]);  // All transactions
  filteredTransactions = signal<TransactionLog[]>([]);  // Filtered transactions based on selected filter
  loading = signal<boolean>(true);
  error = signal<string | null>(null);
  transactionFilter = signal<'all' | 'debits' | 'credits'>('all');  // Default filter is 'all'

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
        this.applyFilter();  // Apply filter to transactions after they are loaded
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(err?.error?.message || 'Unable to load transaction history.');
        this.loading.set(false);
      },
    });
  }

  // Apply filter based on the selected filter type
  applyFilter(): void {
    const filter = this.transactionFilter();
    if (filter === 'all') {
      this.filteredTransactions.set(this.transactions());
    } else if (filter === 'debits') {
      this.filteredTransactions.set(this.transactions().filter(tx => this.asDebit(tx)));
    } else if (filter === 'credits') {
      this.filteredTransactions.set(this.transactions().filter(tx => !this.asDebit(tx)));
    }
  }

  // Check if the transaction is a debit (based on fromAccountId)
  asDebit(row: TransactionLog): boolean {
    const accountId = this.auth.getAccountId();
    return row.fromAccountId === accountId;
  }

  // Handle filter change
  onFilterChange(filter: 'all' | 'debits' | 'credits'): void {
    this.transactionFilter.set(filter);
    this.applyFilter();  // Reapply filter when selection changes
  }

  goBack() {
    this.location.back();
  }
}
