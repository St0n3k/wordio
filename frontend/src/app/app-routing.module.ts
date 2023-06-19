import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LoginComponent } from './components/login/login.component';
import { RegisterComponent } from './components/register/register.component';
import { ProfileComponent } from './components/profile/profile.component';
import { PlayerGuard } from './guards/player.guard';
import { LoginRegisterGuard } from './guards/login-register.guard';
import { GameSettingsComponent } from './components/game-settings/game-settings.component';

const routes: Routes = [
    {
        path: 'login',
        component: LoginComponent,
        canActivate: [LoginRegisterGuard]
    },
    {
        path: 'register',
        component: RegisterComponent,
        canActivate: [LoginRegisterGuard]
    },
    {
        path: 'profile',
        component: ProfileComponent,
        canActivate: [PlayerGuard]
    },
    {
        path: 'game-settings',
        component: GameSettingsComponent,
        canActivate: [PlayerGuard]
    }
];

@NgModule({
    imports: [RouterModule.forRoot(routes)],
    exports: [RouterModule]
})
export class AppRoutingModule {}
