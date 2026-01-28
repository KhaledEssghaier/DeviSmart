import { Client } from './client.model';
import { Ligne } from './ligne.model';

export interface Devis {
  id?: number;
  numero: string;
  dateCreation: string;
  dateValidite: string;
  statut: 'BROUILLON' | 'VALIDÉ' | 'REFUSÉ';
  client: Client;
  lignes: Ligne[];
  tauxTVA?: number;
  totalHT?: number;
  montantTVA?: number;
  totalTTC?: number;
}
