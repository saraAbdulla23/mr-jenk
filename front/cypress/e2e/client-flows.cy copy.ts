describe('Login Page - Successful Login, Add Products, Cart, and Checkout', () => {
  const baseUrl = 'http://localhost:4200';

  beforeEach(() => {
    cy.visit(`${baseUrl}/login`);
  });

  it('logs in, adds two products, goes to cart, and proceeds to checkout', () => {

    // ---------- LOGIN ----------
    cy.get('input[formControlName="email"]')
      .should('be.visible')
      .type('john@example.com');

    cy.get('input[formControlName="password"]')
      .should('be.visible')
      .type('Sara!123456');

    cy.get('button')
      .contains('Login')
      .should('not.be.disabled')
      .click();

    cy.url({ timeout: 10000 }).should('include', '/dashboard');

    // ---------- ADD PRODUCTS ----------
    cy.get('.product-card', { timeout: 10000 })
      .should('have.length.at.least', 2);

    cy.get('.add-to-cart-btn').eq(0).click();
    cy.get('.add-to-cart-btn').eq(1).click();

    // ---------- GO TO CART ----------
    cy.get('.cart-btn').click();

    cy.url({ timeout: 10000 }).should('include', '/cart');

    cy.get('.cart-item', { timeout: 10000 })
      .should('have.length.at.least', 2);

    // ---------- CHECKOUT ----------
    cy.contains('button', 'Proceed to Checkout')
      .should('be.visible')
      .click();

    // Confirm navigation to checkout page
    cy.url({ timeout: 10000 }).should('include', '/checkout');

  });
});