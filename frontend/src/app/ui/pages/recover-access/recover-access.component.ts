import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { LucideLock } from '@lucide/angular';

@Component({
  imports: [LucideLock, RouterLink],
  selector: 'app-recover-access.component',
  styleUrl: './recover-access.component.scss',
  templateUrl: './recover-access.component.html',
})
export default class RecoverAccessComponent {}
