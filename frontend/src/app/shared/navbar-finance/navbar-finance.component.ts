import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-navbar-finance',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './navbar-finance.component.html',
  styleUrls: ['./navbar-finance.component.css']
})
export class NavbarFinanceComponent {
  showProfileMenu = false;
  showMobileMenu = false;

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  toggleProfileMenu(): void {
    this.showProfileMenu = !this.showProfileMenu;
  }

  toggleMobileMenu(): void {
    if (window.innerWidth <= 768) {
      this.showMobileMenu = !this.showMobileMenu;
    }
  }

  logout(): void {
    this.showProfileMenu = false;
    this.authService.logout();
  }
}
