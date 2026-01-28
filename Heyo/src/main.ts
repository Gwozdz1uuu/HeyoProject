import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { App } from './app/app';
import 'bootstrap/dist/js/bootstrap.bundle.min.js';

// Polyfill for Node.js global in browser (required by SockJS/STOMP)
(window as any).global = window;

bootstrapApplication(App, appConfig)
  .catch((err) => console.error(err));
