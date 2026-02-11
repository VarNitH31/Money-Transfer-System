import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { LoginRequest, LoginResponse } from '../models/auth.model';
import { environment } from '../../../environments/environment';

const TOKEN_KEY = 'mts_token';
const ACCOUNT_ID_KEY = 'mts_account_id';
const HOLDER_NAME_KEY = 'mts_holder_name';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  readonly isLoggedIn = signal<boolean>(this.hasToken());
  readonly holderName = signal<string | null>(localStorage.getItem(HOLDER_NAME_KEY));

  constructor(private http: HttpClient) {}

  login(payload: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${environment.apiBaseUrl}/auth/login`, payload).pipe(
      tap((res) => {
        console.log(res);
        localStorage.setItem(TOKEN_KEY, res.token);
        localStorage.setItem(ACCOUNT_ID_KEY, String(res.accountId));
        localStorage.setItem(HOLDER_NAME_KEY, res.holderName);
        this.isLoggedIn.set(true);
        this.holderName.set(res.holderName);
      })
    );
  }

  logout(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(ACCOUNT_ID_KEY);
    localStorage.removeItem(HOLDER_NAME_KEY);
    this.isLoggedIn.set(false);
    this.holderName.set(null);
  }

  isAuthenticated(): boolean {
    return this.hasToken();
  }

  getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  getAccountId(): number | null {
    const raw = localStorage.getItem(ACCOUNT_ID_KEY);
    return raw ? Number(raw) : null;
  }

  getHolderName(): string | null {
    return localStorage.getItem(HOLDER_NAME_KEY);
  }

  private hasToken(): boolean {
    return !!localStorage.getItem(TOKEN_KEY);
  }
}

