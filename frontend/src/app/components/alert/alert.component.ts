import { Component, OnInit } from '@angular/core';
import { AlertService } from '../../services/alert.service';

@Component({
    selector: 'app-alert',
    templateUrl: './alert.component.html'
})
export class AlertComponent {
    error = false;
    success = false;
    message = '';
    lastCallback: any;

    constructor(private alertService: AlertService) {
        this.alertService.alert.subscribe((event) => {
            this.resetAlerts();
            this.message = event.message;
            switch (event.type) {
                case 'error': {
                    this.error = true;
                    break;
                }
                case 'success': {
                    this.success = true;
                    break;
                }
            }
            const timeout = 5000;
            const callback = setTimeout(() => {
                this.resetAlerts();
            }, timeout);
            clearTimeout(this.lastCallback);
            this.lastCallback = callback;
        });
    }

    resetAlerts() {
        this.error = false;
    }
}
