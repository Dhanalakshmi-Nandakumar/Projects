 package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.Cart;
import com.ecommerce.project.model.CartItem;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.model.User;
import com.ecommerce.project.payload.CartDTO;
import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.repositories.CartItemRepository;
import com.ecommerce.project.repositories.CartRepository;
import com.ecommerce.project.repositories.ProductRepository;
import com.ecommerce.project.util.AuthUtil;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public CartDTO addProductToCart(Long productId, Integer quantity) {

        Cart cart = createCart();

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product", "productId", productId));

        CartItem cartItem = cartItemRepository
                .findCartItemByProductIdAndCartId(cart.getCartId(), productId);

        if (cartItem != null) {
            throw new APIException(
                    "Product " + product.getProductName() + " Already Exist in the Cart "
            );
        }

        if (product.getQuantity() == 0) {
            throw new APIException(
                    product.getProductName() + " is Currently out of stock "
            );
        }

        if (product.getQuantity() < quantity) {
            throw new APIException(
                    "Please make an order of the " + product.getProductName()
                            + " less than or equal to Quantity : "
                            + product.getQuantity() + " ."
            );
        }
      //add new product to the cart
        CartItem newCartItem = new CartItem();
        newCartItem.setProduct(product);
        newCartItem.setCart(cart);
        newCartItem.setQuantity(quantity);
        newCartItem.setDicount(product.getDiscount());
        newCartItem.setProductPrice(product.getSpecialPrice());

// Add the new CartItem to the Cart's collection so the Cart contains the newly added item
        cart.getCartItemList().add(newCartItem);

        cartItemRepository.save(newCartItem);

        product.setQuantity(product.getQuantity() - quantity);
        productRepository.save(product);

        cart.setTotalPrice(
                cart.getTotalPrice() + product.getSpecialPrice() * quantity
        );

        cartRepository.save(cart);


        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

        List<CartItem> cartItems = cart.getCartItemList();

        Stream<ProductDTO> productDTOStream = cartItems.stream()
                .map(item -> {

                    ProductDTO productDTO = modelMapper.map(
                            item.getProduct(),
                            ProductDTO.class
                    );

                    productDTO.setQuantity(item.getQuantity());

                    return productDTO;
                });

        cartDTO.setProducts(productDTOStream.toList());

        return cartDTO;
    }

    @Override
    public List<CartDTO> getAllCarts() {
        List<Cart> carts=cartRepository.findAll();
        if(carts.isEmpty())
        {
            throw new APIException("No card exist !");
        }
        List<CartDTO> cartDTOS=carts.stream()
                .map(cart->{

                    CartDTO cartDTO=modelMapper.map(cart, CartDTO.class);
                    //set quantity for every product or cart item
                    cart.getCartItemList()
                            .forEach(p->p.getProduct().setQuantity(p.getQuantity()));
                    List<ProductDTO> productDTOS=cart.getCartItemList().stream()
                            .map(cartItem->modelMapper.map(cartItem.getProduct(), ProductDTO.class))
                            .toList();

                    cartDTO.setProducts(productDTOS);
                    //It returns one CartDTO for one Cart.
                    return cartDTO;
                }).toList();

        return cartDTOS;
    }

    @Override
    public CartDTO getCart(String emailId, Long cartId) {
        Cart cart=cartRepository.findCartByEmailIdAndCartId(emailId,cartId);
        if(cart==null)
        {
            throw new ResourceNotFoundException("Cart","cartId",cartId);

        }
        CartDTO cartDTO=modelMapper.map(cart,CartDTO.class);
        cart.getCartItemList().forEach(c->c.getProduct().setQuantity(c.getQuantity()));
        List<ProductDTO> productDTO=cart.getCartItemList().stream()
                .map(cartItem -> modelMapper.map(cartItem.getProduct(), ProductDTO.class))
                .collect(Collectors.toList());
        cartDTO.setProducts(productDTO);

        return cartDTO;
    }

    @Override
    @Transactional
    public CartDTO updateProductQuantityInCart(Long productId, Integer quantity) {
        String email=authUtil.loggedInEmail();
        Cart cart=cartRepository.findCartByEmail(email);
        Long cartId=cart.getCartId();
        Cart cart1=cartRepository.findById(cartId)
        .orElseThrow(()->new ResourceNotFoundException("Cart","cartId",cartId));
        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product", "productId", productId));
        if (product.getQuantity() == 0) {
            throw new APIException(
                    product.getProductName() + " is Currently out of stock "
            );
        }
        if (product.getQuantity() < quantity) {
            throw new APIException(
                    "Please make an order of the " + product.getProductName()
                            + " less than or equal to Quantity : "
                            + product.getQuantity() + " ."
            );
        }
        CartItem cartItem=cartItemRepository.findCartItemByProductIdAndCartId(productId,cartId);
        if(cartItem==null)
        {
            throw new APIException("Product"+product.getProductName()+" is not available in the cart ");
        }
        //Calculate new quantity
        int newQuantity=cartItem.getQuantity()+quantity;
        if(newQuantity<0)
        {
            throw new APIException("Resulting quantity cannot be negative ");
        }

        if(newQuantity==0)
        {
            deleteProductFromCart(cartId,productId);
        }
        else{
            cartItem.setProductPrice(product.getSpecialPrice());
            cartItem.setQuantity(cartItem.getQuantity()+quantity);
            cartItem.setDicount(product.getDiscount());
            cart1.setTotalPrice(cart1.getTotalPrice()+(cartItem.getProductPrice()*quantity));
            cartRepository.save(cart1);
        }

        CartItem updatedCartItem=cartItemRepository.save(cartItem);
        if(updatedCartItem.getQuantity()==0){
            cartItemRepository.deleteById(cartItem.getCartItemId());
        }
        CartDTO cartDTO=modelMapper.map(cart1, CartDTO.class);
         List<CartItem> cartItems=cart1.getCartItemList();
         //Converting a Product entity into a ProductDTO.
         Stream<ProductDTO> productDTOStream=cartItems.stream().map(
                 cartItem1->{
                     ProductDTO prd = modelMapper.map(cartItem1.getProduct(), ProductDTO.class);
                     //sets the quantity belonging to the cart item.
                     prd.setQuantity(cartItem1.getQuantity());
                     return prd;
                 });
         cartDTO.setProducts(productDTOStream.toList());
         return cartDTO;
    }

    @Transactional
    @Override
    public String deleteProductFromCart(Long cartId, Long productId) {
        Cart cart=cartRepository.findById(cartId)
                .orElseThrow(()->new ResourceNotFoundException("Cart","cartId",cartId));
        CartItem cartItem=cartItemRepository.findCartItemByProductIdAndCartId(cartId,productId);
        if(cartItem==null)
        {
            throw new ResourceNotFoundException("Product","productId",productId);
        }
        cart.setTotalPrice(cart.getTotalPrice()-(cartItem.getProductPrice()*cartItem.getQuantity()));
        cartItemRepository.deleteCartItemByProductIdAndCartId(cartId,productId);
        return "Product : "+cartItem.getProduct().getProductName()+" removed from the cart";
    }

    private Cart createCart() {

        Cart existingUserCart = cartRepository.findCartByEmail(
                authUtil.loggedInEmail()
        );

        if (existingUserCart != null) {
            return existingUserCart;
        }

        Cart cart = new Cart();
        cart.setTotalPrice(0.00);
        cart.setUser(authUtil.loggedInUser());

        Cart newCart = cartRepository.save(cart);

        return newCart;
    }
}
