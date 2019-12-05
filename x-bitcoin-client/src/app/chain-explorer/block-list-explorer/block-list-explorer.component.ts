import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';

@Component({
  selector: 'app-block-list-explorer',
  templateUrl: './block-list-explorer.component.html',
  styleUrls: ['./block-list-explorer.component.scss']
})
export class BlockListExplorerComponent implements OnInit {

  @Input() blocks: string[];

  @Input() selectedBlock: string;
  @Output() selectedBlockChange = new EventEmitter<string>();
  selectedOptions: any;

  constructor() {
  }

  ngOnInit() {

  }

  handleSelection(value: string) {
    this.selectedBlockChange.emit(value);
  }
}
