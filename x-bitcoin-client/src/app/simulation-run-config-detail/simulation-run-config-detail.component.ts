import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";

@Component({
  selector: 'app-simulation-run-config-detail',
  templateUrl: './simulation-run-config-detail.component.html',
  styleUrls: ['./simulation-run-config-detail.component.scss']
})
export class SimulationRunConfigDetailComponent {
  config: any;

  constructor(public dialogRef: MatDialogRef<SimulationRunConfigDetailComponent>,
              @Inject(MAT_DIALOG_DATA) public data: any) {
    this.config = JSON.stringify(data, undefined, 2);
  }

  closeDialog() {
    this.dialogRef.close('Pizza!');
  }
}
