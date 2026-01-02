import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AuthSuccesComponent } from './auth-succes.component';

describe('AuthSuccesComponent', () => {
  let component: AuthSuccesComponent;
  let fixture: ComponentFixture<AuthSuccesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AuthSuccesComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AuthSuccesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
