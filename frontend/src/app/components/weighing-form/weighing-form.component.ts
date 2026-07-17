import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { interval, Subscription } from 'rxjs';
import { WeighingService, WeighingTransaction, WeightReading } from '../../services/weighing.service';

@Component({
  selector: 'app-weighing-form',
  templateUrl: './weighing-form.component.html',
  styleUrls: ['./weighing-form.component.css'],
  standalone: true,
  imports: [CommonModule, FormsModule]
})
export class WeighingFormComponent implements OnInit, OnDestroy {

  productionOrderId = '';
  batchId = '';
  userId = '';

  currentWeight: WeightReading | null = null;
  capturedReadings: number[] = [];

  recentTransactions: WeighingTransaction[] = [];

  submitting = false;
  statusMessage = '';
  statusIsError = false;

  private pollSub?: Subscription;
  private listSub?: Subscription;

  constructor(private weighingService: WeighingService) {}

  ngOnInit(): void {
    // Poll the live scale reading every second
    this.pollSub = interval(1000).subscribe(() => {
      this.weighingService.getCurrentWeight().subscribe({
        next: (reading) => this.currentWeight = reading,
        error: () => { /* scale temporarily unreachable - keep last known reading on screen */ }
      });
    });

    this.refreshRecent();
    // Refresh the "pending sync" list periodically
    this.listSub = interval(15000).subscribe(() => this.refreshRecent());
  }

  ngOnDestroy(): void {
    this.pollSub?.unsubscribe();
    this.listSub?.unsubscribe();
  }

  captureReading(): void {
    if (this.currentWeight) {
      this.capturedReadings.push(this.currentWeight.weight);
    }
  }

  removeReading(index: number): void {
    this.capturedReadings.splice(index, 1);
  }

  get totalWeight(): number {
    return Math.round(this.capturedReadings.reduce((a, b) => a + b, 0) * 100) / 100;
  }

  get canSubmit(): boolean {
    return !!this.productionOrderId && !!this.batchId && !!this.userId
      && this.capturedReadings.length > 0 && !this.submitting;
  }

  submit(): void {
    if (!this.canSubmit) return;

    this.submitting = true;
    this.statusMessage = '';

    this.weighingService.submitTransaction({
      productionOrderId: this.productionOrderId,
      batchId: this.batchId,
      userId: this.userId,
      weightReadings: this.capturedReadings,
      unit: this.currentWeight?.unit || 'KG'
    }).subscribe({
      next: (tx) => {
        this.submitting = false;
        if (tx.status === 'SENT') {
          this.statusIsError = false;
          this.statusMessage = `Sent to SAP successfully (ref: ${tx.sapReference}).`;
        } else {
          this.statusIsError = true;
          this.statusMessage = `Saved locally, but SAP post failed — it will retry automatically. (${tx.lastError ?? ''})`;
        }
        this.resetForm();
        this.refreshRecent();
      },
      error: (err) => {
        this.submitting = false;
        this.statusIsError = true;
        this.statusMessage = 'Could not save the transaction: ' + (err?.error?.message || err.message);
      }
    });
  }

  private resetForm(): void {
    this.productionOrderId = '';
    this.batchId = '';
    this.userId = '';
    this.capturedReadings = [];
  }

  private refreshRecent(): void {
    this.weighingService.getRecentTransactions().subscribe({
      next: (list) => this.recentTransactions = list,
      error: () => { /* ignore, transient */ }
    });
  }

  get pendingCount(): number {
    return this.recentTransactions.filter(t => t.status !== 'SENT').length;
  }
}

