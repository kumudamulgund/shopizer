# Wishlist Implementation Plan

## Phase 1 — Backend

### Step 1: Data Model (`sm-core-model`)

Create two new JPA entities:

**`Wishlist.java`**
- `id` (Long, PK)
- `wishlistCode` (String, unique) — used like cart code for lookup
- `customerId` (Long, plain column — loose coupling like ShoppingCart)
- `merchantStore` (@ManyToOne → MerchantStore)
- `lineItems` (@OneToMany → WishlistItem)
- `auditSection` (@Embedded)

**`WishlistItem.java`**
- `id` (Long, PK)
- `productId` (Long, plain column — same pattern as ShoppingCartItem)
- `sku` (String)
- `wishlist` (@ManyToOne → Wishlist)
- `auditSection` (@Embedded)

---

### Step 2: API Models (`sm-shop-model`)

- `PersistableWishlistItem` — request body: `{ productId, sku }`
- `ReadableWishlistItem` — response: `{ productId, sku, name, image, price, available }`
- `ReadableWishlist` — response: `{ code, items: [ReadableWishlistItem] }`

---

### Step 3: Repository (`sm-core`)

**`WishlistRepository`** (Spring Data JPA)
- `findByCustomerIdAndMerchantStore(Long customerId, MerchantStore store)`
- `findByWishlistCode(String code)`

---

### Step 4: Service (`sm-core`)

**`WishlistService` / `WishlistServiceImpl`**
- `getByCustomer(Long customerId, MerchantStore store)` → Wishlist
- `addItem(Wishlist wishlist, WishlistItem item)` → Wishlist
- `removeItem(Wishlist wishlist, Long productId)` → void
- `getOrCreate(Long customerId, MerchantStore store)` → Wishlist

---

### Step 5: Facade (`sm-shop`)

**`WishlistFacade` / `WishlistFacadeImpl`**
- `getWishlist(Customer customer, MerchantStore store, Language lang)` → ReadableWishlist
  - Fetches wishlist, enriches each item with product name, image, price via `ProductService`
- `addToWishlist(PersistableWishlistItem item, Customer customer, MerchantStore store)` → void
- `removeFromWishlist(Long productId, Customer customer, MerchantStore store)` → void

---

### Step 6: REST Controller (`sm-shop`)

