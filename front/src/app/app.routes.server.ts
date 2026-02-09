import { RenderMode, ServerRoute } from '@angular/ssr';

export const serverRoutes: ServerRoute[] = [
  // ✅ Static routes → prerender
  {
    path: '',
    renderMode: RenderMode.Prerender,
  },
  {
    path: 'login',
    renderMode: RenderMode.Prerender,
  },
  {
    path: 'register',
    renderMode: RenderMode.Prerender,
  },

  // 🔥 Dynamic route → CLIENT render (NO prerender)
  {
    path: 'edit-product/:id',
    renderMode: RenderMode.Client,
  },

  // Fallback → SSR
  {
    path: '**',
    renderMode: RenderMode.Server,
  },
];
