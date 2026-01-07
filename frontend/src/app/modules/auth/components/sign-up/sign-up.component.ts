import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { HttpClientModule } from '@angular/common/http';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../../../core/services/auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-sign-up',
  standalone: true,
  imports: [FormsModule, CommonModule, HttpClientModule, RouterModule],
  templateUrl: './sign-up.component.html',
  styleUrl: './sign-up.component.css'
})
export class SignUpComponent {
  credentials = {
    username: '',
    email: '',
    password: ''
  };

  showErrorModal = false;
  errorTitle = '';
  errorMessage = '';
  isLoading = false;

  constructor(private auth: AuthService, private router: Router) {}

  loginWithGoogle() {
    window.location.href = 'http://localhost:3050/backend/oauth/authenticate/google';
  }

  onSubmit() {
    if (!this.credentials.username || !this.credentials.email || !this.credentials.password) {
      return;
    }

    this.isLoading = true;
    this.auth.register(this.credentials).subscribe({
      next: (res: any) => {
        this.isLoading = false;
        this.errorTitle = 'Registro exitoso';
        this.errorMessage = 'Favor iniciar sesión con sus credenciales.';
        this.showErrorModal = true;
      },
      error: (err) => {
        console.error('Register failed', err);
        this.isLoading = false;
        this.errorTitle = err?.error?.error || 'Error al registrar';
        this.errorMessage = err?.error?.message || 'No se pudo crear la cuenta.';
        this.showErrorModal = true;
      }
    });
  }

  closeErrorModal() {
    this.showErrorModal = false;
    if (this.errorTitle === 'Registro exitoso') {
      this.router.navigate(['/login']);
    }
  }
}
