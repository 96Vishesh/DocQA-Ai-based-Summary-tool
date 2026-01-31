import { Component, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { Document, TimestampEntry } from '../../models/document.model';
import { DocumentService } from '../../services/document.service';
import { ChatbotComponent } from '../../components/chatbot/chatbot.component';
import { MediaPlayerComponent } from '../../components/media-player/media-player.component';

@Component({
    selector: 'app-document-view',
    standalone: true,
    imports: [CommonModule, RouterLink, ChatbotComponent, MediaPlayerComponent],
    templateUrl: './document-view.component.html',
    styleUrls: ['./document-view.component.css']
})
export class DocumentViewComponent implements OnInit {
    @ViewChild(MediaPlayerComponent) mediaPlayer!: MediaPlayerComponent;

    document: Document | null = null;
    timestamps: TimestampEntry[] = [];
    loading = true;
    currentTime = 0;

    constructor(
        private route: ActivatedRoute,
        private documentService: DocumentService
    ) { }

    ngOnInit(): void {
        const id = Number(this.route.snapshot.paramMap.get('id'));
        if (id) {
            this.loadDocument(id);
        }
    }

    loadDocument(id: number): void {
        forkJoin({
            document: this.documentService.getById(id),
            timestamps: this.documentService.getTimestamps(id)
        }).subscribe({
            next: (result) => {
                this.document = result.document;
                this.timestamps = result.timestamps.timestamps || [];
                this.loading = false;
            },
            error: (err) => {
                console.error('Failed to load document:', err);
                this.loading = false;
            }
        });
    }

    onTimeUpdate(time: number): void {
        this.currentTime = time;
    }

    seekTo(time: number): void {
        if (this.mediaPlayer) {
            this.mediaPlayer.seekTo(time);
        }
    }

    getTypeIcon(): string {
        switch (this.document?.type) {
            case 'PDF': return '📄';
            case 'AUDIO': return '🎵';
            case 'VIDEO': return '🎬';
            default: return '📄';
        }
    }

    isMedia(): boolean {
        return this.document?.type === 'AUDIO' || this.document?.type === 'VIDEO';
    }

    isActiveTimestamp(ts: TimestampEntry): boolean {
        return this.currentTime >= ts.startTime && this.currentTime < ts.endTime;
    }
}
