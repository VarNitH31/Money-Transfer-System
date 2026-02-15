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
  paginatedTransactions = signal<TransactionLog[]>([]);  // Current page slice
  loading = signal<boolean>(true);
  error = signal<string | null>(null);
  transactionFilter = signal<'all' | 'debits' | 'credits'>('all');  // Default filter is 'all'

  pageSize = signal<5 | 10>(5);  // Rows per page: 5 or 10
  currentPage = signal(1);

  readonly pageSizeOptions: { value: 5 | 10; label: string }[] = [
    { value: 5, label: '5 rows' },
    { value: 10, label: '10 rows' },
  ];

  get totalPages(): number {
    const total = this.filteredTransactions().length;
    const size = this.pageSize();
    return total ? Math.ceil(total / size) : 1;
  }

  get startIndex(): number {
    return (this.currentPage() - 1) * this.pageSize() + 1;
  }

  get endIndex(): number {
    const total = this.filteredTransactions().length;
    const end = this.currentPage() * this.pageSize();
    return Math.min(end, total);
  }

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

  // Apply filter based on the selected filter type; sort by most recent first
  applyFilter(): void {
    const filter = this.transactionFilter();
    let list: TransactionLog[];
    if (filter === 'all') {
      list = this.transactions();
    } else if (filter === 'debits') {
      list = this.transactions().filter(tx => this.asDebit(tx));
    } else {
      list = this.transactions().filter(tx => !this.asDebit(tx));
    }
    // Sort by most recent transaction first (createdOn descending)
    list = [...list].sort((a, b) =>
      new Date(b.createdOn).getTime() - new Date(a.createdOn).getTime()
    );
    this.filteredTransactions.set(list);
    this.currentPage.set(1);  // Reset to first page when filter changes
    this.updatePaginatedSlice();
  }

  private updatePaginatedSlice(): void {
    const filtered = this.filteredTransactions();
    const page = this.currentPage();
    const size = this.pageSize();
    const start = (page - 1) * size;
    const slice = filtered.slice(start, start + size);
    this.paginatedTransactions.set(slice);
  }

  onPageSizeChange(value: 5 | 10): void {
    this.pageSize.set(value);
    this.currentPage.set(1);
    this.updatePaginatedSlice();
  }

  goToPage(page: number): void {
    const total = this.totalPages;
    if (page >= 1 && page <= total) {
      this.currentPage.set(page);
      this.updatePaginatedSlice();
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
