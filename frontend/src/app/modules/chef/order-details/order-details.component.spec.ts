import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ChefOrderDetailsComponent } from './order-details.component';

describe('OrderDetailsComponent', () => {
  let component: ChefOrderDetailsComponent;
  let fixture: ComponentFixture<ChefOrderDetailsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ChefOrderDetailsComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ChefOrderDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});