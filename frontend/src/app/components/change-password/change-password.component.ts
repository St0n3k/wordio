import { Component } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';
import { AlertService } from '../../services/alert.service';

@Component({
    selector: 'app-change-password',
    templateUrl: './change-password.component.html',
    styleUrls: ['./change-password.component.css']
})
export class ChangePasswordComponent {
    changePasswordForm = new FormGroup({
        oldPassword: new FormControl('', [Validators.required]), //Validators.email]),
        newPassword: new FormControl('', [
            Validators.required,
            Validators.minLength(8),
            Validators.maxLength(60)
        ]),
        newPasswordConfirm: new FormControl('', [
            Validators.required,
            Validators.minLength(8),
            Validators.maxLength(60)
        ])
    });

    constructor(
        private authService: AuthService,
        private router: Router,
        private alertService: AlertService
    ) {}

    get oldPassword() {
        return this.changePasswordForm.get('oldPassword');
    }

    get newPasswordConfirm() {
        return this.changePasswordForm.get('newPasswordConfirm');
    }

    get newPassword() {
        return this.changePasswordForm.get('newPassword');
    }

    onSubmit() {
        if (this.changePasswordForm.valid) {
            const oldPassword =
                this.changePasswordForm.getRawValue().oldPassword;
            const newPassword =
                this.changePasswordForm.getRawValue().newPassword;
            const newPasswordConfirm =
                this.changePasswordForm.getRawValue().newPasswordConfirm;

            if (newPassword != newPasswordConfirm) {
                this.newPasswordConfirm?.setErrors({ notSame: true });
                return;
            }
            if (oldPassword === newPassword) {
                this.newPassword?.setErrors({ sameAsOld: true });
                return;
            }

            this.authService
                .changePassword(
                    oldPassword!.toString(),
                    newPassword!.toString()
                )
                .subscribe(
                    (result) => {
                        if (result.status == 200) {
                            this.alertService.showAlert(
                                'success',
                                'Successfully changed password!'
                            );
                            this.clearInputs();
                        }
                    },
                    (error) => {
                        this.alertService.showAlert(
                            'error',
                            error.error.message
                        );
                    }
                );
        }
    }

    clearInputs() {
        this.changePasswordForm.get('oldPassword')?.reset();
        this.changePasswordForm.get('newPassword')?.reset();
        this.changePasswordForm.get('newPasswordConfirm')?.reset();
    }
}
