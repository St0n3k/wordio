import { Pipe, PipeTransform } from '@angular/core';

@Pipe({ name: 'usernamePipe' })
export class UsernamePipe implements PipeTransform {
    transform(username: string | null, amount: number): string {
        if (username === null) {
            return '';
        }
        if (username.length > amount) {
            return username.slice(0, amount) + '...';
        }
        return username;
    }
}
