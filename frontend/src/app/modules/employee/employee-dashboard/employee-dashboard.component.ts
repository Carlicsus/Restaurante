import { Component, OnInit } from '@angular/core';
import { NavbarEmployeeComponent } from '../../../shared/navbar-employee/navbar-employee.component';
import { RouterModule } from '@angular/router';


@Component({
  selector: 'app-employee-dashboard',
  standalone: true,
  imports: [NavbarEmployeeComponent, RouterModule],
  templateUrl: './employee-dashboard.component.html',
  styleUrls: ['./employee-dashboard.component.css']
})
export class EmployeeDashboardComponent implements OnInit {


  ngOnInit(): void {
    
  }

}
