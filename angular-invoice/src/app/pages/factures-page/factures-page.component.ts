import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { FactureService } from '../../services/facture.service';
import { Facture } from '../../models/facture.model';
import { FactureDialogComponent } from '../../components/facture-dialog/facture-dialog.component';

interface CachedFacture {
  nomClient: string;
  adresseClient?: string;
  telephoneClient?: string;
  emailClient?: string;
  matriculeFiscalClient?: string;
  tauxTVA: number;
  conditionsPaiement?: string;
  notes?: string;
  lignes: { designation: string; quantite: number; prixUnitaireHT: number }[];
  totalHT: number;
  totalTVA: number;
  totalTTC: number;
  dateCreation: string;
}

@Component({
  selector: 'app-factures-page',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatSnackBarModule,
    MatTooltipModule,
    MatDialogModule
  ],
  templateUrl: './factures-page.component.html',
  styleUrl: './factures-page.component.scss'
})
export class FacturesPageComponent implements OnInit {
  private factureService = inject(FactureService);
  private snackBar = inject(MatSnackBar);
  private dialog = inject(MatDialog);

  private readonly CACHE_KEY = 'factures_brouillons';

  factures: Facture[] = [];
  cachedFactures: CachedFacture[] = [];
  displayedColumns: string[] = ['numero', 'client', 'dateEmission', 'statut', 'total', 'actions'];

  ngOnInit(): void {
    this.loadFactures();
    this.loadCachedFactures();
  }

  loadFactures(): void {
    this.factureService.getFactures().subscribe({
      next: (data) => this.factures = data,
      error: () => this.showError('Erreur lors du chargement des factures')
    });
  }

  loadCachedFactures(): void {
    const cached = localStorage.getItem(this.CACHE_KEY);
    if (cached) {
      this.cachedFactures = JSON.parse(cached);
    }
  }

  saveCachedFactures(): void {
    localStorage.setItem(this.CACHE_KEY, JSON.stringify(this.cachedFactures));
  }

  marquerPayee(id: number): void {
    this.factureService.marquerPayee(id).subscribe({
      next: () => {
        this.showSuccess('Facture marquée comme payée');
        this.loadFactures();
      },
      error: () => this.showError('Erreur lors de la mise à jour')
    });
  }

  marquerNonPayee(id: number): void {
    this.factureService.marquerNonPayee(id).subscribe({
      next: () => {
        this.showSuccess('Facture marquée comme non payée');
        this.loadFactures();
      },
      error: () => this.showError('Erreur lors de la mise à jour')
    });
  }

  deleteFacture(id: number): void {
    if (confirm('Êtes-vous sûr de vouloir supprimer cette facture ?')) {
      this.factureService.deleteFacture(id).subscribe({
        next: () => {
          this.showSuccess('Facture supprimée');
          this.loadFactures();
        },
        error: () => this.showError('Erreur lors de la suppression')
      });
    }
  }

