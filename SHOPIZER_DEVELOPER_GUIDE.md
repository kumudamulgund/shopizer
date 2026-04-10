# Shopizer Tech Stack

## Overview

Shopizer is an open-source Java-based e-commerce platform (v3.2.5) built on Spring Boot. It follows a multi-module Maven architecture with a separate React frontend.

## Backend

| Layer | Technology | Version |
|-------|-----------|---------|
| Framework | Spring Boot | 2.5.12 |
| Language | Java | 11 |
| Build Tool | Apache Maven | Multi-module |
| Security | Spring Security + JWT (jjwt) | 0.8.0 |
| ORM | Hibernate (via Spring Data JPA) | — |
| API Docs | Swagger / Springfox | 2.9.2 |
| Templating | FreeMarker | — |

## Database

| Option | Driver | Notes |
|--------|--------|-------|
| H2 (default/docker) | `org.h2.Driver` | Embedded, file-based. Used for dev/docker profiles |
| MySQL | `com.mysql.cj.jdbc.Driver` 8.0.21 | Used for local/cloud profiles |
| PostgreSQL | `postgresql` 42.2.18 | Available but commented out |

Schema: `SALESMANAGER`

## Caching

- **Ehcache** — Hibernate second-level cache
- **Infinispan** 9.4.18.Final — CMS content and product image caching (tree-based cache store)

## Search

- **Elasticsearch** 7.5.2 (via `shopizer-search` module v2.11.1)

## Payment Integrations

- Stripe / Stripe 3D Secure
- PayPal Express Checkout
- Braintree
- BeanStream
- Money Order (offline)

## Shipping Integrations

- Canada Post (via `shopizer-canadapost` module v2.15.0)
- UPS
- USPS
- Store Pickup
- Custom weight-based / price-by-distance rules

## Cloud Services

- **AWS S3** — Product images and static content storage
- **AWS SES** — Email sending
- **Google Cloud Storage** — Alternative CMS/content storage

## Rules Engine

- **Drools** 7.32.0.Final — Business rules for order totals, promo codes, shipping calculations

## Other Libraries

- Jackson 2.13.4 — JSON serialization
- MapStruct 1.3.0 — Object mapping
- Google Maps Services 0.1.6 — Geocoding and distance calculations
- GeoIP2 2.7.0 — IP-based geolocation
- Apache Commons (Lang3, IO, Collections4, Validator, FileUpload)
- Guava 27.1-jre

## Project Modules

```
shopizer/
├── sm-core-model     # JPA entities and domain model
├── sm-core-modules   # Integration module interfaces (payment, shipping, CMS)
├── sm-core           # Business services, repositories, CMS implementations
├── sm-shop-model     # REST API DTOs and facade interfaces
└── sm-shop           # Spring Boot app, REST controllers, security config
```

## Frontend (separate project)

| Technology | Version |
|-----------|---------|
| React | 16.x |
| Redux | State management |
| React Router | Client-side routing |
| Axios | HTTP client |
| Bootstrap | 4.5.0 |
| Stripe.js | Payment UI |

## Running

- **Backend (Docker):** Runs on port 8080 by default, uses H2 embedded database
- **Frontend (Dev):** `npm run dev` — starts on port 3000, expects backend at `http://localhost:8080`

---

## Backend Architecture

### 1. High-Level Overview

Shopizer follows a layered architecture split across five Maven modules, where each module has a clear responsibility and dependencies only flow downward.

```
┌─────────────────────────────────────────────────────┐
│                     sm-shop                         │
│         (Spring Boot App, REST Controllers,         │
│          Security, Facades, Mappers)                │
├──────────────────────┬──────────────────────────────┤
│    sm-shop-model     │          sm-core             │
│  (DTOs, API Models,  │   (Business Services,        │
│   Facade Interfaces) │    Repositories, CMS,        │
│                      │    Integration Impls)         │
│                      ├──────────────┬───────────────┤
│                      │ sm-core-model│ sm-core-modules│
│                      │ (JPA Entities│ (Integration   │
│                      │  Domain Objs)│  Interfaces)   │
└──────────────────────┴──────────────┴───────────────┘
```

