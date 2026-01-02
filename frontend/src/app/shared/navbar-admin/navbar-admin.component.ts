import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-navbar-admin',
  imports: [CommonModule, RouterModule],
  templateUrl: './navbar-admin.component.html',
  styleUrl: './navbar-admin.component.css'
})
export class NavbarAdminComponent {
  showProfileMenu = false;
  showMobileMenu = false;
  userAvatar = '/fperfil.jpg';

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
    this.authService.logout();
    this.showProfileMenu = false;
    this.router.navigate(['']);
  }
}
