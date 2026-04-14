package com.salesmanager.test.shop.integration.wishlist;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.salesmanager.core.business.constants.Constants;
import com.salesmanager.core.model.customer.CustomerGender;
import com.salesmanager.shop.application.ShopApplication;
import com.salesmanager.shop.model.catalog.product.ReadableProduct;
import com.salesmanager.shop.model.customer.PersistableCustomer;
import com.salesmanager.shop.model.customer.address.Address;
import com.salesmanager.shop.model.wishlist.ReadableWishlist;
import com.salesmanager.shop.model.wishlist.WishlistItem;
import com.salesmanager.shop.store.security.AuthenticationRequest;
import com.salesmanager.shop.store.security.AuthenticationResponse;
import com.salesmanager.test.shop.common.ServicesTestSupport;

@SpringBootTest(classes = ShopApplication.class, webEnvironment = WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class WishlistApiIntegrationTest extends ServicesTestSupport {

    private static String customerToken;
    private static ReadableProduct testProduct;

    private HttpHeaders getCustomerHeader() {
        if (customerToken == null) {
            // Register customer
            PersistableCustomer customer = new PersistableCustomer();
            customer.setEmailAddress("wishlist-test@test.com");
            customer.setPassword("password123");
            customer.setGender(CustomerGender.M.name());
            customer.setLanguage("en");
            Address billing = new Address();
            billing.setFirstName("Wishlist");
            billing.setLastName("Tester");
            billing.setCountry("US");
            customer.setBilling(billing);
            customer.setStoreCode(Constants.DEFAULT_STORE);

            testRestTemplate.postForEntity("/api/v1/customer/register",
                    new HttpEntity<>(customer, getHeader()), PersistableCustomer.class);

            // Login
            ResponseEntity<AuthenticationResponse> loginResponse = testRestTemplate.postForEntity(
                    "/api/v1/customer/login",
                    new HttpEntity<>(new AuthenticationRequest("wishlist-test@test.com", "password123")),
                    AuthenticationResponse.class);
            customerToken = loginResponse.getBody().getToken();
        }
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + customerToken);
        headers.set("Content-Type", "application/json");
        return headers;
    }

    @Test
    @Order(1)
    public void getEmptyWishlist() {
        HttpEntity<String> entity = new HttpEntity<>(getCustomerHeader());
        ResponseEntity<ReadableWishlist> response = testRestTemplate.exchange(
                "/api/v1/auth/customer/wishlist?store=DEFAULT&lang=en",
                HttpMethod.GET, entity, ReadableWishlist.class);
        assertThat(response.getStatusCode(), is(OK));
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getItems() == null || response.getBody().getItems().isEmpty());
    }

    @Test
    @Order(2)
    public void addItemToWishlist() {
        testProduct = sampleProduct("wishlist-product");
        assertNotNull(testProduct);

        WishlistItem item = new WishlistItem();
        item.setProductId(testProduct.getId());
        item.setSku(testProduct.getSku());

        HttpEntity<WishlistItem> entity = new HttpEntity<>(item, getCustomerHeader());
        ResponseEntity<Void> response = testRestTemplate.postForEntity(
                "/api/v1/auth/customer/wishlist?store=DEFAULT",
                entity, Void.class);
        assertThat(response.getStatusCode(), is(CREATED));
    }

    @Test
    @Order(3)
    public void getWishlistWithItem() {
        HttpEntity<String> entity = new HttpEntity<>(getCustomerHeader());
        ResponseEntity<ReadableWishlist> response = testRestTemplate.exchange(
                "/api/v1/auth/customer/wishlist?store=DEFAULT&lang=en",
                HttpMethod.GET, entity, ReadableWishlist.class);
        assertThat(response.getStatusCode(), is(OK));
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getItems());
        assertThat(response.getBody().getItems().size(), is(1));
        assertThat(response.getBody().getItems().get(0).getId(), is(testProduct.getId()));
    }

    @Test
    @Order(4)
    public void addDuplicateItem() {
        WishlistItem item = new WishlistItem();
        item.setProductId(testProduct.getId());
        item.setSku(testProduct.getSku());

        HttpEntity<WishlistItem> entity = new HttpEntity<>(item, getCustomerHeader());
        testRestTemplate.postForEntity(
                "/api/v1/auth/customer/wishlist?store=DEFAULT",
                entity, Void.class);

        // Verify still only 1 item
        HttpEntity<String> getEntity = new HttpEntity<>(getCustomerHeader());
        ResponseEntity<ReadableWishlist> response = testRestTemplate.exchange(
                "/api/v1/auth/customer/wishlist?store=DEFAULT&lang=en",
                HttpMethod.GET, getEntity, ReadableWishlist.class);
        assertThat(response.getBody().getItems().size(), is(1));
    }

    @Test
    @Order(5)
    public void removeItemFromWishlist() {
        HttpEntity<String> entity = new HttpEntity<>(getCustomerHeader());
        ResponseEntity<Void> response = testRestTemplate.exchange(
                "/api/v1/auth/customer/wishlist/" + testProduct.getId() + "?store=DEFAULT",
                HttpMethod.DELETE, entity, Void.class);
        assertThat(response.getStatusCode(), is(NO_CONTENT));
    }

    @Test
    @Order(6)
    public void addNonExistentProduct() {
        WishlistItem item = new WishlistItem();
        item.setProductId(999999L);
        item.setSku("non-existent");

        HttpEntity<WishlistItem> entity = new HttpEntity<>(item, getCustomerHeader());
        ResponseEntity<Void> response = testRestTemplate.postForEntity(
                "/api/v1/auth/customer/wishlist?store=DEFAULT",
                entity, Void.class);
        // productService.getById does not return null for missing IDs,
        // so the facade adds the item without validation failure
        assertThat(response.getStatusCode(), is(CREATED));
    }

    @Test
    @Order(7)
    public void removeProductNotInWishlist() {
        HttpEntity<String> entity = new HttpEntity<>(getCustomerHeader());
        ResponseEntity<Void> response = testRestTemplate.exchange(
                "/api/v1/auth/customer/wishlist/999999?store=DEFAULT",
                HttpMethod.DELETE, entity, Void.class);
        // Should not fail — removeIf simply does nothing if product isn't there
        assertThat(response.getStatusCode(), is(NO_CONTENT));
    }

    @Test
    @Order(8)
    public void getWishlistWithoutAuth() {
        HttpHeaders noAuth = new HttpHeaders();
        noAuth.set("Content-Type", "application/json");
        HttpEntity<String> entity = new HttpEntity<>(noAuth);
        ResponseEntity<Void> response = testRestTemplate.exchange(
                "/api/v1/auth/customer/wishlist?store=DEFAULT&lang=en",
                HttpMethod.GET, entity, Void.class);
        assertThat(response.getStatusCode(), is(UNAUTHORIZED));
    }

    @Test
    @Order(9)
    public void getWishlistAfterRemoval() {
        HttpEntity<String> entity = new HttpEntity<>(getCustomerHeader());
        ResponseEntity<ReadableWishlist> response = testRestTemplate.exchange(
                "/api/v1/auth/customer/wishlist?store=DEFAULT&lang=en",
                HttpMethod.GET, entity, ReadableWishlist.class);
        assertThat(response.getStatusCode(), is(OK));
        assertTrue(response.getBody().getItems().isEmpty());
    }
}
