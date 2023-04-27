import { Component } from '@angular/core';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';
import { AlertService } from '../../services/alert.service';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';

@Component({
    selector: 'app-delete-account',
    templateUrl: './delete-account.component.html',
    styleUrls: ['./delete-account.component.css']
})
export class DeleteAccountComponent {
    deleteModal: NgbModalRef | undefined;

    constructor(
        private authService: AuthService,
        private router: Router,
        private alertService: AlertService,
        private modalService: NgbModal
    ) {}

    showDeleteModal(deleteModal: any): void {
        this.deleteModal = this.modalService.open(deleteModal, {
            size: 'xl',
            centered: true,
            scrollable: true
        });
    }

    deleteAccount() {
        this.authService.deleteAccount().subscribe(
            (result) => {
                if (result.status == 200) {
                    this.alertService.showAlert(
                        'success',
                        'Successfully deleted account!'
                    );
                    this.authService.logout();
                }
            },
            (error) => {
                this.alertService.showAlert('error', error.error.message);
            }
        );
    }
}
