import { Component } from '@angular/core';
import { WeighingFormComponent } from './components/weighing-form/weighing-form.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [WeighingFormComponent],
  templateUrl: './app.component.html'
})
export class AppComponent {
  title = 'Weighing Station';
}
