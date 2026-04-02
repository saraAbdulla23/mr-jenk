import { ApplicationConfig, provideBrowserGlobalErrorListeners, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideClientHydration, withEventReplay } from '@angular/platform-browser';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideAnimations } from '@angular/platform-browser/animations';

import { routes } from './app.routes';

/**
 * Optional HTTP interceptor placeholder for JWT or other auth headers.
 * Replace this with a real interceptor when implementing auth.
 */
export function authInterceptor(req: any, next: any) {
  return next(req);
}

/**
 * Angular application configuration.
 * Includes routing, HTTP client, animations, and hydration support.
 */
export const appConfig: ApplicationConfig = {
  providers: [
    // Global error handling
    provideBrowserGlobalErrorListeners(),

    // Zone.js change detection with event coalescing
    provideZoneChangeDetection({ eventCoalescing: true }),

    // Routing
    provideRouter(routes),

    // HTTP client with optional interceptors
    provideHttpClient(
      withInterceptors([authInterceptor])
    ),

    // Required for Angular Material animations
    provideAnimations(),

    // Hydration support for server-side rendering
    provideClientHydration(withEventReplay()),
  ]
};