### 2. Module Breakdown

#### sm-core-model (Domain Layer)
The foundation of the application. Contains all JPA/Hibernate entity classes mapped to the `SALESMANAGER` database schema.

Organized into 15 domain packages:
- `catalog` — Product, Category, Manufacturer, ProductVariant, ProductPrice, ProductImage, Catalog
- `order` — Order, OrderProduct, OrderTotal, OrderStatusHistory
- `customer` — Customer, CustomerReview, CustomerAttribute
- `merchant` — MerchantStore (the central tenant entity)
- `payments` — Transaction
- `shipping` — ShippingConfiguration, ShippingQuote, ShippingOption
- `tax` — TaxRate, TaxClass
- `user` — User, Group, Permission
- `content` — Content, ContentDescription
- `reference` — Country, Zone, Language, Currency
- `shoppingcart` — ShoppingCart, ShoppingCartItem
- `system` — MerchantConfiguration, IntegrationModule, Optin
- `common` — Billing, Delivery, Criteria (shared value objects)

#### sm-core-modules (Integration Contracts)
Defines interfaces that decouple the core business logic from external service providers:
- `ShippingQuoteModule` — shipping rate calculation
- `PaymentModule` — payment authorization, capture, refund
- `ContentModule` — CMS file storage and retrieval
- `OrderTotalModule` — pluggable order total calculations
- `Encryption`, `GeoLocation` — utility contracts

No implementations live here — only contracts.

#### sm-core (Business Logic Layer)
The heart of the application. Contains all business services, data access, and integration implementations.

**Services** (`business/services/`) — 14 domain service packages:
- `catalog/product` — ProductService, pricing, availability, variants, images, reviews
- `catalog/category` — CategoryService, hierarchy management
- `order` — OrderService, order processing pipeline
- `payments` — PaymentService, TransactionService
- `shipping` — ShippingService, quote calculation
- `customer` — CustomerService, attributes, reviews
- `merchant` — MerchantStoreService
- `tax` — TaxService, TaxRateService, TaxClassService
- `content` — ContentService, file management
- `shoppingcart` — ShoppingCartService, cart calculations
- `search` — SearchService (Elasticsearch indexing and querying)
- `user` — UserService, PermissionService
- `system` — MerchantConfigurationService, ModuleConfigurationService
- `reference` — CountryService, ZoneService, LanguageService

**Repositories** (`business/repositories/`) — Spring Data JPA interfaces with custom `*RepositoryImpl` classes for complex JPQL queries.

**Integration Implementations** (`business/modules/`):
- `cms/` — Infinispan (default), AWS S3, Google Cloud Storage, Local filesystem
- `integration/payment/` — Stripe, Stripe 3DS, PayPal, Braintree, BeanStream, MoneyOrder
- `integration/shipping/` — UPS, USPS, Canada Post, custom rules-based
- `email/` — Default SMTP (FreeMarker templates), AWS SES
- `order/total/` — Promo code calculator, manufacturer-based shipping

**Configuration:**
- `DataConfiguration.java` — HikariCP pool → JPA EntityManagerFactory → Hibernate + Ehcache
- `DroolsBeanFactory.java` — Drools rules engine setup
- Spring XML configs in `resources/spring/` wire integration modules, CMS backends, cache, and processors

#### sm-shop-model (API Model Layer)
Contains all REST API data transfer objects and facade interfaces:
- Request/response models organized by domain (product, order, customer, store, etc.)
- Facade interfaces (e.g., `ProductFacade`, `OrderFacade`, `CustomerFacade`)
- Validation utilities

This module exists so that `sm-shop` and `sm-core` can share API model definitions without circular dependencies.

#### sm-shop (Web/Application Layer)
The Spring Boot application module. Entry point: `ShopApplication.java`.

**REST Controllers** (`store/api/`):
- `v1/` — Primary API version (product, order, category, customer, cart, payment, shipping, content, user, store, tax, security)
- `v2/` — Newer endpoints for products, variants, variant groups
- `v0/` — Legacy endpoints

