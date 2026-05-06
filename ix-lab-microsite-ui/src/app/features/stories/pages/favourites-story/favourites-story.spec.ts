import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FavouritesStory } from './favourites-story';

describe('FavouritesStory', () => {
  let component: FavouritesStory;
  let fixture: ComponentFixture<FavouritesStory>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FavouritesStory]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FavouritesStory);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
