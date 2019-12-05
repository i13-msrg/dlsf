import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {WorkerModel} from "./worker.model";
import {Observable} from "rxjs";
import {webSocket, WebSocketSubject} from "rxjs/webSocket";
import {SimulationModel} from "./simulation.model";
import {SimulationRunModel} from "./simulation-run.model";
import {CreateSimulationRunModel} from "./create-simulation-run.model";

@Injectable({
  providedIn: 'root'
})
export class DLSFService {
  private readonly httpBaseUrl = 'http://localhost:8080';
  private readonly wsBaseUrl = 'ws://localhost:8080';

  constructor(private http: HttpClient) {
  }

  getWorkers(): Observable<WorkerModel[]> {
    const url = `${this.httpBaseUrl}/workers`;
    return this.http.get<WorkerModel[]>(url);
  }

  getSimulations(): Observable<SimulationModel[]> {
    const url = `${this.httpBaseUrl}/simulations`;
    return this.http.get<SimulationModel[]>(url);
  }

  runSimulation(config: CreateSimulationRunModel): Observable<any> {
    const url = `${this.httpBaseUrl}/active-run`;
    return this.http.put<SimulationModel[]>(url, config);
  }

  getActiveSimulationRun(): Observable<SimulationRunModel> {
    const url = `${this.httpBaseUrl}/active-run`;
    return this.http.get<SimulationRunModel>(url);
  }

  stopActiveSimulationRun(): Observable<any> {
    const url = `${this.httpBaseUrl}/active-run`;
    return this.http.delete<SimulationRunModel>(url);
  }

  getActiveSimulationRunUpdates<T>(): WebSocketSubject<T> {
    const url = `${this.wsBaseUrl}/active-run/updates`;
    return webSocket<T>(url);
  }

  getSimulationRunResults<T>(id: string): Observable<T> {
    const url = `${this.httpBaseUrl}/runs/${id}/results`;
    return this.http.get<T>(url);
  }

  getSimulationRunList(): Observable<SimulationRunModel[]> {
    const url = `${this.httpBaseUrl}/runs`;
    return this.http.get<SimulationRunModel[]>(url);
  }

  getSimulationRun(id: string): Observable<SimulationRunModel> {
    const url = `${this.httpBaseUrl}/runs/${id}`;
    return this.http.get<SimulationRunModel>(url);
  }

  deleteSimulationRun(id: string): Observable<any> {
    const url = `${this.httpBaseUrl}/runs/${id}`;
    return this.http.delete(url);
  }
}
