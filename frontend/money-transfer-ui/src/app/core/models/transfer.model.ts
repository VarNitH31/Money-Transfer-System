export interface TransferRequest {
  fromAccountId: number;
  toAccountId: number;
  amount: number;
  idempotencyKey: string;
}

export interface TransferSuccessResponse {
  transactionId: string;
  status: 'SUCCESS';
  message: string;
  debitedFrom: number;
  creditedTo: number;
  amount: number;
}

export interface ErrorResponse {
  errorCode: string;
  message: string;
}

