export interface Account {
  id: number;
  holderName: string;
  balance: number;
  status: 'ACTIVE' | 'LOCKED' | 'CLOSED';
  lastUpdated?: string;
}

export interface BalanceResponse {
  balance: number;
}