  downloadPdf(facture: Facture): void {
    this.factureService.telechargerPdf(facture.id!).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `facture-${facture.numeroFacture || facture.numero || facture.id}.pdf`;
        link.click();
        window.URL.revokeObjectURL(url);
        this.showSuccess('PDF téléchargé avec succès');
      },
      error: () => {
        this.showError('Erreur lors du téléchargement du PDF');
      }
    });
  }

  getStatutClass(statut: string): string {
    return (statut === 'PAYÉE' || statut === 'PAYEE') ? 'status-success' : 'status-danger';
  }

  getTotalPayees(): number {
    return this.factures.filter(f => f.statut === 'PAYÉE' || f.statut === 'PAYEE').reduce((sum, f) => sum + (f.totalTTC || 0), 0);
  }

  getTotalNonPayees(): number {
    return this.factures.filter(f => f.statut === 'NON PAYÉE' || f.statut === 'NON_PAYEE').reduce((sum, f) => sum + (f.totalTTC || 0), 0);
  }

  openNewFactureDialog(editData?: CachedFacture, editIndex?: number): void {
    const dialogRef = this.dialog.open(FactureDialogComponent, {
      width: '800px',
      maxHeight: '90vh',
      disableClose: true,
      data: editData || null
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        // Calculate totals
        const totalHT = result.lignes.reduce((sum: number, l: any) => sum + (l.quantite * l.prixUnitaireHT), 0);
        const totalTVA = totalHT * (result.tauxTVA || 0.19);
        const totalTTC = totalHT + totalTVA;

        const cachedFacture: CachedFacture = {
          ...result,
          totalHT,
          totalTVA,
          totalTTC,
          dateCreation: new Date().toISOString()
        };

        if (editIndex !== undefined) {
          // Update existing cached facture
          this.cachedFactures[editIndex] = cachedFacture;
          this.showSuccess('Brouillon mis à jour');
        } else {
          // Add new cached facture
          this.cachedFactures.push(cachedFacture);
          this.showSuccess('Facture sauvegardée en brouillon');
        }
        
        this.saveCachedFactures();
      }
    });
  }

  editCachedFacture(index: number): void {
    const facture = this.cachedFactures[index];
    this.openNewFactureDialog(facture, index);
  }

  saveCachedToServer(index: number): void {
    const cachedFacture = this.cachedFactures[index];
    
    this.factureService.creerFactureManuelle(cachedFacture).subscribe({
      next: (savedFacture) => {
        // Remove from cache
        this.cachedFactures.splice(index, 1);
        this.saveCachedFactures();
        
        this.showSuccess('Facture sauvegardée sur le serveur');
        this.loadFactures();
        
        // Offer PDF download
        if (savedFacture && savedFacture.id) {
          this.downloadPdf(savedFacture);
        }
      },
      error: () => this.showError('Erreur lors de la sauvegarde sur le serveur')
    });
  }

  exportCachedToPdf(index: number): void {
    const facture = this.cachedFactures[index];
    this.generateLocalPdf(facture);
  }

  deleteCachedFacture(index: number): void {
    if (confirm('Êtes-vous sûr de vouloir supprimer ce brouillon ?')) {
      this.cachedFactures.splice(index, 1);
      this.saveCachedFactures();
      this.showSuccess('Brouillon supprimé');
    }
  }

  private generateLocalPdf(facture: CachedFacture): void {
    // Create a simple PDF preview using browser print
    const printWindow = window.open('', '_blank');
    if (printWindow) {
      const html = `
        <!DOCTYPE html>
        <html>
        <head>
          <title>Facture - ${facture.nomClient}</title>
          <style>
            body { font-family: Arial, sans-serif; padding: 40px; max-width: 800px; margin: 0 auto; }
            .header { text-align: center; margin-bottom: 30px; border-bottom: 2px solid #dc2626; padding-bottom: 20px; }
            .header h1 { color: #dc2626; margin: 0; }
            .info-section { display: flex; justify-content: space-between; margin-bottom: 30px; }
            .client-info, .facture-info { width: 48%; }
            .client-info h3, .facture-info h3 { color: #333; border-bottom: 1px solid #ddd; padding-bottom: 5px; }
            table { width: 100%; border-collapse: collapse; margin: 20px 0; }
            th, td { border: 1px solid #ddd; padding: 12px; text-align: left; }
            th { background: #f5f5f5; font-weight: bold; }
            .totals { text-align: right; margin-top: 20px; }
            .totals div { margin: 5px 0; }
            .grand-total { font-size: 1.2em; font-weight: bold; color: #dc2626; }
            @media print { body { padding: 20px; } }
          </style>
        </head>
        <body>
          <div class="header">
            <h1>FACTURE</h1>
            <p>Brouillon - ${new Date(facture.dateCreation).toLocaleDateString('fr-FR')}</p>
          </div>
          
          <div class="info-section">
            <div class="client-info">
              <h3>Client</h3>
              <p><strong>${facture.nomClient}</strong></p>
              ${facture.adresseClient ? `<p>${facture.adresseClient}</p>` : ''}
              ${facture.telephoneClient ? `<p>Tél: ${facture.telephoneClient}</p>` : ''}
              ${facture.emailClient ? `<p>Email: ${facture.emailClient}</p>` : ''}
              ${facture.matriculeFiscalClient ? `<p>MF: ${facture.matriculeFiscalClient}</p>` : ''}
            </div>
            <div class="facture-info">
              <h3>Détails</h3>
              <p>Date: ${new Date().toLocaleDateString('fr-FR')}</p>
              <p>TVA: ${(facture.tauxTVA * 100).toFixed(0)}%</p>
              ${facture.conditionsPaiement ? `<p>Conditions: ${facture.conditionsPaiement}</p>` : ''}
            </div>
          </div>
          
          <table>
            <thead>
              <tr>
                <th>Désignation</th>
                <th>Quantité</th>
                <th>Prix Unit. HT</th>
                <th>Total HT</th>
              </tr>
            </thead>
            <tbody>
              ${facture.lignes.map(l => `
                <tr>
                  <td>${l.designation}</td>
                  <td>${l.quantite}</td>
                  <td>${l.prixUnitaireHT.toFixed(3)} TND</td>
                  <td>${(l.quantite * l.prixUnitaireHT).toFixed(3)} TND</td>
                </tr>
              `).join('')}
            </tbody>
          </table>
          
          <div class="totals">
            <div>Total HT: ${facture.totalHT.toFixed(3)} TND</div>
            <div>TVA (${(facture.tauxTVA * 100).toFixed(0)}%): ${facture.totalTVA.toFixed(3)} TND</div>
            <div class="grand-total">Total TTC: ${facture.totalTTC.toFixed(3)} TND</div>
          </div>
          
          ${facture.notes ? `<div style="margin-top: 30px; padding: 15px; background: #f9f9f9; border-radius: 5px;"><strong>Notes:</strong> ${facture.notes}</div>` : ''}
          
          <script>window.print();</script>
        </body>
        </html>
      `;
      printWindow.document.write(html);
      printWindow.document.close();
    }
  }

  private showSuccess(message: string): void {
    this.snackBar.open(message, 'Fermer', {
      duration: 3000,
      panelClass: ['success-snackbar']
    });
  }

  private showError(message: string): void {
    this.snackBar.open(message, 'Fermer', {
      duration: 3000,
      panelClass: ['error-snackbar']
    });
  }
}
