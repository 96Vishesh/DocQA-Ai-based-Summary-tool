import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { Subject, interval, takeUntil } from 'rxjs';
import { Document } from '../../models/document.model';
import { DocumentService } from '../../services/document.service';
import { FileUploadComponent } from '../../components/file-upload/file-upload.component';

@Component({
    selector: 'app-dashboard',
    standalone: true,
    imports: [CommonModule, RouterLink, FileUploadComponent],
    templateUrl: './dashboard.component.html',
    styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit, OnDestroy {
    documents: Document[] = [];
    loading = true;
    private destroy$ = new Subject<void>();

    constructor(private documentService: DocumentService) { }

    ngOnInit(): void {
        this.fetchDocuments();

        // Poll for updates every 5 seconds
        interval(5000)
            .pipe(takeUntil(this.destroy$))
            .subscribe(() => this.fetchDocuments());
    }

    ngOnDestroy(): void {
        this.destroy$.next();
        this.destroy$.complete();
    }

    fetchDocuments(): void {
        this.documentService.getAll().subscribe({
            next: (docs) => {
                this.documents = docs;
                this.loading = false;
            },
            error: (err) => {
                console.error('Failed to fetch documents:', err);
                this.loading = false;
            }
        });
    }

    onUploadComplete(): void {
        this.fetchDocuments();
    }

    deleteDocument(id: number, event: Event): void {
        event.preventDefault();
        event.stopPropagation();

        if (confirm('Are you sure you want to delete this document?')) {
            this.documentService.delete(id).subscribe({
                next: () => {
                    this.documents = this.documents.filter(d => d.id !== id);
                },
                error: (err) => console.error('Failed to delete document:', err)
            });
        }
    }

    getTypeIcon(type: string): string {
        switch (type) {
            case 'PDF': return '📄';
            case 'AUDIO': return '🎵';
            case 'VIDEO': return '🎬';
            default: return '📄';
        }
    }

    getStatusClass(status: string): string {
        switch (status) {
            case 'COMPLETED': return 'badge-success';
            case 'PROCESSING': return 'badge-warning';
            case 'FAILED': return 'badge-error';
            default: return 'badge-info';
        }
    }

    formatFileSize(bytes: number): string {
        if (bytes < 1024) return bytes + ' B';
        if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
        return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
    }

    formatDate(dateStr: string): string {
        return new Date(dateStr).toLocaleDateString('en-US', {
            month: 'short',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    }
}
