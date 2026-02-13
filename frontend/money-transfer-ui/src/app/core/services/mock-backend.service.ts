import { Injectable } from '@angular/core';
import {
  HttpErrorResponse,
  HttpRequest,
  HttpResponse,
} from '@angular/common/http';
import { Observable, of, throwError } from 'rxjs';
import { delay } from 'rxjs/operators';
import { LoginRequest, LoginResponse } from '../models/auth.model';
import { Account } from '../models/account.model';
import {
  ErrorResponse,
  TransferRequest,
  TransferSuccessResponse,
} from '../models/transfer.model';
import { TransactionLog } from '../models/transaction.model';

@Injectable({
  providedIn: 'root',
})
export class MockBackendService {
  // Simple toggle: set to false when you connect real backend.
  readonly enabled = false;

  private accounts: Account[] = [
    {
      id: 1,
      holderName: 'John Doe',
      balance: 5000.0,
      status: 'ACTIVE',
      lastUpdated: new Date().toISOString(),
    },
    {
      id: 2,
      holderName: 'Jane Smith',
      balance: 2500.5,
      status: 'ACTIVE',
      lastUpdated: new Date().toISOString(),
    },
    {
      id: 3,
      holderName: 'Closed Account',
      balance: 100.0,
      status: 'CLOSED',
      lastUpdated: new Date().toISOString(),
    },
  ];

  private transactions: TransactionLog[] = [];

  handle(req: HttpRequest<unknown>): Observable<HttpResponse<unknown>> | null {
    if (!this.enabled || !req.url.startsWith('/api')) {
      return null;
    }

    // Small artificial delay to feel like a real backend.
    const latencyMs = 300;

    // Auth
    if (req.method === 'POST' && req.url === '/api/auth/login') {
      return this.handleLogin(req).pipe(delay(latencyMs));
    }

    // Transfer
    if (req.method === 'POST' && req.url === '/api/v1/transfers') {
      return this.handleTransfer(req).pipe(delay(latencyMs));
    }

    // Accounts
    const accountMatch = req.url.match(/^\/api\/v1\/accounts\/(\d+)(.*)$/);
    if (accountMatch && req.method === 'GET') {
      const id = Number(accountMatch[1]);
      const suffix = accountMatch[2];

      if (suffix === '') {
        return this.handleGetAccount(id).pipe(delay(latencyMs));
      }
      if (suffix === '/balance') {
        return this.handleGetBalance(id).pipe(delay(latencyMs));
      }
      if (suffix === '/transactions') {
        return this.handleGetTransactions(id).pipe(delay(latencyMs));
      }
    }

    return null;
  }

  private handleLogin(
    req: HttpRequest<unknown>
  ): Observable<HttpResponse<LoginResponse>> {
    const body = req.body as LoginRequest;

    // Very simple mock rule: username 'user1' logs into account 1, anything else 401.
    if (body?.username === 'user1' && body?.password) {
      const account = this.accounts.find((a) => a.id === 1)!;
      const response: LoginResponse = {
        token: 'mock-jwt-token',
        accountId: account.id,
        holderName: account.holderName,
      };
      return of(new HttpResponse({ status: 200, body: response }));
    }

    const error: ErrorResponse = {
      errorCode: 'AUTH-401',
      message: 'Invalid username or password',
    };
    return throwError(
      () =>
        new HttpErrorResponse({
          status: 401,
          error,
        })
    );
  }

  private handleGetAccount(
    id: number
  ): Observable<HttpResponse<Account | ErrorResponse>> {
    const account = this.accounts.find((a) => a.id === id);
    if (!account) {
      return this.wrapError('ACC-404', 'Account not found', 404);
    }
    return of(new HttpResponse({ status: 200, body: account }));
  }

  private handleGetBalance(
    id: number
  ): Observable<HttpResponse<{ balance: number } | ErrorResponse>> {
    const account = this.accounts.find((a) => a.id === id);
    if (!account) {
      return this.wrapError('ACC-404', 'Account not found', 404);
    }
    return of(new HttpResponse({ status: 200, body: { balance: account.balance } }));
  }

  private handleGetTransactions(
    id: number
  ): Observable<HttpResponse<TransactionLog[] | ErrorResponse>> {
    const account = this.accounts.find((a) => a.id === id);
    if (!account) {
      return this.wrapError('ACC-404', 'Account not found', 404);
    }

    const list = this.transactions.filter(
      (t) => t.fromAccountId === id || t.toAccountId === id
    );
    // Newest first
    list.sort(
      (a, b) =>
        new Date(b.createdOn).getTime() - new Date(a.createdOn).getTime()
    );

    return of(new HttpResponse({ status: 200, body: list }));
  }

  private handleTransfer(
    req: HttpRequest<unknown>
  ): Observable<HttpResponse<TransferSuccessResponse | ErrorResponse>> {
    const body = req.body as TransferRequest;

    // Basic validation aligned with business rules.
    if (body.fromAccountId === body.toAccountId) {
      return this.wrapError(
        'VAL-422',
        'Accounts must be different',
        422
      );
    }

    const from = this.accounts.find((a) => a.id === body.fromAccountId);
    const to = this.accounts.find((a) => a.id === body.toAccountId);

    if (!from) {
      return this.wrapError('ACC-404', 'Source account not found', 404);
    }
    if (!to) {
      return this.wrapError('ACC-404', 'Destination account not found', 404);
    }
    if (from.status !== 'ACTIVE') {
      return this.wrapError('ACC-403', 'Source account not active', 403);
    }
    if (to.status !== 'ACTIVE') {
      return this.wrapError('ACC-403', 'Destination account not active', 403);
    }
    if (body.amount <= 0) {
      return this.wrapError('VAL-422', 'Amount must be greater than 0', 422);
    }

    // Idempotency check
    if (body.idempotencyKey) {
      const existing = this.transactions.find(
        (t) => t.idempotencyKey === body.idempotencyKey
      );
      if (existing) {
        return this.wrapError(
          'TRX-409',
          'Duplicate transfer request',
          409
        );
      }
    }

    if (from.balance < body.amount) {
      return this.wrapError(
        'TRX-400',
        'Insufficient balance',
        400
      );
    }

    // Debit before credit
    from.balance -= body.amount;
    to.balance += body.amount;
    const nowIso = new Date().toISOString();
    from.lastUpdated = nowIso;
    to.lastUpdated = nowIso;

    const trxId = this.generateTransactionId();

    const trx: TransactionLog = {
      id: trxId,
      fromAccountId: from.id,
      toAccountId: to.id,
      amount: body.amount,
      status: 'SUCCESS',
      failureReason: null,
      idempotencyKey: body.idempotencyKey,
      createdOn: nowIso,
    };

    this.transactions.push(trx);

    const response: TransferSuccessResponse = {
      transactionId: trxId,
      status: 'SUCCESS',
      message: 'Transfer completed',
      debitedFrom: from.id,
      creditedTo: to.id,
      amount: body.amount,
    };

    return of(new HttpResponse({ status: 200, body: response }));
  }

  private wrapError(
    errorCode: string,
    message: string,
    status: number
  ): Observable<HttpResponse<ErrorResponse>> {
    const error: ErrorResponse = { errorCode, message };
    return throwError(
      () =>
        new HttpErrorResponse({
          status,
          error,
        })
    );
  }

  private generateTransactionId(): string {
    return `TRX-${Date.now().toString(36)}-${Math.random()
      .toString(16)
      .slice(2, 8)
      .toUpperCase()}`;
  }
}

