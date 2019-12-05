import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';

import {AppRoutingModule} from './app-routing.module';
import {AppComponent} from './app.component';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {MaterialModule} from "./material/material.module";
import {DashboardComponent} from './dashboard/dashboard.component';
import {SimulationRunListComponent} from './simulation-run-list/simulation-run-list.component';
import {HttpClientModule} from "@angular/common/http";
import {CreateRunComponent} from './create-run/create-run.component';
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {ActiveRunComponent} from './active-run/active-run.component';
import {NgxGraphModule} from "@swimlane/ngx-graph";
import {NetworkTopologyComponent} from './network-topology/network-topology.component';
import {BlockGraphComponent} from './block-graph/block-graph.component';
import {SimulationRunDetailComponent} from './simulation-run-detail/simulation-run-detail.component';
import {SimulationStatsComponent} from './simulation-stats/simulation-stats.component';
import {ChainExplorerModule} from "./chain-explorer/chain-explorer.module";
import {SimulationRunTxDetailComponent} from './simulation-run-tx-detail/simulation-run-tx-detail.component';
import {SimulationNetworkStatsComponent} from './simulation-network-stats/simulation-network-stats.component';
import {SimulationRunConfigDetailComponent} from './simulation-run-config-detail/simulation-run-config-detail.component';
import {SimulationRunInfoComponent} from './simulation-run-info/simulation-run-info.component';
import {NgxChartsModule} from "@swimlane/ngx-charts";
import {NetworkChartComponent} from './network-chart/network-chart.component';

@NgModule({
  declarations: [
    AppComponent,
    DashboardComponent,
    SimulationRunListComponent,
    CreateRunComponent,
    ActiveRunComponent,
    NetworkTopologyComponent,
    BlockGraphComponent,
    SimulationRunDetailComponent,
    SimulationStatsComponent,
    SimulationRunTxDetailComponent,
    SimulationNetworkStatsComponent,
    SimulationRunConfigDetailComponent,
    SimulationRunInfoComponent,
    NetworkChartComponent,
  ],
  entryComponents: [
    SimulationRunConfigDetailComponent,
  ],
  imports: [
    BrowserModule,
    HttpClientModule,
    AppRoutingModule,
    ReactiveFormsModule,
    BrowserAnimationsModule,
    MaterialModule,
    FormsModule,
    ChainExplorerModule,
    NgxGraphModule,
    NgxChartsModule,
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule {
}
