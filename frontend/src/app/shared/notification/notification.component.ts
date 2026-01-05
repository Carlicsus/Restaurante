import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NotificationService } from '../../core/services/notification.service';

@Component({
  selector: 'app-notification',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="notification-container">
      <div *ngFor="let notification of notificationService.notifications$ | async" 
           class="notification" 
           [class.notification-success]="notification.type === 'success'"
           [class.notification-error]="notification.type === 'error'"
           [class.notification-info]="notification.type === 'info'">
        {{ notification.message }}
      </div>
    </div>
  `,
  styles: [`
    .notification-container {
      position: fixed;
      top: 20px;
      right: 20px;
      z-index: 9999;
      display: flex;
      flex-direction: column;
      gap: 12px;
      pointer-events: none;
    }

    .notification {
      background: white;
      color: #111827;
      padding: 14px 20px;
      border-radius: 12px;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
      font-size: 14px;
      font-weight: 500;
      font-family: 'Inter', sans-serif;
      max-width: 350px;
      animation: slideIn 0.3s ease;
      pointer-events: auto;
      border-left: 4px solid #6b7280;
    }

    .notification-success {
      border-left-color: #10b981;
    }

    .notification-error {
      border-left-color: #ef4444;
    }

    .notification-info {
      border-left-color: #2563eb;
    }

    @keyframes slideIn {
      from {
        transform: translateX(400px);
        opacity: 0;
      }
      to {
        transform: translateX(0);
        opacity: 1;
      }
    }
  `]
})
export class NotificationComponent {
  constructor(public notificationService: NotificationService) {}
}
