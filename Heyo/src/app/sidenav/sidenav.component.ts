import { Component, inject, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive, Router } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { AuthService } from '../services/auth.service';

interface NavItem {
  label: string;
  route: string;
  icon: string;
  color?: string;
}

@Component({
  selector: 'app-sidenav',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    RouterLinkActive,
    MatIconModule
  ],
  templateUrl: './sidenav.component.html',
  styleUrl: './sidenav.component.css'
})
export class SidenavComponent {
  private authService = inject(AuthService);
  private router = inject(Router);

  navItems: NavItem[] = [
    { label: 'Dla Ciebie', route: '/home', icon: '✨' },
    { label: 'Znajomi', route: '/friends', icon: '👥' },
    { label: 'Wiadomości', route: '/messages', icon: '🔔' },
    { label: 'Chat', route: '/chat', icon: '💬' },
    { label: 'Wydarzenia', route: '/events', icon: '📅' }
  ];

  showMoreMenu = false;

  toggleMoreMenu(event: Event): void {
    event.preventDefault();
    event.stopPropagation();
    this.showMoreMenu = !this.showMoreMenu;
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: Event): void {
    if (this.showMoreMenu) {
      this.showMoreMenu = false;
    }
  }

  onMenuAction(action: string): void {
    this.showMoreMenu = false;
    switch (action) {
      case 'chat':
        this.router.navigate(['/chat/new']);
        break;
      case 'settings':
        this.router.navigate(['/settings']);
        break;
      case 'profile':
        this.router.navigate(['/profile']);
        break;
      case 'logout':
        this.authService.logout();
        break;
    }
  }
}
