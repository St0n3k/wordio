import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { LoginResponse } from '../model/loginResponse';
import { environment } from '../../environments/environment';
import jwt_decode from 'jwt-decode';
import { Router } from '@angular/router';
import { BehaviorSubject } from 'rxjs';

@Injectable({
    providedIn: 'root'
})
export class AuthService {
    authenticated = new BehaviorSubject(false);

    constructor(private http: HttpClient, private router: Router) {
        const jwt = localStorage.getItem('jwt');
        if (jwt != null) {
            const expires = this.getDecodedJwtToken(jwt).exp;
            if (expires > Date.now()) {
                window.location.href = '/login';
            }
            if (this.getDecodedJwtToken(jwt)) this.authenticated.next(true);
        }
        window.addEventListener(
            'storage',
            (event) => {
                if (event.storageArea == localStorage) {
                    const token = localStorage.getItem('jwt');
                    if (token == undefined) {
                        window.location.href = '/login';
                    }
                }
            },
            false
        );
    }

    login(username: string, password: string) {
        return this.http.post<LoginResponse>(
            `${environment.apiUrl}/auth/login`,
            { username, password },
            { observe: 'response' }
        );
    }

    saveUserData(result: any) {
        const tokenInfo = this.getDecodedJwtToken(result.body.jwt);
        localStorage.setItem('username', tokenInfo.sub);
        localStorage.setItem('jwt', result.body.jwt);
        localStorage.setItem('role', tokenInfo.role);
    }

    register(email: string, username: string, password: string) {
        return this.http.post(
            `${environment.apiUrl}/auth/register`,
            { email, username, password },
            { observe: 'response' }
        );
    }

    changePassword(oldPassword: string, newPassword: string) {
        return this.http.put<LoginResponse>(
            `${environment.apiUrl}/auth/password`,
            { oldPassword, newPassword },
            { observe: 'response' }
        );
    }

    deleteAccount() {
        return this.http.delete(`${environment.apiUrl}/user`, {
            observe: 'response'
        });
    }

    logout() {
        this.clearUserData();
        this.authenticated.next(false);
        this.router.navigate(['/login']);
    }

    getRole() {
        const token = localStorage.getItem('jwt');
        if (token === null) {
            return '';
        }
        const tokenInfo = this.getDecodedJwtToken(token);
        return tokenInfo.role;
    }

    getUsername() {
        return localStorage.getItem('username');
    }

    clearUserData() {
        localStorage.removeItem('role');
        localStorage.removeItem('username');
        localStorage.removeItem('jwt');
    }

    getDecodedJwtToken(token: string): any {
        try {
            return jwt_decode(token);
        } catch (Error) {
            return null;
        }
    }
}
