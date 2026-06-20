export interface Account {
  accountId: number;
  holderName: string;
  balance: number;
  rewardPoints: number;
  status: 'ACTIVE' | 'LOCKED' | 'CLOSED';
  lastUpdated?: string;
}

export interface BalanceResponse {
  balance: number;
}

