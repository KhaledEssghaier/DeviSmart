import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Devis } from '../models/devis.model';
import { Facture } from '../models/facture.model';

@Injectable({
  providedIn: 'root'
})
export class DevisService {
  private apiUrl = 'http://localhost:8080/api/devis';

  constructor(private http: HttpClient) {}

  getDevis(): Observable<Devis[]> {
    return this.http.get<Devis[]>(this.apiUrl);
  }

  getDevisById(id: number): Observable<Devis> {
    return this.http.get<Devis>(`${this.apiUrl}/${id}`);
  }

  getDevisByClient(clientId: number): Observable<Devis[]> {
    return this.http.get<Devis[]>(`${this.apiUrl}/client/${clientId}`);
  }

  getDevisByStatut(statut: string): Observable<Devis[]> {
    return this.http.get<Devis[]>(`${this.apiUrl}/statut/${statut}`);
  }

  addDevis(devis: Devis): Observable<Devis> {
    return this.http.post<Devis>(this.apiUrl, devis);
  }

  updateDevis(id: number, devis: Devis): Observable<Devis> {
    return this.http.put<Devis>(`${this.apiUrl}/${id}`, devis);
  }

  deleteDevis(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  validerDevis(id: number): Observable<Facture> {
    return this.http.post<Facture>(`${this.apiUrl}/${id}/valider`, {});
  }

  refuserDevis(id: number): Observable<Devis> {
    return this.http.post<Devis>(`${this.apiUrl}/${id}/refuser`, {});
  }

  getTotaux(id: number): Observable<{ totalHT: number; totalTTC: number; tva: number }> {
    return this.http.get<{ totalHT: number; totalTTC: number; tva: number }>(`${this.apiUrl}/${id}/totaux`);
  }

  // ==================== PDF ====================

  /**
   * Télécharge le PDF d'un devis (retourne Observable pour gestion d'erreur)
   * @param id ID du devis
   * @returns Observable<Blob> du fichier PDF
   */
  telechargerPdf(id: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/${id}/pdf`, { responseType: 'blob' });
  }

  /**
   * Télécharge automatiquement le PDF (méthode utilitaire)
   * @param id ID du devis
   * @param numeroDevis Numéro de devis pour le nom du fichier
   */
  downloadPdf(id: number, numeroDevis?: string): void {
    this.telechargerPdf(id).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = numeroDevis 
          ? `devis-${numeroDevis.replace(/\//g, '-')}.pdf`
          : `devis-${id}.pdf`;
        link.click();
        window.URL.revokeObjectURL(url);
      },
      error: (err) => console.error('Erreur lors du téléchargement du PDF', err)
    });
  }

  /**
   * Retourne l'URL du PDF pour aperçu
   */
  getPdfUrl(id: number): string {
    return `${this.apiUrl}/${id}/pdf`;
  }
}