**`WishlistApi`** — secured under Customer JWT filter chain (no new security config needed)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/customer/wishlist` | Get authenticated customer's wishlist |
| POST | `/api/v1/customer/wishlist` | Add item to wishlist |
| DELETE | `/api/v1/customer/wishlist/{productId}` | Remove item from wishlist |

---

## Phase 2 — Frontend

### Step 1: Redux (`src/redux/`)

**`wishlistReducer.js`**
- State: `{ items: [], count: 0 }`
- Actions: `GET_WISHLIST`, `ADD_TO_WISHLIST`, `REMOVE_FROM_WISHLIST`

**`wishlistActions.js`**
- `getWishlist()` — GET `/api/v1/customer/wishlist`
- `addToWishlist(productId, sku)` — POST `/api/v1/customer/wishlist`
- `removeFromWishlist(productId)` — DELETE `/api/v1/customer/wishlist/{productId}`

Add `wishlistData` to `rootReducer.js` and persist `count` via `redux-localstorage-simple`.

---

### Step 2: API Constant

Add to `constant.js`:
```js
WISHLIST: 'customer/wishlist/'
```

---

### Step 3: Components

**`IconGroup.js`** (header)
- Add wishlist heart icon with item count badge, same pattern as cart icon
- Clicking navigates to `/wishlist`

**Product cards & `ProductDescriptionInfo.js`**
- Add heart icon button
- Filled state if `productId` is in `wishlistData.items`
- On click: dispatch `addToWishlist` if logged in, redirect to `/login` if not
- Toggle — clicking again dispatches `removeFromWishlist`

**`Wishlist.js`** (new page — `src/pages/other/Wishlist.js`)
- Lists all wishlist items with product image, name, price, availability
- "Add to Cart" button per item — calls existing `addToCart` action
- "Remove" button per item — calls `removeFromWishlist`
- Empty state message with link to homepage

---

### Step 4: Routing

Add to `App.js`:
```js
const Wishlist = lazy(() => import("./pages/other/Wishlist"));
<Route path="/wishlist" component={Wishlist} />
```

---

## Phase 3 — Auth Guard

- All wishlist API calls require a valid JWT (Customer token)
- On the frontend, check `userData` in Redux before dispatching wishlist actions
- If not logged in, redirect to `/login` with a return URL back to the product

---

## Execution Order

1. Wishlist + WishlistItem entities
2. WishlistRepository
3. WishlistService + Impl
4. API models (Persistable + Readable)
5. WishlistFacade + Impl
6. WishlistApi controller
7. Redux slice + actions
8. Heart icon on product cards
9. Wishlist page
10. Header icon + count badge

---

## Tickets

**WL-TICKET-01 — Create Wishlist & WishlistItem JPA Entities**
- Type: Backend
- Create `Wishlist.java` and `WishlistItem.java` in `sm-core-model`
- Follow the same patterns as `ShoppingCart` and `ShoppingCartItem`
- `customerId` and `productId` stored as plain Long columns (loose coupling)
- Acceptance: Entities exist, Hibernate auto-creates tables on startup

---

**WL-TICKET-02 — Create WishlistRepository**
- Type: Backend
- Create `WishlistRepository` in `sm-core`
- Methods: `findByCustomerIdAndMerchantStore`, `findByWishlistCode`
- Acceptance: Repository queries return correct results

---

**WL-TICKET-03 — Create WishlistService**
- Type: Backend
- Create `WishlistService` interface and `WishlistServiceImpl` in `sm-core`
- Methods: `getByCustomer`, `addItem`, `removeItem`, `getOrCreate`
- Acceptance: Service correctly delegates to repository, handles create-if-not-exists logic

---

**WL-TICKET-04 — Create Wishlist API Models**
- Type: Backend
- Create `PersistableWishlistItem`, `ReadableWishlistItem`, `ReadableWishlist` in `sm-shop-model`
- Acceptance: Models serialise/deserialise correctly via Jackson

---

**WL-TICKET-05 — Create WishlistFacade**
- Type: Backend
- Create `WishlistFacade` interface and `WishlistFacadeImpl` in `sm-shop`
- Methods: `getWishlist`, `addToWishlist`, `removeFromWishlist`
- Enrich items with product name, image, price via `ProductService`
- Acceptance: Facade returns fully populated `ReadableWishlist`

---

**WL-TICKET-06 — Create WishlistApi REST Controller**
- Type: Backend
- Create `WishlistApi` in `sm-shop` under `/api/v1/customer/wishlist`
- Endpoints: GET (get wishlist), POST (add item), DELETE (remove item)
- Secured under existing Customer JWT filter chain
- Acceptance: All 3 endpoints return correct responses, unauthorised requests return 401

---

**WL-TICKET-07 — Create Redux Wishlist Slice**
- Type: Frontend
- Create `wishlistReducer.js` and `wishlistActions.js` in `src/redux/`
- Actions: `getWishlist`, `addToWishlist`, `removeFromWishlist`
- Add `wishlistData` to `rootReducer.js`, persist `count` to localStorage
- Acceptance: Redux state updates correctly on all three actions

---

**WL-TICKET-08 — Add Heart Icon to Product Cards & Product Detail Page**
- Type: Frontend
- Add wishlist toggle button to product cards and `ProductDescriptionInfo.js`
- Filled state when product is in wishlist, outline when not
- Redirect to `/login` if user is not authenticated
- Acceptance: Icon toggles correctly, unauthenticated users are redirected

---

**WL-TICKET-09 — Create Wishlist Page**
- Type: Frontend
- Create `src/pages/other/Wishlist.js`
- Display saved products with image, name, price, availability
- "Add to Cart" and "Remove" actions per item
- Empty state with link to homepage
- Acceptance: Page renders correctly, all actions work

---

**WL-TICKET-10 — Add Wishlist Icon to Header**
- Type: Frontend
- Update `IconGroup.js` to include wishlist heart icon with count badge
- Navigates to `/wishlist` on click
- Add `/wishlist` route to `App.js`
- Acceptance: Icon shows correct count, navigation works
