import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ChefStatisticsComponent } from './chef-statistics.component';

describe('ChefStatisticsComponent', () => {
  let component: ChefStatisticsComponent;
  let fixture: ComponentFixture<ChefStatisticsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ChefStatisticsComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ChefStatisticsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