**Facades** (`store/facade/`, `store/controller/*/facade/`):
- Orchestration layer between controllers and core services
- Handles DTO ↔ entity conversion
- Coordinates multiple services for complex operations (e.g., checkout flow)

**Mappers & Populators** (`mapper/`, `populator/`):
- `Mapper` classes (newer pattern) — implement a generic `Mapper<Source, Destination>` interface
- `Populator` classes (older pattern) — manual field-by-field conversion
- Both coexist; mappers are used in newer code

**Security** (`store/security/`, `application/config/`):
- `MultipleEntryPointsSecurityConfig` — defines separate filter chains
- `JWTTokenUtil` — token generation, validation, refresh
- `AuthenticationTokenFilter` — extracts and validates JWT from requests

### 3. Request Flow

```
Client Request
     │
     ▼
┌─────────────────────────────────────────┐
│  AuthenticationTokenFilter              │  ← JWT validation
│  (Spring Security Filter Chain)         │
└────────────────┬────────────────────────┘
                 ▼
┌─────────────────────────────────────────┐
│  REST Controller                        │  ← Route handling, input validation
│  e.g., ProductApi.java                  │
└────────────────┬────────────────────────┘
                 ▼
┌─────────────────────────────────────────┐
│  Facade                                 │  ← Orchestration, DTO ↔ Entity mapping
│  e.g., ProductFacadeImpl.java           │
└────────────────┬────────────────────────┘
                 ▼
┌─────────────────────────────────────────┐
│  Service                                │  ← Business logic, validation
│  e.g., ProductServiceImpl.java          │
└────────────────┬────────────────────────┘
                 ▼
┌─────────────────────────────────────────┐
│  Repository                             │  ← Data access (JPA/JPQL)
│  e.g., ProductRepositoryImpl.java       │
└────────────────┬────────────────────────┘
                 ▼
┌─────────────────────────────────────────┐
│  Database                               │  ← H2 (dev) / MySQL / PostgreSQL
│  Schema: SALESMANAGER                   │
└─────────────────────────────────────────┘
```

### 4. Security Architecture

Three separate security filter chains handle different API paths:

| Filter Chain | URL Pattern | Auth Type | Purpose |
|-------------|-------------|-----------|---------|
| Admin API | `/api/v*/admin/**` | JWT (JWTAdminAuthenticationManager) | Back-office operations |
| Services API | `/api/v*/auth/**` | JWT (UserApi auth) | User/service authentication |
| Customer API | `/api/v*/customer/**` | JWT (JWTCustomerAuthenticationManager) | Storefront customer operations |

All chains use `AuthenticationTokenFilter` for JWT extraction, with `BCryptPasswordEncoder` for password hashing.

### 5. Data & Caching

**Database Configuration** (`DataConfiguration.java`):
- Connection pool: HikariCP (4–8 connections, configurable per profile)
- ORM: Hibernate with `hbm2ddl.auto=update`
- Profiles: `docker` (H2), `local` (MySQL), `cloud`, `gcp`, `mysql`

**Caching (two layers):**
- Ehcache — Hibernate second-level cache for entity/query caching
- Infinispan 9.4.18 — Tree-based cache for CMS content and product images (acts as default file storage)

### 6. Integration Architecture

External integrations are pluggable via interfaces defined in `sm-core-modules` and wired through Spring XML configs:

```
shopizer-core-modules.xml     → Payment & shipping module beans
shopizer-core-cms.xml         → CMS storage backend beans
shopizer-core-ehcache.xml     → Cache configuration
processors/*-processors.xml   → Order total & shipping rule processors
```

**Payment flow:** Controller → OrderFacade → PaymentService → PaymentModule impl (Stripe/PayPal/etc.)

**CMS flow:** Controller → ContentService → ContentFileManager → Storage impl (Infinispan/S3/GCS/Local)

### 7. Event System

Spring AOP + Application Events keep the search index in sync:

