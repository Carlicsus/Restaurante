import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NavbarChefComponent } from './navbar-chef.component';

describe('NavbarChefComponent', () => {
  let component: NavbarChefComponent;
  let fixture: ComponentFixture<NavbarChefComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NavbarChefComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(NavbarChefComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
