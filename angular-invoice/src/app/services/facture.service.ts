import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Facture } from '../models/facture.model';

@Injectable({
  providedIn: 'root'
})
export class FactureService {
  private apiUrl = 'http://localhost:8080/api/factures';

  constructor(private http: HttpClient) {}

  getFactures(): Observable<Facture[]> {
    return this.http.get<Facture[]>(this.apiUrl);
  }

  getFacture(id: number): Observable<Facture> {
    return this.http.get<Facture>(`${this.apiUrl}/${id}`);
  }

  getFacturesByClient(clientId: number): Observable<Facture[]> {
    return this.http.get<Facture[]>(`${this.apiUrl}/client/${clientId}`);
  }

  getFacturesByStatut(statut: string): Observable<Facture[]> {
    return this.http.get<Facture[]>(`${this.apiUrl}/statut/${statut}`);
  }

  addFacture(facture: Facture): Observable<Facture> {
    return this.http.post<Facture>(this.apiUrl, facture);
  }

  /**
   * Crée une facture manuelle sans client existant
   */
  creerFactureManuelle(data: {
    nomClient: string;
    adresseClient?: string;
    telephoneClient?: string;
    emailClient?: string;
    matriculeFiscalClient?: string;
    tauxTVA?: number;
    conditionsPaiement?: string;
    notes?: string;
    lignes: { designation: string; quantite: number; prixUnitaireHT: number }[];
  }): Observable<Facture> {
    return this.http.post<Facture>(`${this.apiUrl}/creer-manuelle`, data);
  }

  updateFacture(id: number, facture: Facture): Observable<Facture> {
    return this.http.put<Facture>(`${this.apiUrl}/${id}`, facture);
  }

  deleteFacture(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  marquerPayee(id: number): Observable<Facture> {
    return this.http.post<Facture>(`${this.apiUrl}/${id}/payer`, {});
  }

  marquerNonPayee(id: number): Observable<Facture> {
    return this.http.post<Facture>(`${this.apiUrl}/${id}/impayer`, {});
  }

  getTotaux(id: number): Observable<{ totalHT: number; totalTTC: number; tva: number }> {
    return this.http.get<{ totalHT: number; totalTTC: number; tva: number }>(`${this.apiUrl}/${id}/totaux`);
  }

  // ==================== PDF ====================

  /**
   * Télécharge le PDF d'une facture (retourne Observable pour gestion d'erreur)
   * @param id ID de la facture
   * @returns Observable<Blob> du fichier PDF
   */
  telechargerPdf(id: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/${id}/pdf`, { responseType: 'blob' });
  }

  /**
   * Télécharge automatiquement le PDF (méthode utilitaire)
   * @param id ID de la facture
   * @param numeroFacture Numéro de facture pour le nom du fichier
   */
  downloadPdf(id: number, numeroFacture?: string): void {
    this.telechargerPdf(id).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = numeroFacture 
          ? `facture-${numeroFacture.replace(/\//g, '-')}.pdf`
          : `facture-${id}.pdf`;
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

  // ==================== STATISTIQUES ====================

  /**
   * Récupère les statistiques des factures
   */
  getStatistiques(): Observable<{ chiffreAffaires: number; montantEnAttente: number; montantEnRetard: number }> {
    return this.http.get<{ chiffreAffaires: number; montantEnAttente: number; montantEnRetard: number }>(`${this.apiUrl}/stats`);
  }
}