```
Product CRUD operation
     │
     ▼
PublishProductAspect (AOP)
     │  intercepts save/delete on Product, ProductVariant, ProductImage, ProductAttribute
     ▼
Spring ApplicationEvent
     │
     ▼
IndexProductEventListener
     │  re-indexes the product in Elasticsearch
     ▼
SearchService → Elasticsearch
```

### 8. Rules Engine

Drools 7.32 handles dynamic business rules:
- Order totals — promo code discounts, manufacturer-based shipping surcharges
- Shipping — decision-based shipping rules, distance-based pricing

Rules can be defined in `.drl` files or Excel decision tables, loaded via `DroolsBeanFactory`.

### 9. Configuration Style

The project uses a hybrid configuration approach:
- Spring Boot auto-config — web layer, actuator, embedded server
- Java config — security, data source, web MVC
- XML config — integration modules, CMS, cache, processors (legacy Spring, imported via `shopizer-core-context.xml`)

This reflects the project's evolution from traditional Spring to Spring Boot.

---

## Entity Relationships

### Overview

MerchantStore is the central multi-tenancy entity — nearly every domain entity links back to it. The model follows an e-commerce domain with Product as the most connected entity, loose coupling between Order/Cart and Customer, and a description pattern for i18n support.

### 1. MerchantStore — The Central Tenant

```
                                ┌─────────────────────┐
                                │    MerchantStore     │
                                │  (self-ref parent/   │
                                │   children)          │
                                └──────────┬──────────┘
                                           │
       ┌───────────┬───────────┬───────────┼───────────┬───────────┬───────────┐
       ▼           ▼           ▼           ▼           ▼           ▼           ▼
    Product    Category    Customer      Order    ShoppingCart   Content      User
    Manufacturer Catalog   TaxRate    Transaction  ProductOption(Value)      Group
                                                   ProductVariation
```

MerchantStore represents a single storefront. Every major entity has a `@ManyToOne` relationship back to it, which is how Shopizer supports multi-tenancy — all queries are scoped to a specific store.

A MerchantStore can also have a parent store and child stores (self-referencing), enabling a marketplace model where a parent store manages multiple child retailers.

It holds references to the store's Country, Zone, Currency, default Language, and a set of supported Languages (via a ManyToMany join table `MERCHANT_LANGUAGE`).

### 2. Product Domain

```
                         Manufacturer ◄──ManyToOne── Product ──ManyToOne──► ProductType
                                                       │         ManyToOne──► TaxClass
                                                       │         ManyToOne──► Customer (owner)
                                                       │
                                              ManyToMany (PRODUCT_CATEGORY)
                                                       │
                                                   Category (self-ref parent/children)
                                                       │
                         ┌─────────────┬───────────────┼───────────────┬──────────────┐
                         │ OneToMany   │ OneToMany     │ OneToMany     │ OneToMany     │
                         ▼             ▼               ▼               ▼               │
                   ProductImage  ProductAttribute  ProductVariant  ProductRelationship  │
                                  │          │         │          (product↔relatedProduct)
                            ManyToOne   ManyToOne  ManyToOne(x2)                       │
                                │          │         │                                 │
                         ProductOption  ProductOptionValue  ProductVariation            │
                                                                                       │
                         Product ──OneToMany──► ProductAvailability ──OneToMany──► ProductPrice
                                                    │
                                               ManyToOne to ProductVariant
```

Product is the most connected entity in the system. Here's what each relationship does:

