import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AddStory } from './add-story';

describe('AddStory', () => {
  let component: AddStory;
  let fixture: ComponentFixture<AddStory>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AddStory]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AddStory);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
