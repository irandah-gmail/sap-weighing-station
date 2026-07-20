import { Component, OnDestroy, OnInit } from '@angular/core';
import { interval, Subscription } from 'rxjs';
import { WeighingService, WeighingTransaction, WeightReading } from '../../services/weighing.service';

@Component({
  selector: 'app-weighing-form',
  templateUrl: './weighing-form.component.html',
  styleUrls: ['./weighing-form.component.css']
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

  // Track which transactions are currently being resent
  resendingIds: Set<number> = new Set<number>();

  private pollSub?: Subscription;
  private listSub?: Subscription;

  constructor(private weighingService: WeighingService) {}

  clearFailed(tx: WeighingTransaction): void {
    // kept for backward compatibility (calls clearFailed then refreshes)
    this.weighingService.clearFailedTransaction(tx.id).subscribe({
      next: (updated) => this.refreshRecent(),
      error: () => { /* ignore transient errors; could show toast */ }
    });
  }

  resendFailed(tx: WeighingTransaction): void {
    // mark as resending in UI and disable the button
    this.resendingIds.add(tx.id);
    // Optimistically show resending in the UI by leaving tx.status unchanged but UI will read resendingIds
    this.weighingService.resendFailedTransaction(tx.id).subscribe({
      next: (updated) => {
        // update local transaction with returned state
        const idx = this.recentTransactions.findIndex(t => t.id === tx.id);
        if (idx !== -1 && updated) {
          this.recentTransactions[idx] = updated;
        }
        // remove resending flag (will re-enable buttons if FAILED)
        this.resendingIds.delete(tx.id);
      },
      error: () => {
        // on error, mark as failed and re-enable button
        const idx = this.recentTransactions.findIndex(t => t.id === tx.id);
        if (idx !== -1) {
          this.recentTransactions[idx].status = 'FAILED';
        }
        this.resendingIds.delete(tx.id);
      }
    });
  }

  deleteFailed(tx: WeighingTransaction): void {
    if (!confirm('Delete this FAILED transaction from the database? This cannot be undone.')) return;
    this.weighingService.deleteFailedTransaction(tx.id).subscribe({
      next: () => this.refreshRecent(),
      error: () => { alert('Could not delete transaction. It must be in FAILED state.'); }
    });
  }

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
