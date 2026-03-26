import { TestBed } from '@angular/core/testing';

import { Usecase } from './usecase';

describe('Usecase', () => {
  let service: Usecase;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(Usecase);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
