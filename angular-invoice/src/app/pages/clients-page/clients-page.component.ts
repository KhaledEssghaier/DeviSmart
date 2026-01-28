import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ClientService } from '../../services/client.service';
import { Client } from '../../models/client.model';

@Component({
  selector: 'app-clients-page',
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
    MatSnackBarModule,
    MatTooltipModule
  ],
  templateUrl: './clients-page.component.html',
  styleUrl: './clients-page.component.scss'
})
export class ClientsPageComponent implements OnInit {
  private clientService = inject(ClientService);
  private snackBar = inject(MatSnackBar);
  private fb = inject(FormBuilder);

  clients: Client[] = [];
  displayedColumns: string[] = ['nom', 'email', 'telephone', 'adresse', 'matriculeFiscal', 'actions'];
  showForm = false;
  isEditing = false;
  editingId: number | null = null;

  clientForm: FormGroup = this.fb.group({
    nom: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    telephone: ['', Validators.required],
    adresse: ['', Validators.required],
    matriculeFiscal: ['', Validators.required]
  });

  ngOnInit(): void {
    this.loadClients();
  }

  loadClients(): void {
    this.clientService.getClients().subscribe({
      next: (data) => this.clients = data,
      error: (err) => this.showError('Erreur lors du chargement des clients')
    });
  }

  openForm(): void {
    this.showForm = true;
    this.isEditing = false;
    this.clientForm.reset();
  }

  editClient(client: Client): void {
    this.showForm = true;
    this.isEditing = true;
    this.editingId = client.id!;
    this.clientForm.patchValue(client);
  }

  closeForm(): void {
    this.showForm = false;
    this.isEditing = false;
    this.editingId = null;
    this.clientForm.reset();
  }

  saveClient(): void {
    if (this.clientForm.invalid) return;

    const client: Client = this.clientForm.value;

    if (this.isEditing && this.editingId) {
      this.clientService.updateClient(this.editingId, client).subscribe({
        next: () => {
          this.showSuccess('Client mis à jour avec succès');
          this.loadClients();
          this.closeForm();
        },
        error: () => this.showError('Erreur lors de la mise à jour')
      });
    } else {
      this.clientService.addClient(client).subscribe({
        next: () => {
          this.showSuccess('Client ajouté avec succès');
          this.loadClients();
          this.closeForm();
        },
        error: () => this.showError('Erreur lors de l\'ajout')
      });
    }
  }

  deleteClient(id: number): void {
    if (confirm('Êtes-vous sûr de vouloir supprimer ce client ?')) {
      this.clientService.deleteClient(id).subscribe({
        next: () => {
          this.showSuccess('Client supprimé avec succès');
          this.loadClients();
        },
        error: () => this.showError('Erreur lors de la suppression')
      });
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
