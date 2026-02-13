import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ErrorResponse, TransferRequest, TransferSuccessResponse } from '../models/transfer.model';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class TransferService {
  constructor(private http: HttpClient) {}

  transfer(payload: TransferRequest): Observable<TransferSuccessResponse | ErrorResponse> {
    return this.http.post<TransferSuccessResponse | ErrorResponse>(`${environment.apiBaseUrl}/api/v1/transfers`, payload);
  }
}

