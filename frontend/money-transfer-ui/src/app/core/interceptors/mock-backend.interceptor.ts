import { HttpEvent, HttpHandlerFn, HttpRequest, HttpResponse } from '@angular/common/http';
import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Observable } from 'rxjs';
import { MockBackendService } from '../services/mock-backend.service';

export const mockBackendInterceptor: HttpInterceptorFn = (
  req: HttpRequest<unknown>,
  next: HttpHandlerFn
): Observable<HttpEvent<unknown>> => {
  const mockBackend = inject(MockBackendService);

  const mockResponse = mockBackend.handle(req);
  if (mockResponse) {
    // Cast because handle() always returns HttpResponse when not null.
    return mockResponse as unknown as Observable<HttpEvent<unknown>>;
  }

  return next(req);
};

