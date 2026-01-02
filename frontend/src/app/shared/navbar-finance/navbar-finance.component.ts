import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-navbar-finance',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './navbar-finance.component.html',
  styleUrls: ['./navbar-finance.component.css']
})
export class NavbarFinanceComponent {}
