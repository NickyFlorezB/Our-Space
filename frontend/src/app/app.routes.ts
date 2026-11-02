import { Routes } from '@angular/router';
import { authGuard } from './core/auth/guards/auth.guard';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./ui/layouts/main-layout/main-layout.component'),
    children: [
      {
        path: 'news',
        loadComponent: () => import('./ui/pages/news-page/news-page.component'),
        canActivate: [authGuard],
        title: 'News Page',
      },
      {
        path: 'photos',
        loadComponent: () => import('./ui/pages/photos-page/photos-page.component'),
        canActivate: [authGuard],
        title: 'Photos Page',
      },
      {
        path: 'photos/:imageId',
        loadComponent: () => import('./ui/pages/photo-detail-page/photo-detail-page.component'),
        canActivate: [authGuard],
        title: 'Detalle de foto',
      },
    ],
  },
  {
    path: 'home',
    loadComponent: () => import('./ui/pages/home-page/home-page.component'),
    title: 'Home Page',
  },
  {
    path: 'login',
    loadComponent: () => import('./ui/pages/sign-in-page/sign-in-page.component'),
    title: 'Sign In Page',
  },
  {
    path: 'recover',
    loadComponent: () => import('./ui/pages/recover-access/recover-access.component'),
    title: 'Recover Access Page',
  },
  {
    path: '**',
    redirectTo: 'home',
  },
];
