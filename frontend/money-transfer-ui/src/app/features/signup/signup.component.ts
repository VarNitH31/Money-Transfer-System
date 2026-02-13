import { Component, inject, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { ToastComponent } from '../../toast/toast.component';

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

  form = this.fb.nonNullable.group({
    username: ['', [Validators.required, Validators.minLength(3)]],
    password: ['', [Validators.required, Validators.minLength(4)]],
  });
  
    goToLogin() {
    this.router.navigate(['/']);
  }


  submit(): void {
  if (this.loading) return;

  if (this.form.invalid) {
    this.form.markAllAsTouched();
    this.toast.show('Please fill all required fields correctly', 'error');
    return;
  }

    console.log(this.toast)

    const payload = this.form.getRawValue();
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
          err?.error?.message || 'Signup failed',
          'error'
        );
      },
    });
  }
}