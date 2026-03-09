package com.example.e_commerce.cart.service;

import com.example.e_commerce.cart.CartMapper;
import com.example.e_commerce.cart.dto.CartItemRequestDto;
import com.example.e_commerce.cart.dto.CartResponseDto;
import com.example.e_commerce.cart.entity.Cart;
import com.example.e_commerce.cart.entity.CartItem;
import com.example.e_commerce.cart.repo.CartRepository;
import com.example.e_commerce.product.entity.Product;
import com.example.e_commerce.product.repo.ProductRepository;
import com.example.e_commerce.user.repo.UserRepository;
import com.example.e_commerce.user.entity.User;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CartServiceImp implements CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CartMapper cartMapper;

    @Override
    @Transactional(readOnly = true)
    public CartResponseDto getCartByUserId(Long userId) {
        Cart cart = getOrCreateCart(userId);
        return cartMapper.toResponseDto(cart);
    }

    @Override
    public CartResponseDto addItemToCart(Long userId, CartItemRequestDto cartItemRequestDto) {
        Cart cart = getOrCreateCart(userId);

        Product product = productRepository.findById(cartItemRequestDto.getProductId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Product not found with id: " + cartItemRequestDto.getProductId()));

        // ckeck if item already is in the cart
        CartItem existingItem = cart.getCartItems().stream()
                .filter(item -> item.getProduct().getId().equals(cartItemRequestDto.getProductId()))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + cartItemRequestDto.getQuantity());
            log.info("Cart item quantity updated: userId={}, productId={}, newQty={}",
                    userId, cartItemRequestDto.getProductId(), existingItem.getQuantity());
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(cartItemRequestDto.getQuantity())
                    .build();
            cart.getCartItems().add(newItem);
            log.info("Item added to cart: userId={}, productId={}, qty={}",
                    userId, cartItemRequestDto.getProductId(), cartItemRequestDto.getQuantity());
        }

        recalculate(cart);
        Cart savedCart = cartRepository.save(cart);
        return cartMapper.toResponseDto(savedCart);
    }

    @Override
    public CartResponseDto updateCartItem(Long userId, Long itemId, Integer quantity) {
        Cart cart = getOrCreateCart(userId);

        CartItem cartItem = cart.getCartItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Cart item not found with id: " + itemId));

        cartItem.setQuantity(quantity);
        recalculate(cart);
        Cart savedCart = cartRepository.save(cart);
        return cartMapper.toResponseDto(savedCart);
    }

    @Override
    public void removeItemFromCart(Long userId, Long itemId) {
        Cart cart = getOrCreateCart(userId);
        cart.getCartItems().removeIf(item -> item.getId().equals(itemId));
        recalculate(cart);
        cartRepository.save(cart);
        log.info("Cart item removed: userId={}, itemId={}", userId, itemId);
    }

    @Override
    public void clearCart(Long userId) {
        Cart cart = getOrCreateCart(userId);
        cart.getCartItems().clear();
        recalculate(cart);
        cartRepository.save(cart);
    }

    private void recalculate(Cart cart) {
        if (cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
            cart.setTotalPrice(BigDecimal.ZERO);
            return;
        }
        cart.getCartItems().forEach(item -> item.setSubtotal(item.getProduct().getPrice()
                .multiply(BigDecimal.valueOf(item.getQuantity()))));
        BigDecimal total = cart.getCartItems().stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        cart.setTotalPrice(total);
    }

    private Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
                    Cart newCart = Cart.builder()
                            .user(user)
                            .cartItems(new ArrayList<>())
                            .totalPrice(BigDecimal.ZERO)
                            .build();
                    Cart savedCart = cartRepository.save(newCart);
                    log.info("New cart created for userId={}", userId);
                    return savedCart;
                });
    }
}
