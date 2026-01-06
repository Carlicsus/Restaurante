import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export interface Notification {
  id: number;
  message: string;
  type: 'success' | 'error' | 'info';
}

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private notifications = new BehaviorSubject<Notification[]>([]);
  public notifications$ = this.notifications.asObservable();
  private nextId = 0;

  show(message: string, type: 'success' | 'error' | 'info' = 'info', duration = 3000) {
    const notification: Notification = {
      id: this.nextId++,
      message,
      type
    };

    const current = this.notifications.value;
    this.notifications.next([...current, notification]);

    setTimeout(() => {
      this.remove(notification.id);
    }, duration);
  }

  success(message: string, duration = 3000) {
    this.show(message, 'success', duration);
  }

  error(message: string, duration = 3000) {
    this.show(message, 'error', duration);
  }

  info(message: string, duration = 3000) {
    this.show(message, 'info', duration);
  }

  private remove(id: number) {
    const current = this.notifications.value;
    this.notifications.next(current.filter(n => n.id !== id));
  }
}
