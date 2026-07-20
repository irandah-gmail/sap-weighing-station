import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface WeightReading {
  weight: number;
  unit: string;
  stable: boolean;
}

export interface WeighingSubmitRequest {
  productionOrderId: string;
  batchId: string;
  userId: string;
  weightReadings: number[];
  unit: string;
}

export interface WeighingTransaction {
  id: number;
  productionOrderId: string;
  batchId: string;
  userId: string;
  totalWeight: number;
  unit: string;
  status: 'PENDING' | 'SENT' | 'FAILED';
  lastError?: string;
  retryCount: number;
  capturedAt: string;
  sentAt?: string;
  sapReference?: string;
}




@Injectable({ providedIn: 'root' })
export class WeighingService {

  //private baseUrl = 'http://localhost:8080'; // ← ADD THIS
  private baseUrl = 'https://sap-weighing-station-frontend-production.up.railway.app/'; // ← ADD THIS

  constructor(private http: HttpClient) {}

  getCurrentWeight(): Observable<WeightReading> {
    return this.http.get<WeightReading>(`${this.baseUrl}/api/weight`); // ← CHANGE THIS
  }

  submitTransaction(request: WeighingSubmitRequest): Observable<WeighingTransaction> {
    return this.http.post<WeighingTransaction>(`${this.baseUrl}/api/transactions`, request); // ← CHANGE THIS
  }

  getRecentTransactions(): Observable<WeighingTransaction[]> {
    return this.http.get<WeighingTransaction[]>(`${this.baseUrl}/api/transactions/recent`); // ← CHANGE THIS
  }

  /** Clear FAILED status on a transaction so it can be retried */
  clearFailedTransaction(id: number): Observable<WeighingTransaction> {
    return this.http.post<WeighingTransaction>(`${this.baseUrl}/api/transactions/${id}/clear-failed`, {});
  }

  /** Resend a failed transaction immediately */
  resendFailedTransaction(id: number): Observable<WeighingTransaction> {
    return this.http.post<WeighingTransaction>(`${this.baseUrl}/api/transactions/${id}/resend`, {});
  }

  /** Delete a failed transaction from the backend */
  deleteFailedTransaction(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/api/transactions/${id}`);
  }
}
