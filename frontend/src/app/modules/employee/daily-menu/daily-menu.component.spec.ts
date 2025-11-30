import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DailyMenuComponent } from './daily-menu.component';

describe('DailyMenuComponent', () => {
  let component: DailyMenuComponent;
  let fixture: ComponentFixture<DailyMenuComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DailyMenuComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DailyMenuComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
