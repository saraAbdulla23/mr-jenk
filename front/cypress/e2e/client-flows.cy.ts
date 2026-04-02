describe('Full Client Purchase Flow', () => {

  const baseUrl = 'http://localhost:4200';

  beforeEach(() => {
    cy.visit(`${baseUrl}/login`);
    cy.screenshot('01-login-page-loaded');
  });

  it('logs in, adds products, and completes checkout', () => {

    // ---------- LOGIN ----------
    cy.get('input[formControlName="email"]')
      .should('be.visible')
      .type('john@example.com');

    cy.get('input[formControlName="password"]')
      .should('be.visible')
      .type('Sara!123456');

    cy.screenshot('02-login-form-filled');

    cy.contains('button', 'Login')
      .should('not.be.disabled')
      .click();

    cy.url({ timeout: 10000 }).should('include', '/dashboard');
    cy.screenshot('03-dashboard-loaded');


    // ---------- ADD PRODUCTS ----------
    cy.get('.product-card', { timeout: 10000 })
      .should('have.length.at.least', 2);

    cy.screenshot('04-products-visible');

    cy.get('.add-to-cart-btn').eq(0).click();
    cy.screenshot('05-first-product-added');

    cy.get('.add-to-cart-btn').eq(1).click();
    cy.screenshot('06-second-product-added');


    // ---------- GO TO CART ----------
    cy.get('.cart-btn').click();

    cy.url({ timeout: 10000 }).should('include', '/cart');
    cy.screenshot('07-cart-page');

    cy.get('.cart-item', { timeout: 10000 })
      .should('have.length.at.least', 2);

    cy.screenshot('08-cart-items-confirmed');


    // ---------- OPEN CHECKOUT ----------
    cy.contains('button', 'Proceed to Checkout')
      .should('be.visible')
      .click();

    cy.url({ timeout: 10000 }).should('include', '/checkout');
    cy.screenshot('09-checkout-page');


    // ---------- STEP 1: ADDRESS ----------
    cy.get('input[formControlName="street"]')
      .clear()
      .type('123 Test Street');

    cy.get('input[formControlName="city"]')
      .clear()
      .type('Manama');

    cy.get('input[formControlName="zip"]')
      .clear()
      .type('12345');

    cy.get('select[formControlName="country"]')
      .select('Bahrain');

    cy.screenshot('10-address-filled');

    cy.contains('button', 'Next')
      .should('not.be.disabled')
      .click();


    // ---------- STEP 2: REVIEW ----------
    cy.contains('Review Your Order', { timeout: 10000 })
      .should('be.visible');

    cy.screenshot('11-review-order');

    cy.contains('button', 'Confirm')
      .click();


    // ---------- STEP 3: CONFIRM ORDER ----------
    cy.contains('Confirm Your Order', { timeout: 10000 })
      .should('be.visible');

    cy.screenshot('12-confirm-order');

    cy.contains('button', 'Confirm & Pay on Delivery')
      .click();


    // ---------- VERIFY SUCCESS ----------
    cy.contains('Order placed successfully', { timeout: 10000 })
      .should('be.visible');

    cy.screenshot('13-order-success');

  });

});