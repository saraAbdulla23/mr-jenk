import { ApplicationConfig, provideBrowserGlobalErrorListeners, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideClientHydration, withEventReplay } from '@angular/platform-browser';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideAnimations } from '@angular/platform-browser/animations';

import { routes } from './app.routes';

// (Optional placeholder – I'll give you a real one when we add JWT)
export function authInterceptor(req: any, next: any) {
  return next(req);
}

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),

    provideZoneChangeDetection({ eventCoalescing: true }),

    // Routing
    provideRouter(routes),

    // Enable HttpClient for backend calls
    provideHttpClient(
      withInterceptors([authInterceptor])   // ← interceptor placeholder
    ),

    // Required for Angular Material
    provideAnimations(),

    // Hydration support
    provideClientHydration(withEventReplay()),
  ]
};
