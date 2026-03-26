import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FeaturedStories } from './featured-stories';

describe('FeaturedStories', () => {
  let component: FeaturedStories;
  let fixture: ComponentFixture<FeaturedStories>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FeaturedStories]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FeaturedStories);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
