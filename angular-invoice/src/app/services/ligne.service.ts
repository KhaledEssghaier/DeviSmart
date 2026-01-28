import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Ligne } from '../models/ligne.model';

@Injectable({
  providedIn: 'root'
})
export class LigneService {
  private apiUrl = 'http://localhost:8080/api/lignes';

  constructor(private http: HttpClient) {}

  getLignes(): Observable<Ligne[]> {
    return this.http.get<Ligne[]>(this.apiUrl);
  }

  getLigne(id: number): Observable<Ligne> {
    return this.http.get<Ligne>(`${this.apiUrl}/${id}`);
  }

  getLignesByDevis(devisId: number): Observable<Ligne[]> {
    return this.http.get<Ligne[]>(`${this.apiUrl}/devis/${devisId}`);
  }

  getLignesByFacture(factureId: number): Observable<Ligne[]> {
    return this.http.get<Ligne[]>(`${this.apiUrl}/facture/${factureId}`);
  }

  addLigne(ligne: Ligne): Observable<Ligne> {
    return this.http.post<Ligne>(this.apiUrl, ligne);
  }

  updateLigne(id: number, ligne: Ligne): Observable<Ligne> {
    return this.http.put<Ligne>(`${this.apiUrl}/${id}`, ligne);
  }

  deleteLigne(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
