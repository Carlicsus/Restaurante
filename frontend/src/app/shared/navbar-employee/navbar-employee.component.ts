import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-navbar-employee',
  imports: [CommonModule, RouterModule],
  templateUrl: './navbar-employee.component.html',
  styleUrl: './navbar-employee.component.css'
})
export class NavbarEmployeeComponent {
  showProfileMenu = false;
  showMobileMenu = false;

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  toggleProfileMenu() {
    this.showProfileMenu = !this.showProfileMenu;
  }

  toggleMobileMenu() {
    if (window.innerWidth <= 768) {
      this.showMobileMenu = !this.showMobileMenu;
    }
  }

  logout() {
    this.authService.logout();
  }
}
