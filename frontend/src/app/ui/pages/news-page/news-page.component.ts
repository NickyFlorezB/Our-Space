import { Component } from '@angular/core';
import {
  LucideBookOpen,
  LucideEighthNotes,
  LucideImage,
  LucideClapperboard,
} from '@lucide/angular';

@Component({
  imports: [LucideBookOpen, LucideEighthNotes, LucideImage, LucideClapperboard],
  selector: 'app-news-page.component',
  styleUrl: './news-page.component.scss',
  templateUrl: './news-page.component.html',
})
export default class NewsPageComponent {}
