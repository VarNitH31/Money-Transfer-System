import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Account, BalanceResponse } from '../models/account.model';
import { TransactionLog } from '../models/transaction.model';
import { environment } from '../../../environments/environment';
import { RewardRedeemResponse } from '../models/reward-redeem-response.model';

@Injectable({
  providedIn: 'root',
})
export class AccountService {
  constructor(private http: HttpClient) {}


  getAccount(id: number): Observable<Account> {
    console.log(`${environment.apiBaseUrl}/api/v1/accounts/${id}`);
    this.http.get<Account>(`${environment.apiBaseUrl}/api/v1/accounts/${id}`).subscribe((res) => {
      console.log(res);
    });
    return this.http.get<Account>(`${environment.apiBaseUrl}/api/v1/accounts/${id}`);
  }

  getBalance(id: number): Observable<BalanceResponse> {
    return this.http.get<BalanceResponse>(`${environment.apiBaseUrl}/api/v1/accounts/${id}/balance`);
  }

  getTransactions(id: number): Observable<TransactionLog[]> {
    return this.http.get<TransactionLog[]>(`${environment.apiBaseUrl}/api/v1/accounts/${id}/transactions`);
  }

  redeemRewards(accountId: number) {
  return this.http.post<RewardRedeemResponse>(
    `${environment.apiBaseUrl}/api/v1/accounts/${accountId}/redeem-rewards`,
    {}
  );
}

deactivateAccount(accountId: number) {
  return this.http.post(
    `${environment.apiBaseUrl}/api/v1/accounts/${accountId}/deactivate`,
    {}
  );
}
}

