
import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { CommonModule } from '@angular/common';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { RenderingServiceController } from '../controller/rendering.service';
import { MatProgressBar } from '@angular/material/progress-bar';

@Component({
  selector: 'app-rendering-pdf-detail',
  standalone: true,
  imports: [CommonModule, MatProgressBar],
  templateUrl: './rendering-pdf-detail.component.html',
})
export class RenderingPdfDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly renderingService = inject(RenderingServiceController);
  private readonly sanitizer = inject(DomSanitizer);
  error = signal<string | null>(null);
  loading = signal<boolean>(false);
  pdfUrl: SafeResourceUrl | null = null;

  ngOnInit(): void {
    this.loading.set(true);
    const idParam = this.route.snapshot.paramMap.get('id');
    if (!idParam) {
      this.error.set('Chybí ID PDF v URL');
      this.loading.set(false);
      return;
    }
    const pdfId = Number(idParam);
    if (!pdfId) {
      this.error.set('Neplatné ID PDF');
      this.loading.set(false);
      return;
    }
    this.loadPdf(pdfId);
  }

  async loadPdf(id: number) {
    try {
      const pdfBlob = await this.renderingService.getPdfById(id).toPromise();

      if (pdfBlob && pdfBlob instanceof Blob) {
        console.log('Načteno PDF blob, velikost:', pdfBlob.size, 'bytes');
        const blobUrl = URL.createObjectURL(pdfBlob);
        this.pdfUrl = this.sanitizer.bypassSecurityTrustResourceUrl(blobUrl);
      } else {
        this.error.set('PDF nenalezeno nebo neplatný formát');
      }
    } catch (e: any) {
      console.error('Chyba při načítání PDF:', e);
      this.error.set('Chyba při načítání PDF');
    }
    this.loading.set(false);
  }
}
