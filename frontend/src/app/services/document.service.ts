import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Document, TimestampResponse, SummaryResponse } from '../models/document.model';

@Injectable({
    providedIn: 'root'
})
export class DocumentService {
    private apiUrl = '/api/documents';

    constructor(private http: HttpClient) { }

    upload(file: File): Observable<Document> {
        const formData = new FormData();
        formData.append('file', file);
        return this.http.post<Document>(`${this.apiUrl}/upload`, formData);
    }

    getAll(): Observable<Document[]> {
        return this.http.get<Document[]>(this.apiUrl);
    }

    getById(id: number): Observable<Document> {
        return this.http.get<Document>(`${this.apiUrl}/${id}`);
    }

    delete(id: number): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/${id}`);
    }

    getSummary(id: number): Observable<SummaryResponse> {
        return this.http.get<SummaryResponse>(`${this.apiUrl}/${id}/summary`);
    }

    getTimestamps(id: number): Observable<TimestampResponse> {
        return this.http.get<TimestampResponse>(`${this.apiUrl}/${id}/timestamps`);
    }

    getStreamUrl(id: number): string {
        return `${this.apiUrl}/${id}/stream`;
    }
}
