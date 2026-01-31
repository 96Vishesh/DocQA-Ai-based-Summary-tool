import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';

interface AuthResponse {
    token: string;
    expiresIn?: number;
}

@Injectable({
    providedIn: 'root'
})
export class AuthService {
    private readonly TOKEN_KEY = 'docqa_token';
    private isAuthenticatedSubject = new BehaviorSubject<boolean>(this.hasToken());

    isAuthenticated$ = this.isAuthenticatedSubject.asObservable();

    constructor(private http: HttpClient) { }

    login(email: string, password: string): Observable<AuthResponse> {
        return this.http.post<AuthResponse>('/api/auth/login', { email, password }).pipe(
            tap(response => {
                this.setToken(response.token);
                this.isAuthenticatedSubject.next(true);
            })
        );
    }

    register(name: string, email: string, password: string): Observable<AuthResponse> {
        return this.http.post<AuthResponse>('/api/auth/register', { name, email, password }).pipe(
            tap(response => {
                this.setToken(response.token);
                this.isAuthenticatedSubject.next(true);
            })
        );
    }

    logout(): void {
        localStorage.removeItem(this.TOKEN_KEY);
        this.isAuthenticatedSubject.next(false);
    }

    getToken(): string | null {
        return localStorage.getItem(this.TOKEN_KEY);
    }

    isAuthenticated(): boolean {
        return this.hasToken();
    }

    private setToken(token: string): void {
        localStorage.setItem(this.TOKEN_KEY, token);
    }

    private hasToken(): boolean {
        return !!localStorage.getItem(this.TOKEN_KEY);
    }
}
