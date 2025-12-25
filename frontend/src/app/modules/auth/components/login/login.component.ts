import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../../core/services/auth.service';
import { CommonModule } from '@angular/common';
import { HttpClientModule } from '@angular/common/http';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, CommonModule, HttpClientModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  credentials = {
    username: '',
    password: ''
  };

  constructor(private auth: AuthService) {}

  onSubmit() {
    console.log('Attempting login with:', this.credentials);
    this.auth.login(this.credentials).subscribe({
      next: (response:any) => {
        console.log('Login successful', response);
        sessionStorage.setItem('token', response.access_token);
        sessionStorage.setItem('expiredToken', response.refresh_token)
      },
      error: (error) => {
        console.error('Login failed', error);
      }
    });
  }
}