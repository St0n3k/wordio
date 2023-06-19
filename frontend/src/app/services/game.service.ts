import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { environment } from 'src/environments/environment';
import { CreateGameResponse } from '../model/createGameResponse';

@Injectable({
    providedIn: 'root'
})
export class GameService {
    constructor(private http: HttpClient, private router: Router) {}

    createGame(CreateGameDTO: object) {
        return this.http.post<CreateGameResponse>(
            `${environment.apiUrl}/games`,
            CreateGameDTO,
            { observe: 'response' }
        );
    }
}
