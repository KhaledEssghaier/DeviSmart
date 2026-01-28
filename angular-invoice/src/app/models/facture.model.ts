import { Client } from './client.model';
import { Ligne } from './ligne.model';

export interface LigneFacture {
  id?: number;
  designation: string;
  quantite: number;
  prixUnitaireHT: number;
  totalLigneHT?: number;
}

export interface Facture {
  id?: number;
  numeroFacture?: string;  // Nouveau format
  numero?: string;         // Ancien format (compatibilité)
  dateEmission: string;
  dateEcheance?: string;
  statut: 'PAYEE' | 'NON_PAYEE' | 'EN_RETARD' | 'ANNULEE' | 'PAYÉE' | 'NON PAYÉE';
  referenceDevis?: string;
  
  // Données entreprise intégrées
  nomEntreprise?: string;
  adresseEntreprise?: string;
  telephoneEntreprise?: string;
  emailEntreprise?: string;
  matriculeFiscal?: string;
  registreCommerce?: string;
  
  // Données client intégrées
  nomClient?: string;
  adresseClient?: string;
  telephoneClient?: string;
  emailClient?: string;
  matriculeFiscalClient?: string;
  
  // Référence client (pour historique)
  client?: Client;
  
  // Lignes
  lignes?: LigneFacture[] | Ligne[];
  
  // Montants
  tauxTVA?: number;
  totalHT?: number;
  montantTVA?: number;
  totalTTC?: number;
  
  // Conditions
  conditionsPaiement?: string;
  notes?: string;
}
