package com.salesmanager.shop.store.api.v1.wishlist;

import com.salesmanager.core.model.customer.Customer;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.shop.model.wishlist.ReadableWishlist;
import com.salesmanager.shop.model.wishlist.WishlistItem;
import com.salesmanager.shop.store.api.exception.UnauthorizedException;
import com.salesmanager.shop.store.controller.customer.facade.CustomerFacade;
import com.salesmanager.shop.store.controller.wishlist.facade.WishlistFacade;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1")
@Api(tags = { "Wishlist management resource" })
@SwaggerDefinition(tags = { @Tag(name = "Wishlist management resource", description = "Manage customer wishlists") })
public class WishlistApi {

    @Inject
    private WishlistFacade wishlistFacade;

    @Inject
    private CustomerFacade customerFacade;

    @GetMapping("/auth/customer/wishlist")
    @ApiOperation(httpMethod = "GET", value = "Get authenticated customer wishlist", produces = "application/json", response = ReadableWishlist.class)
    @ApiImplicitParams({ @ApiImplicitParam(name = "store", dataType = "string", defaultValue = "DEFAULT"),
            @ApiImplicitParam(name = "lang", dataType = "string", defaultValue = "en") })
    public ReadableWishlist get(@ApiIgnore MerchantStore merchantStore, @ApiIgnore Language language,
            HttpServletRequest request) {
        Customer customer = getAuthCustomer(merchantStore, request);
        return wishlistFacade.getWishlist(customer, merchantStore, language);
    }

    @PostMapping("/auth/customer/wishlist")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation(httpMethod = "POST", value = "Add item to wishlist", produces = "application/json")
    @ApiImplicitParams({ @ApiImplicitParam(name = "store", dataType = "string", defaultValue = "DEFAULT") })
    public void add(@Valid @RequestBody WishlistItem item, @ApiIgnore MerchantStore merchantStore,
            HttpServletRequest request) {
        Customer customer = getAuthCustomer(merchantStore, request);
        wishlistFacade.addToWishlist(item, customer, merchantStore);
    }

    @DeleteMapping("/auth/customer/wishlist/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiOperation(httpMethod = "DELETE", value = "Remove item from wishlist")
    @ApiImplicitParams({ @ApiImplicitParam(name = "store", dataType = "string", defaultValue = "DEFAULT") })
    public void remove(@PathVariable Long productId, @ApiIgnore MerchantStore merchantStore,
            HttpServletRequest request) {
        Customer customer = getAuthCustomer(merchantStore, request);
        wishlistFacade.removeFromWishlist(productId, customer, merchantStore);
    }

    private Customer getAuthCustomer(MerchantStore merchantStore, HttpServletRequest request) {
        String userName = request.getUserPrincipal().getName();
        Customer customer = customerFacade.getCustomerByNick(userName, merchantStore);
        if (customer == null) {
            throw new UnauthorizedException();
        }
        return customer;
    }
}
