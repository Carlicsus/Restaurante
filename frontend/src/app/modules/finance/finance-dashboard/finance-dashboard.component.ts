import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import {NavbarFinanceComponent} from "../../../shared/navbar-finance/navbar-finance.component";
@Component({
  selector: 'app-finance-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    NavbarFinanceComponent
  ],
  templateUrl: './finance-dashboard.component.html',
  styleUrls: ['./finance-dashboard.component.css']
})
export class FinanceDashboardComponent {

  stats = [
    {
      title: 'Órdenes Totales',
      value: '1,234',
      change: '+10%',
      positive: true
    },
    {
      title: 'Monto Total Adeudado',
      value: '$5,678.90',
      change: '-5%',
      positive: false
    },
    {
      title: 'Empleados Deudores',
      value: '5',
      change: '+2%',
      positive: true
    }
  ];

}
