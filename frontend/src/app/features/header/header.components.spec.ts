import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { HeaderComponent } from './header.components';
import { KeycloakService } from '../../keycloak.service';

describe('HeaderComponent', () => {
  let component: HeaderComponent;
  let fixture: ComponentFixture<HeaderComponent>;
  let mockKeycloak: { logout: jasmine.Spy };

  beforeEach(async () => {
    mockKeycloak = { logout: jasmine.createSpy('logout') };

    await TestBed.configureTestingModule({
      imports: [RouterTestingModule],
      declarations: [HeaderComponent],
      providers: [{ provide: KeycloakService, useValue: mockKeycloak }]
    }).compileComponents();

    fixture = TestBed.createComponent(HeaderComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display title', () => {
    const titleEl: HTMLElement = fixture.nativeElement.querySelector('.title');
    expect(titleEl.textContent?.trim()).toBe(component.title);
  });

  it('should call keycloakService.logout when logout button clicked', () => {
    const button: HTMLButtonElement = fixture.nativeElement.querySelector('button');
    button.click();
    expect(mockKeycloak.logout).toHaveBeenCalled();
  });
});

