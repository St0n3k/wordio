import { Component, OnInit } from '@angular/core';
import { AlertService } from '../../services/alert.service';

@Component({
    selector: 'app-alert',
    templateUrl: './alert.component.html',
    styleUrls: ['./alert.component.css']
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
            let timeoutString = localStorage.getItem('alertTimeout');
            if (!timeoutString) {
                timeoutString = '5000';
            }
            const timeout = JSON.parse(timeoutString);
            if (timeout != 0) {
                const callback = setTimeout(() => {
                    this.resetAlerts();
                }, timeout);
                clearTimeout(this.lastCallback);
                this.lastCallback = callback;
            }
        });
    }

    resetAlerts() {
        this.error = false;
    }
}
