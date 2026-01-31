import { Component, EventEmitter, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DocumentService } from '../../services/document.service';

@Component({
    selector: 'app-file-upload',
    standalone: true,
    imports: [CommonModule],
    templateUrl: './file-upload.component.html',
    styleUrls: ['./file-upload.component.css']
})
export class FileUploadComponent {
    @Output() uploadComplete = new EventEmitter<void>();

    isDragOver = false;
    file: File | null = null;
    progress = 0;
    status: 'idle' | 'uploading' | 'success' | 'error' = 'idle';
    message = '';

    constructor(private documentService: DocumentService) { }

    onDragOver(event: DragEvent): void {
        event.preventDefault();
        event.stopPropagation();
        this.isDragOver = true;
    }

    onDragLeave(event: DragEvent): void {
        event.preventDefault();
        event.stopPropagation();
        this.isDragOver = false;
    }

    onDrop(event: DragEvent): void {
        event.preventDefault();
        event.stopPropagation();
        this.isDragOver = false;

        const files = event.dataTransfer?.files;
        if (files && files.length > 0) {
            this.handleFile(files[0]);
        }
    }

    onFileSelect(event: Event): void {
        const input = event.target as HTMLInputElement;
        if (input.files && input.files.length > 0) {
            this.handleFile(input.files[0]);
        }
    }

    handleFile(file: File): void {
        if (!this.isValidFile(file)) {
            this.status = 'error';
            this.message = 'Unsupported file type. Please upload PDF, audio, or video files.';
            return;
        }

        this.file = file;
        this.status = 'uploading';
        this.message = 'Uploading...';

        this.documentService.upload(file).subscribe({
            next: () => {
                this.status = 'success';
                this.message = 'Upload complete! Processing document...';
                this.uploadComplete.emit();

                setTimeout(() => this.reset(), 3000);
            },
            error: (err) => {
                console.error('Upload failed:', err);
                this.status = 'error';
                this.message = 'Upload failed. Please try again.';
            }
        });
    }

    isValidFile(file: File): boolean {
        const validTypes = [
            'application/pdf',
            'audio/mpeg', 'audio/wav', 'audio/mp4', 'audio/ogg',
            'video/mp4', 'video/webm', 'video/quicktime'
        ];
        return validTypes.includes(file.type) ||
            file.type.startsWith('audio/') ||
            file.type.startsWith('video/');
    }

    getFileIcon(): string {
        if (!this.file) return '📄';
        if (this.file.type === 'application/pdf') return '📄';
        if (this.file.type.startsWith('audio/')) return '🎵';
        if (this.file.type.startsWith('video/')) return '🎬';
        return '📄';
    }

    reset(): void {
        this.file = null;
        this.progress = 0;
        this.status = 'idle';
        this.message = '';
    }
}
