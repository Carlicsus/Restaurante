import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EmployeeAccountStatusComponent } from './employee-account-status.component';

describe('EmployeeAccountStatusComponent', () => {
  let component: EmployeeAccountStatusComponent;
  let fixture: ComponentFixture<EmployeeAccountStatusComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EmployeeAccountStatusComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EmployeeAccountStatusComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});