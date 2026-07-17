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

  constructor(private http: HttpClient) {}

  getCurrentWeight(): Observable<WeightReading> {
    return this.http.get<WeightReading>('/api/weight');
  }

  submitTransaction(request: WeighingSubmitRequest): Observable<WeighingTransaction> {
    return this.http.post<WeighingTransaction>('/api/transactions', request);
  }

  getRecentTransactions(): Observable<WeighingTransaction[]> {
    return this.http.get<WeighingTransaction[]>('/api/transactions/recent');
  }
}
