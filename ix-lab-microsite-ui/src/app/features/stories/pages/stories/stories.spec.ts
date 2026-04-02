import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Stories } from './stories';

describe('Stories', () => {
  let component: Stories;
  let fixture: ComponentFixture<Stories>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Stories]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Stories);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('toggles menu when dot button is clicked and closes others', () => {
    fixture.detectChanges();
    const cards = fixture.nativeElement.querySelectorAll('.story-card');
    expect(cards.length).toBeGreaterThan(1);

    const firstBtn = cards[0].querySelector('.card-menu-btn');
    const secondBtn = cards[1].querySelector('.card-menu-btn');

    // initially no menu visible
    expect(component.stories[0].showMenu).toBeFalsy();
    expect(component.stories[1].showMenu).toBeFalsy();

    firstBtn.click();
    fixture.detectChanges();
    expect(component.stories[0].showMenu).toBeTrue();
    expect(component.stories[1].showMenu).toBeFalsy();

    // clicking second should close first
    secondBtn.click();
    fixture.detectChanges();
    expect(component.stories[0].showMenu).toBeFalsy();
    expect(component.stories[1].showMenu).toBeTrue();
  });
});
