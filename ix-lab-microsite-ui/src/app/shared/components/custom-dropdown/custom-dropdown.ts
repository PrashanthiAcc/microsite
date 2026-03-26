import { Component, Input, Output, EventEmitter, forwardRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NG_VALUE_ACCESSOR, ControlValueAccessor } from '@angular/forms';

@Component({
  selector: 'app-custom-dropdown',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './custom-dropdown.html',
  styleUrls: ['./custom-dropdown.scss'],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => CustomDropdownComponent),
      multi: true
    }
  ]
})
export class CustomDropdownComponent implements ControlValueAccessor {
  @Input() label: string = '';
  @Input() options: any[] = [];
  @Input() displayKey: string = '';

  @Output() optionSelected = new EventEmitter<any>();

  isOpen = false;
  selected: any = null;

  // ControlValueAccessor hooks
  onChange = (_: any) => {};
  onTouched = () => {};

  // writeValue(value: any): void {
  //   this.selected = value;
  // }

  writeValue(value: any): void {
  if (!value) {
    this.selected = '';
    return;
  }

  // Find the matching option by ID
  const option = this.options.find(opt =>
    opt.industryId === value ||
    opt.subIndustryId === value ||
    opt.valueChainId === value
  );

  // Show the display name if found
  this.selected = option
    ? (this.displayKey ? option[this.displayKey] : option)
    : value; // fallback
}

  registerOnChange(fn: any): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: any): void {
    this.onTouched = fn;
  }

  setDisabledState?(isDisabled: boolean): void {
    // optional: disable UI
  }

  toggle() {
    this.isOpen = !this.isOpen;
  }

  // select(option: any) {
  //   this.selected = this.displayKey ? option[this.displayKey] : option;
  //   this.onChange(option);              // notify Angular forms
  //   this.optionSelected.emit(option);   // notify parent
  //   this.isOpen = false;
  // }

  select(option: any) {
  // Show name in dropdown
  this.selected = this.displayKey ? option[this.displayKey] : option;

  // Pass only the ID back to Angular forms
  const id =
    option?.industryId ??
    option?.subIndustryId ??
    option?.valueChainId ??
    option;

  this.onChange(id);              // formControl gets the ID
  this.optionSelected.emit(option); // parent still gets full object
  this.isOpen = false;
}

  selectAll() {
    this.selected = 'All';
    this.onChange(null);
    this.optionSelected.emit(null);
    this.isOpen = false;
  }
}
