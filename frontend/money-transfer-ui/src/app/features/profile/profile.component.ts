import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import { Account } from '../../core/models/account.model';
import { RewardRedeemResponse } from '../../core/models/reward-redeem-response.model';
import { AccountService } from '../../core/services/account.service';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.scss'
})
export class ProfileComponent implements OnInit {

  private readonly accountService = inject(AccountService);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  // =========================
  // Account Data
  // =========================

  account = signal<Account | null>(null);

  loading = signal(true);
  error = signal<string | null>(null);

  // =========================
  // Deactivate Modal
  // =========================

  showDeactivateModal = signal(false);

  // =========================
  // PIN Modal
  // =========================

  showPinModal = signal(false);

  /**
   * Step 1:
   * Enter current PIN
   *
   * Step 2:
   * Enter new PIN + confirm
   */
  pinStep = signal(1);

  currentPin = '';
  newPin = '';
  confirmPin = '';

  pinError = signal<string | null>(null);

  // =========================
  // Toasts
  // =========================

  showPinToast = signal(false);
  showRewardToast = signal(false);

  ngOnInit(): void {

    const accountId = this.auth.getAccountId();

    if (!accountId) {
      this.router.navigate(['/login']);
      return;
    }

    this.accountService.getAccount(accountId).subscribe({
      next: account => {
        this.account.set(account);
        this.loading.set(false);
      },
      error: err => {
        this.error.set(
          err?.error?.message ||
          'Unable to load profile.'
        );
        this.loading.set(false);
      }
    });
  }

  // =========================
  // Logout
  // =========================

  logout(): void {
    this.auth.logout();
    this.router.navigate(['/login']);
  }

  // =========================
  // Rewards
  // =========================
redeemRewards(): void {

  const account = this.account();

  if (!account) {
    return;
  }

  this.accountService
    .redeemRewards(account.accountId)
    .subscribe({

      next: (response: RewardRedeemResponse) => {

        this.account.set({
          ...account,
          balance: response.newBalance,
          rewardPoints: response.remainingPoints
        });

        this.showRewardToast.set(true);

        setTimeout(() => {
          this.showRewardToast.set(false);
        }, 3000);
      },

      error: err => {

        this.error.set(
          err?.error?.message ??
          'Failed to redeem rewards'
        );
      }
    });
}

  // =========================
  // PIN RESET
  // =========================

  openPinModal(): void {

    this.pinStep.set(1);

    this.currentPin = '';
    this.newPin = '';
    this.confirmPin = '';

    this.pinError.set(null);

    this.showPinModal.set(true);
  }

  closePinModal(): void {

    this.showPinModal.set(false);

    this.currentPin = '';
    this.newPin = '';
    this.confirmPin = '';

    this.pinError.set(null);
  }

  verifyCurrentPin(): void {

    if (!this.currentPin.trim()) {
      this.pinError.set('Please enter your current PIN');
      return;
    }

    /**
     * TODO BACKEND
     *
     * Verify current PIN
     */

    this.pinError.set(null);
    this.pinStep.set(2);
  }

  resetPin(): void {

    if (!this.newPin.trim()) {
      this.pinError.set('Please enter a new PIN');
      return;
    }

    if (this.newPin.length < 4) {
      this.pinError.set('PIN must contain at least 4 digits');
      return;
    }

    if (this.newPin !== this.confirmPin) {
      this.pinError.set('PINs do not match');
      return;
    }

    /**
     * TODO BACKEND
     *
     * this.accountService.resetPin(...)
     */

    console.log('PIN RESET SUCCESS');

    this.closePinModal();

    this.showPinToast.set(true);

    setTimeout(() => {
      this.showPinToast.set(false);
    }, 3000);
  }

  // =========================
  // DEACTIVATE ACCOUNT
  // =========================

  disableAccount(): void {
    this.showDeactivateModal.set(true);
  }

  closeDeactivateModal(): void {
    this.showDeactivateModal.set(false);
  }

confirmDeactivate(): void {

  const account = this.account();

  if (!account) {
    return;
  }

  this.accountService
    .deactivateAccount(account.accountId)
    .subscribe({

      next: () => {

        this.account.set({
          ...account,
          status: 'LOCKED'
        });

        this.closeDeactivateModal();

        this.auth.logout();

        this.router.navigate(['/login']);
      },

      error: err => {

        this.error.set(
          err?.error?.message ??
          'Failed to deactivate account'
        );
      }
    });
}

  goBack(): void {
  this.router.navigate(['/dashboard']);
}

}