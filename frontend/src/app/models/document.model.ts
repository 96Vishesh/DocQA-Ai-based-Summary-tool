export interface Document {
    id: number;
    fileName: string;
    originalFileName: string;
    type: 'PDF' | 'AUDIO' | 'VIDEO';
    mimeType: string;
    fileSize: number;
    summary: string | null;
    uploadedAt: string;
    processedAt: string | null;
    status: 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED';
}

export interface ChatMessage {
    role: 'user' | 'assistant';
    content: string;
    timestamps?: TimestampReference[];
}

export interface TimestampReference {
    startTime: number;
    endTime: number;
    content: string;
    formattedTime: string;
}

export interface ChatRequest {
    documentId: number;
    message: string;
    sessionId?: string;
}

export interface ChatResponse {
    response: string;
    sessionId: string;
    timestamps: TimestampReference[];
}

export interface TimestampEntry {
    startTime: number;
    endTime: number;
    formattedStartTime: string;
    formattedEndTime: string;
    topic: string;
    content: string;
}

export interface TimestampResponse {
    documentId: number;
    timestamps: TimestampEntry[];
}

export interface SummaryResponse {
    documentId: number;
    summary: string;
}
