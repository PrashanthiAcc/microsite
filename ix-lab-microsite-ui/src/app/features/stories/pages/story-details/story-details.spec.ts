import { ComponentFixture, TestBed } from '@angular/core/testing';

import { StoryDetails } from './story-details';

describe('StoryDetails', () => {
  let component: StoryDetails;
  let fixture: ComponentFixture<StoryDetails>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [StoryDetails]
    })
    .compileComponents();

    fixture = TestBed.createComponent(StoryDetails);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