- **Product → Manufacturer** — each product belongs to one brand/manufacturer (e.g., Nike, Apple). Manufacturer itself belongs to a MerchantStore.
- **Product → ProductType** — classifies the product (e.g., "general", "rental"). ProductType controls behavior like whether the product can be added to cart.
- **Product → TaxClass** — determines which tax rules apply to this product during checkout.
- **Product → Customer (owner)** — optional. Used in marketplace scenarios where a customer can also be a seller who owns products.
- **Product ↔ Category (ManyToMany)** — a product can belong to multiple categories, and a category can contain multiple products. Linked via the `PRODUCT_CATEGORY` join table. Category itself is a self-referencing tree (parent/children) for nested navigation like "Electronics > Phones > Smartphones".
- **Product → ProductImage (OneToMany)** — a product has multiple images. One is marked as the default image.
- **Product → ProductAttribute (OneToMany)** — attributes represent configurable options on a product (e.g., size, color). Each attribute links to a ProductOption (the option name like "Size") and a ProductOptionValue (the specific value like "Large"). Attributes can carry additional price and weight.
- **Product → ProductVariant (OneToMany)** — variants represent specific purchasable combinations (e.g., "Red / Large"). Each variant references two ProductVariation entities (which themselves point to ProductOption + ProductOptionValue pairs). Variants have their own availability and pricing.
- **Product → ProductRelationship (OneToMany)** — links products to related/associated products (e.g., "customers also bought", "accessories"). Each relationship has a `product` and a `relatedProduct`, both pointing to Product.
- **Product → ProductAvailability (OneToMany)** — tracks inventory per region/variant. Each availability record holds quantity, status, and has its own set of ProductPrice entries. A ProductAvailability can optionally be tied to a specific ProductVariant.
- **ProductAvailability → ProductPrice (OneToMany)** — a single availability can have multiple prices (e.g., default price, sale price). Each price has a code, amount, optional discount amount, and discount date range.

### 3. Order Domain

```
  Customer ·····(customerId as Long, NOT JPA)····► Order ──ManyToOne──► Currency
                                                     │
                                    ┌────────────────┼────────────────┐
                                    │ OneToMany      │ OneToMany      │ OneToMany
                                    ▼                ▼                ▼
                              OrderProduct      OrderTotal     OrderStatusHistory
                               │    │    │
                        OneToMany  OneToMany  OneToMany
                            │         │          │
                  OrderProductPrice  OrderProductAttribute  OrderProductDownload

                              Transaction ──ManyToOne──► Order
```

The Order domain captures a completed purchase. Key design decisions:

- **Order → Customer (loose coupling)** — the `customerId` is stored as a plain Long column, NOT a JPA foreign key. This is intentional — if a customer deletes their account, the order history remains intact. The customer's billing and delivery addresses are embedded directly in the Order as snapshots at the time of purchase.
- **Order → Currency** — records which currency was used for the transaction.
- **Order → OrderProduct (OneToMany)** — each line item in the order. OrderProduct stores a snapshot of the product name, SKU, quantity, and price at the time of purchase. It does NOT reference the live Product entity — this ensures order data is immutable even if the product is later modified or deleted.
- **OrderProduct → OrderProductPrice (OneToMany)** — price breakdown for each line item (base price, special price, etc.), again stored as snapshots.
- **OrderProduct → OrderProductAttribute (OneToMany)** — captures the selected options (e.g., "Size: Large") as snapshot strings, not references to ProductOption/ProductOptionValue.
- **OrderProduct → OrderProductDownload (OneToMany)** — for digital products, tracks downloadable files associated with the order.
- **Order → OrderTotal (OneToMany)** — the order's total breakdown: subtotal, shipping, tax, discounts, grand total. Each OrderTotal has a code (e.g., "SUBTOTAL", "SHIPPING", "TAX"), a module reference, and a value.
- **Order → OrderStatusHistory (OneToMany)** — audit trail of status changes (e.g., ORDERED → PROCESSING → SHIPPED → DELIVERED) with timestamps and optional comments.
- **Transaction → Order (ManyToOne)** — payment transactions (authorize, capture, refund) linked to an order. Each transaction records the type, amount, date, and payment gateway details.

### 4. Shopping Cart

```
  Customer ·····(customerId as Long, NOT JPA)····► ShoppingCart
                                                       │
                                                  OneToMany
                                                       │
                                                       ▼
                                                 ShoppingCartItem ·····(productId as Long)
                                                       │
                                                  OneToMany
                                                       │
                                                       ▼
                                              ShoppingCartAttributeItem
```

