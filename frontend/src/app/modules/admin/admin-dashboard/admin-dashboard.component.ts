import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { NavbarAdminComponent } from '../../../shared/navbar-admin/navbar-admin.component';

@Component({
  selector: 'app-admin-dashboard',
  imports: [CommonModule, NavbarAdminComponent],
  templateUrl: './admin-dashboard.component.html',
  styleUrl: './admin-dashboard.component.css'
})
export class AdminDashboardComponent implements OnInit {
  totalUsers = 0;
  activeUsers = 0;
  pendingUsers = 0;
  totalRoles = 4;

  constructor(private router: Router) {}

  ngOnInit(): void {
    this.loadStats();
  }

  loadStats(): void {
    // TODO: Cargar estadísticas reales del backend
    this.totalUsers = 25;
    this.activeUsers = 18;
    this.pendingUsers = 7;
    this.totalRoles = 4;
  }

  navigateTo(route: string): void {
    this.router.navigateByUrl(route);
  }
}
