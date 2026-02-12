import { ComponentFixture, TestBed } from '@angular/core/testing';
import { EditProduct } from './edit-product';
import { ProductService } from '../services/product.service';
import { MediaService } from '../services/media.service';
import { ActivatedRoute, Router } from '@angular/router';
import { of } from 'rxjs';

describe('EditProduct', () => {
  let component: EditProduct;
  let fixture: ComponentFixture<EditProduct>;

  // Mock services
  const productServiceMock = {
    getAllProducts: () =>
      of([
        {
          productId: '1',
          name: 'Test Product',
          price: 100,
          quantity: 1,
          userId: 'user1',
          description: 'Sample',
        },
      ]),
    updateProduct: (id: string, product: any) => of(product),
  };

  const mediaServiceMock = {
    getImagesByProduct: (id: string) => of({ images: [] }),
    uploadMedia: (file: File, id: string) =>
      of({ media: { id: 'm1', url: 'test-url' } }),
    deleteMedia: (media: any) => of(null),
  };

  // Mock Router
  const routerMock = {
    navigate: jasmine.createSpy('navigate'),
  };

  // Mock ActivatedRoute
  const activatedRouteMock = {
    snapshot: { paramMap: { get: () => '1' } },
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EditProduct],
      providers: [
        { provide: ProductService, useValue: productServiceMock },
        { provide: MediaService, useValue: mediaServiceMock },
        { provide: Router, useValue: routerMock },
        { provide: ActivatedRoute, useValue: activatedRouteMock },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(EditProduct);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load product on init', () => {
    expect(component.product.name).toBe('Test Product');
    expect(component.loading).toBeFalse();
    expect(component.error).toBe('');
  });

  it('should call router.navigate on save', () => {
    component.save();
    expect(routerMock.navigate).toHaveBeenCalledWith(['/dashboard'], {
      state: { message: 'Product updated successfully!' },
    });
  });
});
