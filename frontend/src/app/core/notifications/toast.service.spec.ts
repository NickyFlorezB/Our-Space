import { TestBed } from '@angular/core/testing';
import { ToastService } from './toast.service';

describe('ToastService', () => {
  let toastService: ToastService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    toastService = TestBed.inject(ToastService);
  });

  it('shows and dismisses error notifications', () => {
    toastService.showError('Something went wrong');
    const toast = toastService.messages()[0];

    expect(toast.message).toBe('Something went wrong');

    toastService.dismiss(toast.id);

    expect(toastService.messages()).toEqual([]);
  });
});
