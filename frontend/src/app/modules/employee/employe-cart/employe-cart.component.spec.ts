import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EmployeCartComponent } from './employe-cart.component';

describe('EmployeCartComponent', () => {
  let component: EmployeCartComponent;
  let fixture: ComponentFixture<EmployeCartComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EmployeCartComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EmployeCartComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});