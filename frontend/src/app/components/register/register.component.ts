import { Component } from '@angular/core';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { AlertService } from '../../services/alert.service';

@Component({
    selector: 'app-register',
    templateUrl: './register.component.html',
    styleUrls: ['./register.component.css']
})
export class RegisterComponent {
    registerForm = new FormGroup({
        email: new FormControl('', [Validators.required, Validators.email]),
        username: new FormControl('', [Validators.required]),
        password: new FormControl('', [
            Validators.required,
            Validators.minLength(8),
            Validators.maxLength(20)
        ]),
        repeatedPassword: new FormControl('', [Validators.required])
    });

    constructor(
        private authService: AuthService,
        private router: Router,
        private alertService: AlertService
    ) {}

    get email() {
        return this.registerForm.get('email');
    }

    get password() {
        return this.registerForm.get('password');
    }

    get username() {
        return this.registerForm.get('username');
    }

    get repeatedPassword() {
        return this.registerForm.get('repeatedPassword');
    }

    clearPassword() {
        this.registerForm.get('password')?.reset();
        this.registerForm.get('repeatedPassword')?.reset();
    }

    onSubmit() {
        if (this.registerForm.valid) {
            const email = this.registerForm.getRawValue().email;
            const username = this.registerForm.getRawValue().username;
            const password = this.registerForm.getRawValue().password;
            const repeatedP = this.registerForm.getRawValue().repeatedPassword;

            if (password !== repeatedP) {
                this.repeatedPassword?.setErrors({ notSame: true });
                return;
            }

            this.authService
                .register(
                    email!.toString(),
                    username!.toString(),
                    password!.toString()
                )
                .subscribe(
                    (result) => {
                        if (result.status == 201) {
                            this.router.navigate(['/login'], {
                                queryParams: { 'register-success': true }
                            });
                        } else {
                            this.clearPassword();
                        }
                    },
                    (error) => {
                        this.alertService.showAlert(
                            'error',
                            error.error.message
                        );
                        this.clearPassword();
                    }
                );
        }
    }
}
