import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RateDishComponent } from './rate-dish.component';

describe('RateDishComponent', () => {
  let component: RateDishComponent;
  let fixture: ComponentFixture<RateDishComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RateDishComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RateDishComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});