import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ChatRequest, ChatResponse } from '../models/document.model';

@Injectable({
    providedIn: 'root'
})
export class ChatService {
    private apiUrl = '/api/chat';

    constructor(private http: HttpClient) { }

    send(request: ChatRequest): Observable<ChatResponse> {
        return this.http.post<ChatResponse>(this.apiUrl, request);
    }
}
