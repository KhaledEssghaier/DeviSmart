import { Component, inject, Inject, Optional } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, FormArray, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatDialogRef, MatDialogModule, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { MatDividerModule } from '@angular/material/divider';

interface FactureDialogData {
  nomClient: string;
  adresseClient?: string;
  telephoneClient?: string;
  emailClient?: string;
  matriculeFiscalClient?: string;
  tauxTVA: number;
  conditionsPaiement?: string;
  notes?: string;
  lignes: { designation: string; quantite: number; prixUnitaireHT: number }[];
}

@Component({
  selector: 'app-facture-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatSelectModule,
    MatDividerModule
  ],
  templateUrl: './facture-dialog.component.html',
  styleUrl: './facture-dialog.component.scss'
})
export class FactureDialogComponent {
  private fb = inject(FormBuilder);
  private dialogRef = inject(MatDialogRef<FactureDialogComponent>);

  factureForm: FormGroup;
  isEditMode = false;

  tauxTVAOptions = [
    { value: 0.19, label: '19%' },
    { value: 0.13, label: '13%' },
    { value: 0.07, label: '7%' },
    { value: 0, label: '0%' }
  ];

  constructor(@Optional() @Inject(MAT_DIALOG_DATA) public data: FactureDialogData | null) {
    this.isEditMode = !!data;
    
    this.factureForm = this.fb.group({
      // Client info
      nomClient: [data?.nomClient || '', Validators.required],
      adresseClient: [data?.adresseClient || ''],
      telephoneClient: [data?.telephoneClient || ''],
      emailClient: [data?.emailClient || '', Validators.email],
      matriculeFiscalClient: [data?.matriculeFiscalClient || ''],
      
      // Facture settings
      tauxTVA: [data?.tauxTVA ?? 0.19],
      conditionsPaiement: [data?.conditionsPaiement || 'Paiement à 30 jours'],
      notes: [data?.notes || ''],
      
      // Lignes
      lignes: this.fb.array([])
    });

    // Populate lignes from data or add default
    if (data?.lignes && data.lignes.length > 0) {
      data.lignes.forEach(ligne => this.ajouterLigne(ligne));
    } else {
      this.ajouterLigne();
    }
  }

  get lignes(): FormArray {
    return this.factureForm.get('lignes') as FormArray;
  }

  ajouterLigne(ligneData?: { designation: string; quantite: number; prixUnitaireHT: number }): void {
    const ligneGroup = this.fb.group({
      designation: [ligneData?.designation || '', Validators.required],
      quantite: [ligneData?.quantite ?? 1, [Validators.required, Validators.min(0.001)]],
      prixUnitaireHT: [ligneData?.prixUnitaireHT ?? 0, [Validators.required, Validators.min(0)]]
    });
    this.lignes.push(ligneGroup);
  }

  supprimerLigne(index: number): void {
    if (this.lignes.length > 1) {
      this.lignes.removeAt(index);
    }
  }

  calculerTotalLigne(index: number): number {
    const ligne = this.lignes.at(index);
    const quantite = ligne.get('quantite')?.value || 0;
    const prix = ligne.get('prixUnitaireHT')?.value || 0;
    return quantite * prix;
  }

  calculerTotalHT(): number {
    let total = 0;
    for (let i = 0; i < this.lignes.length; i++) {
      total += this.calculerTotalLigne(i);
    }
    return total;
  }

  calculerTVA(): number {
    const tauxTVA = this.factureForm.get('tauxTVA')?.value || 0.19;
    return this.calculerTotalHT() * tauxTVA;
  }

  calculerTotalTTC(): number {
    return this.calculerTotalHT() + this.calculerTVA();
  }

  onSubmit(): void {
    if (this.factureForm.valid && this.lignes.length > 0) {
      const formValue = this.factureForm.value;
      this.dialogRef.close(formValue);
    }
  }

  onCancel(): void {
    this.dialogRef.close();
  }
}
