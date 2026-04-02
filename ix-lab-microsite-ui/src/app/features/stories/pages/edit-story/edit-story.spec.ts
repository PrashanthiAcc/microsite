import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EditStory } from './edit-story';

describe('EditStory', () => {
  let component: EditStory;
  let fixture: ComponentFixture<EditStory>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EditStory]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EditStory);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
