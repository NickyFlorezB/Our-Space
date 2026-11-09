import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import {
  LucideBookOpen,
  LucideClapperboard,
  LucideImage,
  LucideMusic2,
  LucideNewspaper,
} from '@lucide/angular';

@Component({
  imports: [
    RouterLink,
    RouterLinkActive,
    RouterOutlet,
    LucideBookOpen,
    LucideClapperboard,
    LucideImage,
    LucideMusic2,
    LucideNewspaper,
  ],
  selector: 'app-main-layout',
  styleUrl: './main-layout.component.scss',
  templateUrl: './main-layout.component.html',
})
export default class MainLayoutComponent {}
