import { ComponentFixture, TestBed } from '@angular/core/testing';

import { KeyCapabilities } from './key-capabilities';

describe('KeyCapabilities', () => {
  let component: KeyCapabilities;
  let fixture: ComponentFixture<KeyCapabilities>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [KeyCapabilities]
    })
    .compileComponents();

    fixture = TestBed.createComponent(KeyCapabilities);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
