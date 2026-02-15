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

  form = this.fb.nonNullable.group(
    {
      username: ['', [
        Validators.required,
        Validators.minLength(4),
        Validators.maxLength(20),
        Validators.pattern(/^[a-zA-Z0-9_]+$/),
      ]],
      password: ['', [
        Validators.required,
        Validators.minLength(6),
        Validators.pattern(/^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d!@#$%^&+=]*$/),
      ]],
      confirmPassword: ['', [Validators.required]],
    },
    { validators: passwordMatchValidator() }
  );
  
    goToLogin() {
    this.router.navigate(['/']);
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

    const { confirmPassword: _, ...payload } = this.form.getRawValue();
    this.loading = true;

    this.auth.signup(payload).subscribe({
      next: () => {

        this.toast.show('Account created successfully', 'success');

        // auto login
        this.auth.login(payload).subscribe({
          next: () => {
            this.loading = false;
            this.toast.show('Logged in', 'success');
            setTimeout(() => this.router.navigate(['/dashboard']), 800);
          },
          error: () => {
            this.loading = false;
            this.toast.show('Signup ok but login failed', 'error');
          },
        });
      },
      error: (err) => {
        this.loading = false;
        this.toast.show(
           err?.error?.details?.username || err?.error?.details?.password || err?.error?.message || 'Signup failed',
          'error'
        );
      },
    });
  }
}