import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { WeighingFormComponent } from './components/weighing-form/weighing-form.component';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  standalone: true,
  imports: [CommonModule, WeighingFormComponent]
})
export class AppComponent {
  title = 'Weighing Station';
}

