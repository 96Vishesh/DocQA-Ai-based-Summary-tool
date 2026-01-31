import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, RouterLink, Router } from '@angular/router';
import { AuthService } from './services/auth.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink],
  template: `
    <div class="layout">
      <header class="header">
        <div class="container">
          <a routerLink="/" class="logo">
            <div class="logo-icon">📄</div>
            <span class="logo-text">DocQA</span>
          </a>
          <nav class="nav">
            <a routerLink="/" class="nav-link">
              <span>🏠</span>
              <span>Dashboard</span>
            </a>
          </nav>
          <div class="header-right">
            @if (isAuthenticated) {
              <button class="btn btn-secondary" (click)="openAIAssistant()">
                <span>💬</span>
                <span>AI Assistant</span>
              </button>
              <button class="btn btn-outline" (click)="logout()">
                <span>🚪</span>
                <span>Logout</span>
              </button>
            } @else {
              <a routerLink="/login" class="btn btn-outline">
                <span>Sign In</span>
              </a>
              <a routerLink="/register" class="btn btn-primary">
                <span>Sign Up</span>
              </a>
            }
          </div>
        </div>
      </header>
      <main class="main">
        <router-outlet></router-outlet>
      </main>
      <footer class="footer">
        <div class="container">
          <p>© 2024 DocQA - AI-Powered Document Assistant</p>
        </div>
      </footer>
    </div>
  `,
  styles: [`
    .layout {
      display: flex;
      flex-direction: column;
      min-height: 100vh;
    }

    .header {
      position: sticky;
      top: 0;
      z-index: 100;
      background: rgba(10, 10, 15, 0.8);
      backdrop-filter: blur(20px);
      border-bottom: 1px solid var(--border-color);
    }

    .header .container {
      display: flex;
      align-items: center;
      justify-content: space-between;
      height: 72px;
    }

    .logo {
      display: flex;
      align-items: center;
      gap: 12px;
      text-decoration: none;
    }

    .logo-icon {
      width: 44px;
      height: 44px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: var(--accent-gradient);
      border-radius: var(--radius-md);
      font-size: 1.4rem;
    }

    .logo-text {
      font-size: 1.5rem;
      font-weight: 700;
      background: var(--accent-gradient);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
    }

    .nav {
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .nav-link {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 10px 16px;
      border-radius: var(--radius-md);
      color: var(--text-secondary);
      font-weight: 500;
      transition: all var(--transition-fast);
      text-decoration: none;
    }

    .nav-link:hover {
      background: var(--bg-glass);
      color: var(--text-primary);
    }

    .header-right {
      display: flex;
      align-items: center;
      gap: 12px;
    }

    .header-right a.btn {
      text-decoration: none;
    }

    .btn-outline {
      background: transparent;
      border: 1px solid var(--border-color);
      color: var(--text-primary);
    }

    .btn-outline:hover {
      background: var(--bg-glass);
      border-color: var(--accent-primary);
    }

    .main {
      flex: 1;
      padding: 32px 0;
    }

    .footer {
      padding: 24px 0;
      border-top: 1px solid var(--border-color);
      text-align: center;
    }

    .footer p {
      font-size: 0.9rem;
      color: var(--text-muted);
    }
  `]
})
export class AppComponent {
  title = 'DocQA';
  isAuthenticated = false;

  constructor(private authService: AuthService, private router: Router) {
    this.authService.isAuthenticated$.subscribe(
      isAuth => this.isAuthenticated = isAuth
    );
  }

  openAIAssistant() {
    // Navigate to dashboard where the chatbot is available
    this.router.navigate(['/']);
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
