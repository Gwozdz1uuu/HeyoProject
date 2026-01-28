import { HttpInterceptorFn } from '@angular/common/http';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = localStorage.getItem('auth_token');
  
  if (token) {
    const clonedRequest = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
    
    // Debug logging for post requests
    if (req.url.includes('/api/posts') && req.method === 'POST') {
      console.debug('Adding auth token to POST request:', req.url);
    }
    
    return next(clonedRequest);
  } else {
    // Log warning if trying to access protected endpoint without token
    if (req.url.includes('/api/') && !req.url.includes('/api/auth/') && !req.url.includes('/api/public/') && !req.url.includes('/api/uploads/')) {
      console.warn('No auth token found for protected endpoint:', req.url);
    }
  }
  
  return next(req);
};
