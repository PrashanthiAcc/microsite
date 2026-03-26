import { TestBed } from '@angular/core/testing';

import { Industry } from './industry';

describe('Industry', () => {
  let service: Industry;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(Industry);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
