import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RecoverAccessComponent } from './recover-access.component';

describe('RecoverAccessComponent', () => {
  let component: RecoverAccessComponent;
  let fixture: ComponentFixture<RecoverAccessComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RecoverAccessComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(RecoverAccessComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
