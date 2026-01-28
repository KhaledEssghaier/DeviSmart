import { Routes } from '@angular/router';
import { LayoutComponent } from './components/layout/layout.component';
import { DashboardComponent } from './pages/dashboard/dashboard.component';
import { ClientsPageComponent } from './pages/clients-page/clients-page.component';
import { DevisPageComponent } from './pages/devis-page/devis-page.component';
import { FacturesPageComponent } from './pages/factures-page/factures-page.component';

export const routes: Routes = [
  {
    path: '',
    component: LayoutComponent,
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      { path: 'dashboard', component: DashboardComponent },
      { path: 'clients', component: ClientsPageComponent },
      { path: 'devis', component: DevisPageComponent },
      { path: 'factures', component: FacturesPageComponent }
    ]
  },
  { path: '**', redirectTo: 'dashboard' }
];
