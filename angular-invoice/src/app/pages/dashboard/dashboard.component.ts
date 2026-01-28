import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { ClientService } from '../../services/client.service';
import { DevisService } from '../../services/devis.service';
import { FactureService } from '../../services/facture.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    MatCardModule,
    MatIconModule,
    MatButtonModule
  ],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent implements OnInit {
  private clientService = inject(ClientService);
  private devisService = inject(DevisService);
  private factureService = inject(FactureService);

  stats = {
    clients: 0,
    devis: 0,
    factures: 0,
    facturesPayees: 0,
    totalCA: 0
  };

  recentDevis: any[] = [];
  recentFactures: any[] = [];

  ngOnInit(): void {
    this.loadStats();
  }

  loadStats(): void {
    this.clientService.getClients().subscribe({
      next: (data) => this.stats.clients = data.length
    });

    this.devisService.getDevis().subscribe({
      next: (data) => {
        this.stats.devis = data.length;
        this.recentDevis = data.slice(-5).reverse();
      }
    });

    this.factureService.getFactures().subscribe({
      next: (data) => {
        this.stats.factures = data.length;
        this.stats.facturesPayees = data.filter(f => f.statut === 'PAYÉE').length;
        this.stats.totalCA = data.filter(f => f.statut === 'PAYÉE').reduce((sum, f) => sum + (f.totalTTC || 0), 0);
        this.recentFactures = data.slice(-5).reverse();
      }
    });
  }
}
