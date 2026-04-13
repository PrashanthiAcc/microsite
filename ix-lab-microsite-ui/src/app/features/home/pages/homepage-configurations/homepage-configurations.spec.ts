import { ComponentFixture, TestBed } from '@angular/core/testing';

import { HomepageConfigurations } from './homepage-configurations';

describe('HomepageConfigurations', () => {
  let component: HomepageConfigurations;
  let fixture: ComponentFixture<HomepageConfigurations>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HomepageConfigurations]
    })
    .compileComponents();

    fixture = TestBed.createComponent(HomepageConfigurations);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
