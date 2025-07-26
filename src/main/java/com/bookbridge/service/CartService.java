package com.bookbridge.service;

import com.bookbridge.model.Book;
import com.bookbridge.model.CartItem;
import com.bookbridge.model.User;
import com.bookbridge.repository.BookRepository;
import com.bookbridge.repository.CartItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    @Autowired
    private CartItemRepository cartItemRepository;
    
    @Autowired
    private BookRepository bookRepository;

    public List<CartItem> getCartItems(User user) {
        return cartItemRepository.findByUser(user);
    }

    @Transactional
    public CartItem addToCart(User user, Long bookId, Integer quantity) {
        Optional<Book> bookOpt = bookRepository.findById(bookId);
        if (bookOpt.isPresent()) {
            Book book = bookOpt.get();
            
            // Check if book is available
            if (book.getStatus() != Book.BookStatus.AVAILABLE) {
                throw new IllegalStateException("Book is not available for purchase");
            }
            
            // Check if book is already in cart
            Optional<CartItem> existingItemOpt = cartItemRepository.findByUserAndBook(user, book);
            if (existingItemOpt.isPresent()) {
                CartItem existingItem = existingItemOpt.get();
                existingItem.setQuantity(existingItem.getQuantity() + quantity);
                return cartItemRepository.save(existingItem);
            } else {
                CartItem newItem = new CartItem(user, book, quantity);
                return cartItemRepository.save(newItem);
            }
        }
        throw new IllegalArgumentException("Book not found");
    }

    @Transactional
    public CartItem updateCartItemQuantity(Long cartItemId, Integer quantity) {
        Optional<CartItem> cartItemOpt = cartItemRepository.findById(cartItemId);
        if (cartItemOpt.isPresent()) {
            CartItem cartItem = cartItemOpt.get();
            cartItem.setQuantity(quantity);
            return cartItemRepository.save(cartItem);
        }
        throw new IllegalArgumentException("Cart item not found");
    }

    @Transactional
    public void removeFromCart(Long cartItemId) {
        cartItemRepository.deleteById(cartItemId);
    }

    @Transactional
    public void clearCart(User user) {
        cartItemRepository.deleteByUser(user);
    }

    public Double calculateCartTotal(User user) {
        Double total = cartItemRepository.calculateCartTotal(user);
        return total != null ? total : 0.0;
    }

    public Long countCartItems(User user) {
        return cartItemRepository.countCartItemsByUser(user);
    }
}
