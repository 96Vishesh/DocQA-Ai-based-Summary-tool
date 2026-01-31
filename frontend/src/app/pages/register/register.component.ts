import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink, Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
    selector: 'app-register',
    standalone: true,
    imports: [CommonModule, FormsModule, RouterLink],
    template: `
    <div class="auth-container">
      <div class="auth-card">
        <div class="auth-header">
          <h1>Create Account</h1>
          <p>Sign up to start using DocQA</p>
        </div>
        
        <form (ngSubmit)="onSubmit()" class="auth-form">
          <div class="form-group">
            <label for="name">Full Name</label>
            <input 
              type="text" 
              id="name" 
              [(ngModel)]="name" 
              name="name"
              placeholder="Enter your full name"
              required
            />
          </div>
          
          <div class="form-group">
            <label for="email">Email</label>
            <input 
              type="email" 
              id="email" 
              [(ngModel)]="email" 
              name="email"
              placeholder="Enter your email"
              required
            />
          </div>
          
          <div class="form-group">
            <label for="password">Password</label>
            <input 
              type="password" 
              id="password" 
              [(ngModel)]="password" 
              name="password"
              placeholder="Create a password (min. 6 characters)"
              required
            />
          </div>
          
          @if (error) {
            <div class="error-message">{{ error }}</div>
          }
          
          <button type="submit" class="btn btn-primary btn-full" [disabled]="loading">
            @if (loading) {
              <span class="spinner"></span>
            } @else {
              Create Account
            }
          </button>
        </form>
        
        <div class="auth-footer">
          <p>Already have an account? <a routerLink="/login">Sign in</a></p>
        </div>
      </div>
    </div>
  `,
    styles: [`
    .auth-container {
      min-height: calc(100vh - 200px);
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 24px;
    }

    .auth-card {
      width: 100%;
      max-width: 420px;
      background: var(--bg-secondary);
      border: 1px solid var(--border-color);
      border-radius: var(--radius-lg);
      padding: 40px;
    }

    .auth-header {
      text-align: center;
      margin-bottom: 32px;
    }

    .auth-header h1 {
      font-size: 1.8rem;
      margin-bottom: 8px;
    }

    .auth-header p {
      color: var(--text-secondary);
    }

    .auth-form {
      display: flex;
      flex-direction: column;
      gap: 20px;
    }

    .form-group {
      display: flex;
      flex-direction: column;
      gap: 8px;
    }

    .form-group label {
      font-weight: 500;
      color: var(--text-secondary);
    }

    .btn-full {
      width: 100%;
      padding: 14px;
      font-size: 1rem;
    }

    .error-message {
      background: rgba(239, 68, 68, 0.1);
      border: 1px solid rgba(239, 68, 68, 0.3);
      color: #ef4444;
      padding: 12px;
      border-radius: var(--radius-md);
      font-size: 0.9rem;
    }

    .auth-footer {
      text-align: center;
      margin-top: 24px;
      color: var(--text-secondary);
    }

    .auth-footer a {
      color: var(--accent-primary);
      text-decoration: none;
    }

    .auth-footer a:hover {
      text-decoration: underline;
    }
  `]
})
export class RegisterComponent {
    name = '';
    email = '';
    password = '';
    loading = false;
    error = '';

    constructor(private authService: AuthService, private router: Router) { }

    onSubmit() {
        if (!this.name || !this.email || !this.password) {
            this.error = 'Please fill in all fields';
            return;
        }

        if (this.password.length < 6) {
            this.error = 'Password must be at least 6 characters';
            return;
        }

        this.loading = true;
        this.error = '';

        this.authService.register(this.name, this.email, this.password).subscribe({
            next: () => {
                this.router.navigate(['/']);
            },
            error: (err) => {
                this.loading = false;
                this.error = err.error?.message || 'Registration failed. Please try again.';
            }
        });
    }
}
