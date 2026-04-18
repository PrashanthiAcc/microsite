import { Directive, HostListener, ElementRef } from '@angular/core';
import { NgControl } from '@angular/forms';

@Directive({
  selector: '[numericPlus]'
})
export class NumericPlusDirective {
  constructor(private el: ElementRef, private control: NgControl) {}

  @HostListener('input', ['$event'])
  onInput(event: any) {
    let val = event.target.value || '';

    // Keep only digits
    val = val.replace(/\D/g, '');

    if (val === '') {
      this.control.control?.setValue('', { emitEvent: false });
      return;
    }

    const newVal = val + '+';
    this.control.control?.setValue(newVal, { emitEvent: false });

    // ✅ Move cursor just before '+'
    const pos = newVal.length - 1;
    setTimeout(() => {
      this.el.nativeElement.setSelectionRange(pos, pos);
    });
  }
}
