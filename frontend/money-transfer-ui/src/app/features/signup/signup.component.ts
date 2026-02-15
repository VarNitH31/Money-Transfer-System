import { Component, inject, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { ToastComponent } from '../../toast/toast.component';

function passwordMatchValidator(): ValidatorFn {
  return (form: AbstractControl): ValidationErrors | null => {
    const password = form.get('password')?.value;
    const confirmPassword = form.get('confirmPassword')?.value;
    if (!password || !confirmPassword) return null;
    return password === confirmPassword ? null : { passwordMismatch: true };
  };
}

@Component({
  selector: 'app-signup',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, ToastComponent],
  templateUrl: './signup.component.html',
  styleUrl: './signup.component.scss',
})
export class SignupComponent {

  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  @ViewChild(ToastComponent) toast!: ToastComponent;

  loading = false;
  showOtpStep = false;
  pendingUsername = '';
  pendingPassword = '';

  form = this.fb.nonNullable.group(
    {
      username: ['', [
        Validators.required,
        Validators.minLength(4),
        Validators.maxLength(20),
        Validators.pattern(/^[a-zA-Z0-9_]+$/),
      ]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [
        Validators.required,
        Validators.minLength(6),
        Validators.pattern(/^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d!@#$%^&+=]*$/),
      ]],
      confirmPassword: ['', [Validators.required]],
    },
    { validators: passwordMatchValidator() }
  );

  otpForm = this.fb.nonNullable.group({
    otp: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(6), Validators.pattern(/^\d+$/)]],
  });

  goToLogin(): void {
    this.router.navigate(['/login']);
  }

  submit(): void {
    if (this.loading) return;

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      const err = this.form.errors?.['passwordMismatch']
        ? 'Passwords do not match'
        : 'Please fill all required fields correctly';
      this.toast.show(err, 'error');
      return;
    }

    const { username, email, password, confirmPassword: _ } = this.form.getRawValue();
    const payload = { userName: username, email, password };
    this.loading = true;

    this.auth.signup(payload).subscribe({
      next: () => {
        this.loading = false;
        this.pendingUsername = username;
        this.pendingPassword = password;
        this.showOtpStep = true;
        this.otpForm.reset();
        this.toast.show('OTP sent to your email. Please verify.', 'success');
      },
      error: (err) => {
        this.loading = false;
        const msg = err?.error?.details?.userName ?? err?.error?.details?.email ?? err?.error?.details?.password ?? err?.error?.message ?? 'Signup failed';
        this.toast.show(msg, 'error');
      },
    });
  }

  submitOtp(): void {
    if (this.loading || !this.pendingUsername) return;

    if (this.otpForm.invalid) {
      this.otpForm.markAllAsTouched();
      this.toast.show('Please enter a valid 6-digit OTP', 'error');
      return;
    }

    this.loading = true;
    this.auth.verifyOtp({ username: this.pendingUsername, otp: this.otpForm.getRawValue().otp }).subscribe({
      next: () => {
        this.loading = false;
        this.toast.show('Email verified successfully. You can now log in.', 'success');
        // Auto-login after successful OTP verification
        this.auth.login({ username: this.pendingUsername, password: this.pendingPassword }).subscribe({
          next: () => {
            this.toast.show('Logged in', 'success');
            setTimeout(() => this.router.navigate(['/dashboard']), 800);
          },
          error: () => {
            this.router.navigate(['/login']);
          },
        });
      },
      error: (err) => {
        this.loading = false;
        this.toast.show(err?.error?.message ?? 'Invalid or expired OTP', 'error');
      },
    });
  }

  backToSignup(): void {
    this.showOtpStep = false;
    this.pendingUsername = '';
    this.pendingPassword = '';
    this.otpForm.reset();
  }
}