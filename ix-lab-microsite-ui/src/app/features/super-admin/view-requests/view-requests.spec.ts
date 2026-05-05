import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ViewRequests } from './view-requests';

describe('ViewRequests', () => {
  let component: ViewRequests;
  let fixture: ComponentFixture<ViewRequests>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ViewRequests]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ViewRequests);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
