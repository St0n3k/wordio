import { Component } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { GameService } from 'src/app/services/game.service';

@Component({
    selector: 'app-game-settings',
    templateUrl: './game-settings.component.html'
})
export class GameSettingsComponent {
    createGameForm = this.fb.group({
        countdown: [
            '',
            [Validators.required, Validators.min(1), Validators.max(30)]
        ],
        maxround: [
            '',
            [Validators.required, Validators.min(1), Validators.max(300)]
        ],
        categories: this.fb.array([])
    });

    categories: string[] = [];
    rounds = 1;

    constructor(
        private fb: FormBuilder,
        private gameService: GameService,
        private router: Router
    ) {
        this.getCategories().push(this.newCategory());
        this.categories.push(' ');
        this.getCategories().push(this.newCategory());
        this.categories.push(' ');
    }

    addCategory(): void {
        this.getCategories().push(this.newCategory());
        if (this.categories.length < 10) {
            this.categories?.push(' ');
        }
    }

    removeCategory(index: number): void {
        this.addFormToCategoryArray();
        this.getCategories().removeAt(index);
        this.categories?.splice(index, 1);
    }

    addFormToCategoryArray() {
        for (let i = 0; i < this.categories?.length; i++) {
            this.categories[i] = this.getCategories()
                .at(i)
                .getRawValue().category;
        }
    }

    addRound(): void {
        if (this.rounds < 10) {
            this.rounds = this.rounds + 1;
        }
    }

    removeRound(): void {
        if (this.rounds > 1) {
            this.rounds = this.rounds - 1;
        }
    }

    onSubmit() {
        this.addFormToCategoryArray();
        const CreateGameDTO: object = {
            numberOfRounds: this.rounds,
            maxRoundDurationTime: this.createGameForm.getRawValue().maxround,
            countdownTime: this.createGameForm.getRawValue().countdown,
            categories: this.categories
        };
        if (this.createGameForm.valid) {
            this.gameService.createGame(CreateGameDTO).subscribe((result) => {
                if (result.ok) {
                    this.router.navigate(['/']);
                }
            });
        }
    }

    getCategories(): FormArray {
        return this.createGameForm.get('categories') as FormArray;
    }

    newCategory(): FormGroup {
        return this.fb.group({
            category: [
                '',
                [
                    Validators.required,
                    Validators.maxLength(20),
                    Validators.minLength(3)
                ]
            ]
        });
    }

    get countdown() {
        return this.createGameForm.get('countdown');
    }

    get maxround() {
        return this.createGameForm.get('maxround');
    }
}
