import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, FormArray, Validators } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatChipsModule } from '@angular/material/chips';
import { DevisService } from '../../services/devis.service';
import { Devis } from '../../models/devis.model';

@Component({
  selector: 'app-devis-page',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatSnackBarModule,
    MatTooltipModule,
    MatChipsModule
  ],
  templateUrl: './devis-page.component.html',
  styleUrl: './devis-page.component.scss'
})
export class DevisPageComponent implements OnInit {
  private devisService = inject(DevisService);
  private snackBar = inject(MatSnackBar);
  private fb = inject(FormBuilder);

  devisList: Devis[] = [];
  displayedColumns: string[] = ['numero', 'client', 'dateCreation', 'dateValidite', 'statut', 'total', 'actions'];
  showForm = false;
  isEditing = false;
  editingId: number | null = null;

  devisForm: FormGroup = this.fb.group({
    clientNom: ['', Validators.required],
    clientEmail: [''],
    clientTelephone: [''],
    clientAdresse: [''],
    dateValidite: ['', Validators.required],
    lignes: this.fb.array([])
  });

  ngOnInit(): void {
    this.loadDevis();
  }

  get lignes(): FormArray {
    return this.devisForm.get('lignes') as FormArray;
  }

  loadDevis(): void {
    this.devisService.getDevis().subscribe({
      next: (data) => this.devisList = data,
      error: () => this.showError('Erreur lors du chargement des devis')
    });
  }

  openForm(): void {
    this.showForm = true;
    this.isEditing = false;
    this.devisForm.reset();
    this.lignes.clear();
    this.addLigne();
  }

  addLigne(): void {
    const ligneGroup = this.fb.group({
      designation: ['', Validators.required],
      quantite: [1, [Validators.required, Validators.min(1)]],
      prixUnitaire: [0, [Validators.required, Validators.min(0)]]
    });
    this.lignes.push(ligneGroup);
  }

  removeLigne(index: number): void {
    if (this.lignes.length > 1) {
      this.lignes.removeAt(index);
    }
  }

  closeForm(): void {
    this.showForm = false;
    this.isEditing = false;
    this.editingId = null;
    this.devisForm.reset();
    this.lignes.clear();
  }

  saveDevis(): void {
    if (this.devisForm.invalid) return;

    const formValue = this.devisForm.value;

    const devis: any = {
      clientNom: formValue.clientNom,
      clientEmail: formValue.clientEmail,
      clientTelephone: formValue.clientTelephone,
      clientAdresse: formValue.clientAdresse,
      dateValidite: this.formatDate(formValue.dateValidite),
      lignes: formValue.lignes
    };

    if (this.isEditing && this.editingId) {
      this.devisService.updateDevis(this.editingId, devis).subscribe({
        next: () => {
          this.showSuccess('Devis modifié avec succès');
          this.loadDevis();
          this.closeForm();
        },
        error: () => this.showError('Erreur lors de la modification du devis')
      });
    } else {
      this.devisService.addDevis(devis).subscribe({
        next: () => {
          this.showSuccess('Devis créé avec succès');
          this.loadDevis();
          this.closeForm();
        },
        error: () => this.showError('Erreur lors de la création du devis')
      });
    }
  }

  editDevis(devis: Devis): void {
    this.isEditing = true;
    this.editingId = devis.id!;
    this.showForm = true;
    this.lignes.clear();

    // Populate the form with devis data
    this.devisForm.patchValue({
      clientNom: devis.clientNom || '',
      clientEmail: devis.clientEmail || '',
      clientTelephone: devis.clientTelephone || '',
      clientAdresse: devis.clientAdresse || '',
      dateValidite: devis.dateValidite ? new Date(devis.dateValidite) : null
    });

    // Add lignes
    if (devis.lignes && devis.lignes.length > 0) {
      devis.lignes.forEach(ligne => {
        const ligneGroup = this.fb.group({
          designation: [ligne.designation, Validators.required],
          quantite: [ligne.quantite, [Validators.required, Validators.min(1)]],
          prixUnitaire: [ligne.prixUnitaire, [Validators.required, Validators.min(0)]]
        });
        this.lignes.push(ligneGroup);
      });
    } else {
      this.addLigne();
    }
  }

  validerDevis(id: number): void {
    this.devisService.validerDevis(id).subscribe({
      next: () => {
        this.showSuccess('Devis validé et facture créée');
        this.loadDevis();
      },
      error: () => this.showError('Erreur lors de la validation')
    });
  }

  refuserDevis(id: number): void {
    this.devisService.refuserDevis(id).subscribe({
      next: () => {
        this.showSuccess('Devis refusé');
        this.loadDevis();
      },
      error: () => this.showError('Erreur lors du refus')
    });
  }

  deleteDevis(id: number): void {
    if (confirm('Êtes-vous sûr de vouloir supprimer ce devis ?')) {
      this.devisService.deleteDevis(id).subscribe({
        next: () => {
          this.showSuccess('Devis supprimé');
          this.loadDevis();
        },
        error: () => this.showError('Erreur lors de la suppression')
      });
    }
  }

  downloadPdf(devis: Devis): void {
    this.devisService.telechargerPdf(devis.id!).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `devis-${devis.numero || devis.id}.pdf`;
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
    switch (statut) {
      case 'VALIDÉ': return 'status-success';
      case 'REFUSÉ': return 'status-danger';
      default: return 'status-warning';
    }
  }

  getDevisEnAttente(): number {
    return this.devisList.filter(d => d.statut === 'BROUILLON').length;
  }

  getDevisValides(): number {
    return this.devisList.filter(d => d.statut === 'VALIDÉ').length;
  }

  calculateTotal(): number {
    return this.lignes.controls.reduce((sum, ligne) => {
      const qty = ligne.get('quantite')?.value || 0;
      const price = ligne.get('prixUnitaire')?.value || 0;
      return sum + (qty * price);
    }, 0);
  }

  private formatDate(date: Date): string {
    return date.toISOString().split('T')[0];
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
