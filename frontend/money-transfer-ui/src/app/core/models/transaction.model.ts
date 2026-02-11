export type TransactionStatus = 'SUCCESS' | 'FAILED';

export interface TransactionLog {
  id: string;
  fromAccountId: number;
  toAccountId: number;
  amount: number;
  status: TransactionStatus;
  failureReason?: string | null;
  idempotencyKey?: string;
  createdOn: string;
}

