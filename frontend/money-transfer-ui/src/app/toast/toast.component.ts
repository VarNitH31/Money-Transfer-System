import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-toast',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './toast.component.html',
  styleUrls: ['./toast.component.scss']
})
export class ToastComponent {

  @Input() message = '';
  @Input() type: 'success' | 'error' = 'success';

  visible = false;

  show(msg: string, type: 'success' | 'error') {
    this.message = msg;
    this.type = type;
    this.visible = true;

    setTimeout(() => {
      this.visible = false;
    }, 3000);
  }
}