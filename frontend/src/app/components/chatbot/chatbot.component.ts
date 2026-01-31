import { Component, Input, Output, EventEmitter, ElementRef, ViewChild, AfterViewChecked } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ChatService } from '../../services/chat.service';
import { ChatMessage, TimestampReference } from '../../models/document.model';

@Component({
    selector: 'app-chatbot',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './chatbot.component.html',
    styleUrls: ['./chatbot.component.css']
})
export class ChatbotComponent implements AfterViewChecked {
    @Input() documentId!: number;
    @Output() timestampClick = new EventEmitter<number>();
    @ViewChild('messagesContainer') messagesContainer!: ElementRef;

    messages: ChatMessage[] = [];
    input = '';
    loading = false;
    sessionId?: string;

    constructor(private chatService: ChatService) { }

    ngAfterViewChecked(): void {
        this.scrollToBottom();
    }

    scrollToBottom(): void {
        if (this.messagesContainer) {
            const el = this.messagesContainer.nativeElement;
            el.scrollTop = el.scrollHeight;
        }
    }

    sendMessage(): void {
        if (!this.input.trim() || this.loading) return;

        const userMessage: ChatMessage = {
            role: 'user',
            content: this.input.trim()
        };

        this.messages.push(userMessage);
        const messageText = this.input;
        this.input = '';
        this.loading = true;

        this.chatService.send({
            documentId: this.documentId,
            message: messageText,
            sessionId: this.sessionId
        }).subscribe({
            next: (response) => {
                this.sessionId = response.sessionId;
                this.messages.push({
                    role: 'assistant',
                    content: response.response,
                    timestamps: response.timestamps
                });
                this.loading = false;
            },
            error: (err) => {
                console.error('Chat error:', err);
                this.messages.push({
                    role: 'assistant',
                    content: 'Sorry, I encountered an error. Please try again.'
                });
                this.loading = false;
            }
        });
    }

    onTimestampClick(time: number): void {
        this.timestampClick.emit(time);
    }

    formatTime(seconds: number): string {
        const mins = Math.floor(seconds / 60);
        const secs = Math.floor(seconds % 60);
        return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
    }

    onKeyDown(event: KeyboardEvent): void {
        if (event.key === 'Enter' && !event.shiftKey) {
            event.preventDefault();
            this.sendMessage();
        }
    }
}
