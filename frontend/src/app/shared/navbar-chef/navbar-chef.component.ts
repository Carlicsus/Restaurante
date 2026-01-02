import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-navbar-chef',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './navbar-chef.component.html',
  styleUrls: ['./navbar-chef.component.css']
})
export class NavbarChefComponent {}
