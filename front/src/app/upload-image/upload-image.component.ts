import { Component, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { CommonModule } from '@angular/common';
import { HttpClient, HttpEventType, HttpHeaders } from '@angular/common/http';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-upload-image',
  standalone: true,
  imports: [CommonModule, MatDialogModule, MatButtonModule],
  template: `
    <h2 mat-dialog-title>Upload Product Image</h2>
    <mat-dialog-content>
      <input type="file" (change)="onFileSelected($event)" />
      <p *ngIf="uploading">Uploading... {{ progress }}%</p>
      <p *ngIf="errorMessage" style="color:red">{{ errorMessage }}</p>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button (click)="skip()" [disabled]="uploading">Skip</button>
      <button mat-raised-button color="primary" (click)="upload()" 
              [disabled]="!selectedFile || uploading">
        Upload
      </button>
    </mat-dialog-actions>
  `,
})
export class UploadImageComponent {
  selectedFile: File | null = null;
  uploading = false;
  progress = 0;
  errorMessage = '';

  constructor(
    private dialogRef: MatDialogRef<UploadImageComponent>,
    private http: HttpClient,
    @Inject(MAT_DIALOG_DATA) public data: { productId: string; userId: string }
  ) {}

  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.selectedFile = input.files[0];
    }
  }

  upload() {
    if (!this.selectedFile) return;
  
    this.uploading = true;
    this.progress = 0;
    this.errorMessage = '';
  
    const formData = new FormData();
    formData.append('file', this.selectedFile);
    formData.append('productId', this.data.productId);
  
    // Use JWT auth
    const token = localStorage.getItem('auth-token'); // or tokenStore.getToken()
    const headers = new HttpHeaders({ Authorization: `Bearer ${token}` });
  
    this.http.post('http://localhost:8080/api/media/upload', formData, {
      headers,
      observe: 'events',
      reportProgress: true,
    }).subscribe({
      next: (event: any) => {
        if (event.type === HttpEventType.UploadProgress) {
          if (event.total) this.progress = Math.round((event.loaded / event.total) * 100);
        } else if (event.type === HttpEventType.Response) {
          this.uploading = false;
          this.dialogRef.close({ uploaded: true, response: event.body });
        }
      },
      error: (err) => {
        console.error('Upload error:', err);
        this.uploading = false;
  
        if (err.status === 0) this.errorMessage = 'Network error or CORS issue';
        else if (err.status === 403) this.errorMessage = 'Forbidden: not owner';
        else if (err.status === 400) this.errorMessage = err.error?.message || 'Invalid file';
        else this.errorMessage = 'Upload failed. Try again.';
      },
    });
  }
  

  skip() {
    this.dialogRef.close({ uploaded: false });
  }
}
