# Wishlist — Functional Story

---

## Epic
**As a registered customer, I want to save products to a wishlist so that I can revisit and purchase them later.**

---

## User Stories

---

### WL-01 — Add Product to Wishlist

> As a logged-in customer, I want to add a product to my wishlist from the product listing or product detail page, so that I can save it for later without adding it to my cart.

**Acceptance Criteria:**
- A heart/wishlist icon is visible on every product card and on the product detail page
- Clicking the icon while logged in adds the product to the wishlist
- The icon changes to a filled state to indicate the product is already wishlisted
- If the customer is not logged in, clicking the icon redirects to the login page
- A success toast notification confirms "Product added to wishlist"
- If the product is already in the wishlist, clicking again removes it (toggle behaviour)

---

### WL-02 — View Wishlist

> As a logged-in customer, I want to view all my saved products in one place, so that I can review what I've saved.

**Acceptance Criteria:**
- A wishlist icon in the header shows the current item count
- Clicking the icon navigates to `/wishlist`
- The wishlist page displays each saved product with its image, name, current price, and availability status
- If the wishlist is empty, a message is shown: "Your wishlist is empty" with a link to continue shopping
- If a product has gone out of stock since it was saved, it is visually marked as unavailable

---

### WL-03 — Remove Product from Wishlist

> As a logged-in customer, I want to remove a product from my wishlist, so that I can keep my list relevant.

**Acceptance Criteria:**
- Each wishlist item has a remove button (X icon or "Remove" link)
- Clicking remove immediately removes the item from the list
- The wishlist count in the header updates accordingly
- A toast notification confirms "Product removed from wishlist"

---

### WL-04 — Move Product from Wishlist to Cart

> As a logged-in customer, I want to add a wishlisted product directly to my cart, so that I can purchase it without having to find it again.

**Acceptance Criteria:**
- Each wishlist item has an "Add to Cart" button
- Clicking it adds the product to the cart using the existing cart flow
- The cart count in the header updates accordingly
- The product remains in the wishlist after being added to cart (customer can choose to remove it separately)
- If the product is out of stock, the "Add to Cart" button is disabled

---

### WL-05 — Wishlist Persistence Across Sessions

> As a logged-in customer, I want my wishlist to be saved to my account, so that it is available when I log in from a different device or browser.

**Acceptance Criteria:**
- Wishlist is stored server-side and tied to the customer's account
- Logging out and logging back in restores the same wishlist
- Wishlist is not available to guest/unauthenticated users
- Wishlist data is loaded on login and stored in Redux for the session


