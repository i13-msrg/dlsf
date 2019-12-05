import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {DashboardComponent} from "./dashboard/dashboard.component";
import {SimulationRunListComponent} from "./simulation-run-list/simulation-run-list.component";
import {CreateRunComponent} from "./create-run/create-run.component";
import {ActiveRunComponent} from "./active-run/active-run.component";
import {SimulationRunDetailComponent} from "./simulation-run-detail/simulation-run-detail.component";
import {SimulationRunTxDetailComponent} from "./simulation-run-tx-detail/simulation-run-tx-detail.component";


const routes: Routes = [
  {path: 'dashboard', component: DashboardComponent},
  {path: 'create-run', component: CreateRunComponent},
  {path: 'active-run', component: ActiveRunComponent},
  {path: 'runs', component: SimulationRunListComponent},
  {path: 'runs/bitcoin-explorer/:runId', component: SimulationRunDetailComponent},
  {path: 'runs/bitcoin-tx/:runId', component: SimulationRunTxDetailComponent},

  {path: '**', redirectTo: 'dashboard'}
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