The shopping cart is designed for flexibility and performance:

- **ShoppingCart → Customer (loose coupling)** — like Order, the `customerId` is a plain Long column. This allows anonymous/guest carts (customerId is null) and prevents issues if a customer is deleted. The cart is identified by a unique `shoppingCartCode` string, often stored in a browser cookie.
- **ShoppingCart → ShoppingCartItem (OneToMany)** — each item in the cart. The `productId` is stored as a plain Long column, and the `Product` field is marked `@Transient` — meaning it's loaded programmatically by the service layer, not by JPA joins. This keeps cart queries lightweight.
- **ShoppingCartItem → ShoppingCartAttributeItem (OneToMany)** — tracks selected product options (e.g., size, color) for each cart item. References `productAttributeId` to link back to the product's attribute configuration.
- The cart also stores `orderId` as a plain Long — set once the cart is converted into an order during checkout.

### 5. Security Model

```
  Customer ──ManyToMany (CUSTOMER_GROUP)──► Group ◄──ManyToMany (USER_GROUP)── User
                                              │
                                        ManyToMany (PERMISSION_GROUP)
                                              │
                                              ▼
                                          Permission
```

Security uses a role-based access control (RBAC) model with three join tables:

- **User → Group (ManyToMany via USER_GROUP)** — admin/back-office users are assigned to groups like "SUPERADMIN", "ADMIN", "ADMIN_CATALOGUE", etc.
- **Customer → Group (ManyToMany via CUSTOMER_GROUP)** — storefront customers are assigned to groups like "CUSTOMER".
- **Group → Permission (ManyToMany via PERMISSION_GROUP)** — each group has a set of permissions that control access to specific API endpoints and operations.

Both User and Customer share the same Group entity, but they're authenticated through separate security filter chains (Admin JWT vs Customer JWT). This allows the same permission model to apply across both back-office and storefront contexts.

### 6. Catalog

```
  Catalog ──OneToMany──► CatalogCategoryEntry ──ManyToOne──► Category
```

Catalogs provide a way to create curated collections of categories:

- **Catalog** — a named, ordered collection belonging to a MerchantStore. Can be marked as the default catalog and toggled visible/invisible.
- **CatalogCategoryEntry** — a join entity linking a Catalog to a Category. This allows the same category to appear in multiple catalogs, and catalogs to cherry-pick which categories to display (e.g., a "Summer Sale" catalog showing only seasonal categories).

### 7. Tax

```
  TaxRate (self-ref parent/children) ──ManyToOne──► TaxClass
                                       ManyToOne──► Country
                                       OneToOne ──► Zone
```

Tax configuration supports complex regional tax rules:

- **TaxRate** — defines a tax percentage for a specific Country + Zone (state/province) combination. Self-referencing parent/children allows compound taxes (e.g., a federal tax rate with provincial sub-rates that piggyback on it).
- **TaxRate → TaxClass** — groups tax rates by type (e.g., "default tax class", "digital goods tax class"). Products reference a TaxClass, and the system matches the product's TaxClass + customer's shipping address to find the applicable TaxRate.
- **TaxRate → Country / Zone** — determines the geographic scope of the tax rule.

### Key Design Patterns Summary

| Pattern | Where Used | Why |
|---------|-----------|-----|
| Loose coupling (Long ID, not FK) | Order→Customer, Cart→Customer, Cart→Product | Entities can be deleted independently without breaking references |
| Snapshot/denormalization | OrderProduct, OrderProductPrice, OrderProductAttribute | Order data is immutable — immune to future product changes |
| Self-referencing trees | Category, MerchantStore, TaxRate | Supports hierarchical navigation, store groups, compound taxes |
| i18n description entities | Product, Category, Manufacturer, Content, TaxRate, ProductOption, etc. | Each entity has a `*Description` child (OneToMany) with one record per language |
| Transient loading | ShoppingCartItem.product | Keeps JPA queries lightweight; product loaded separately by service layer |
| Shared RBAC | User and Customer both → Group → Permission | Single permission model across admin and storefront |
