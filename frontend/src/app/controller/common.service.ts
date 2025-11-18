import {throwError} from 'rxjs';

export const  handleError = (err: any) => {
  let message = 'Neznámá chyba';
  if (err && typeof err === 'object') {
    if ('body' in err && err.body && typeof err.body === 'object') {
      message = (err.body as any).message || JSON.stringify(err.body);
    } else if ('message' in err) {
      message = (err as any).message;
    }
  }
  return throwError(() => new Error(message));
};
