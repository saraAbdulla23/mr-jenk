import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ManageTravel } from './manage-travel';

describe('ManageTravel', () => {
  let component: ManageTravel;
  let fixture: ComponentFixture<ManageTravel>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ManageTravel]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ManageTravel);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
