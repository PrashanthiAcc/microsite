import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ArchivedStory } from './archived-story';

describe('ArchivedStory', () => {
  let component: ArchivedStory;
  let fixture: ComponentFixture<ArchivedStory>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ArchivedStory]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ArchivedStory);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
