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
