import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { LucideHeart, LucideMessageSquare } from '@lucide/angular';

@Component({
  imports: [LucideHeart, LucideMessageSquare, RouterLink],
  selector: 'app-home-page.component',
  styleUrl: './home-page.component.scss',
  templateUrl: './home-page.component.html',
})
export default class HomePageComponent {}
