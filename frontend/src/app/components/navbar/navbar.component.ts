import { Component } from '@angular/core';
import { AuthService } from '../../services/auth.service';
import { AlertService } from '../../services/alert.service';

@Component({
    selector: 'app-navbar',
    templateUrl: './navbar.component.html',
    styleUrls: ['./navbar.component.css']
})
export class NavbarComponent {
    isCollapsed = true;

    constructor(
        protected authService: AuthService,
        private alertService: AlertService
    ) {}

    logout() {
        this.authService.logout();
        this.alertService.showAlert('success', 'Logged out successfully!');
    }
}
