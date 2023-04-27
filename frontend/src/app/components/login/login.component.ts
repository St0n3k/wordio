import { Component } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { AlertService } from '../../services/alert.service';

@Component({
    selector: 'app-login',
    templateUrl: './login.component.html'
})
export class LoginComponent {
    loginForm = new FormGroup({
        username: new FormControl('', [Validators.required]), //Validators.email]),
        password: new FormControl('', [Validators.required])
    });

    constructor(
        private authService: AuthService,
        private router: Router,
        private alertService: AlertService
    ) {}

    get passwordInput() {
        return this.loginForm.get('password');
    }

    onSubmit() {
        if (this.loginForm.valid) {
            const username = this.loginForm.getRawValue().username;
            const password = this.loginForm.getRawValue().password;

            this.authService
                .login(username!.toString(), password!.toString())
                .subscribe(
                    (result) => {
                        if (result.status == 200) {
                            this.authService.saveUserData(result);
                            this.authService.authenticated.next(true);
                            this.router.navigate(['/profile']);
                        }
                    },
                    (error) => {
                        this.authService.clearUserData();
                        this.authService.authenticated.next(false);
                        this.alertService.showAlert(
                            'error',
                            error.error.message
                        );
                        this.clearPassword();
                    }
                );
        }
    }

    clearPassword() {
        this.loginForm.get('password')?.reset();
    }
}
