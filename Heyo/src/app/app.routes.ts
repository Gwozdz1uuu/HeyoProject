import { Routes } from '@angular/router';
import { authGuard } from './guards/auth.guard';
import { LoginComponent } from './login/login.component';
import { LayoutComponent } from './layout/layout.component';
import { HomeComponent } from './home/home.component';
import { DashboardComponent } from './dashboard/dashboard.component';
import { RegisterComponent } from './register/register.component';
import { RegisterProfileComponent } from './register-profile/register-profile.component';
import { MessagesComponent } from './messages/messages.component';
import { NotificationsComponent } from './notifications/notifications.component';
import { EventsComponent } from './events/events.component';
import { FriendsComponent } from './friends/friends.component';
import { ProfileComponent } from './profile/profile.component';
import { SettingsComponent } from './settings/settings.component';

export const routes: Routes = [
  {
    path: 'login',
    component: LoginComponent
  },
  {
    path: 'register',
    component: RegisterComponent
  },
  {
    path: 'register/profile',
    component: RegisterProfileComponent,
    canActivate: [authGuard]
  },
  {
    path: '',
    component: LayoutComponent,
    canActivate: [authGuard],
    children: [
      {
        path: 'home',
        component: HomeComponent
      },
      {
        path: 'dashboard',
        component: DashboardComponent
      },
      {
        path: 'messages',
        component: NotificationsComponent
      },
      {
        path: 'chat',
        component: MessagesComponent
      },
      {
        path: 'events',
        component: EventsComponent
      },
      {
        path: 'friends',
        component: FriendsComponent
      },
      {
        path: 'profile',
        component: ProfileComponent
      },
      {
        path: 'settings',
        component: SettingsComponent
      },
      {
        path: '',
        redirectTo: '/home',
        pathMatch: 'full'
      }
    ]
  },
  {
    path: '**',
    redirectTo: '/login'
  }
];
