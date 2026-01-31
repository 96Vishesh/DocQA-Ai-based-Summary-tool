import { Component, Input, Output, EventEmitter, ElementRef, ViewChild, AfterViewInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DocumentService } from '../../services/document.service';

@Component({
    selector: 'app-media-player',
    standalone: true,
    imports: [CommonModule],
    templateUrl: './media-player.component.html',
    styleUrls: ['./media-player.component.css']
})
export class MediaPlayerComponent implements AfterViewInit, OnDestroy {
    @Input() documentId!: number;
    @Input() type!: 'AUDIO' | 'VIDEO';
    @Output() timeUpdate = new EventEmitter<number>();
    @ViewChild('mediaElement') mediaElement!: ElementRef<HTMLVideoElement | HTMLAudioElement>;

    streamUrl = '';

    constructor(private documentService: DocumentService) { }

    ngAfterViewInit(): void {
        this.streamUrl = this.documentService.getStreamUrl(this.documentId);

        if (this.mediaElement?.nativeElement) {
            this.mediaElement.nativeElement.addEventListener('timeupdate', this.onTimeUpdate.bind(this));
        }
    }

    ngOnDestroy(): void {
        if (this.mediaElement?.nativeElement) {
            this.mediaElement.nativeElement.removeEventListener('timeupdate', this.onTimeUpdate.bind(this));
        }
    }

    onTimeUpdate(): void {
        if (this.mediaElement?.nativeElement) {
            this.timeUpdate.emit(this.mediaElement.nativeElement.currentTime);
        }
    }

    seekTo(time: number): void {
        if (this.mediaElement?.nativeElement) {
            this.mediaElement.nativeElement.currentTime = time;
            this.mediaElement.nativeElement.play();
        }
    }
}
