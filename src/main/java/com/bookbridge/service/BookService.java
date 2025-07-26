package com.bookbridge.service;

import com.bookbridge.model.Book;
import com.bookbridge.model.User;
import com.bookbridge.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class BookService {

    @Autowired
    private BookRepository bookRepository;

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public Page<Book> getAvailableBooks(Pageable pageable) {
        return bookRepository.findAvailableBooks(pageable);
    }

    public Optional<Book> getBookById(Long id) {
        return bookRepository.findById(id);
    }

    public List<Book> getBooksByUser(User user) {
        return bookRepository.findByUser(user);
    }

    @Transactional
    public Book createBook(Book book) {
        return bookRepository.save(book);
    }

    @Transactional
    public Book updateBook(Book book) {
        return bookRepository.save(book);
    }

    @Transactional
    public boolean deleteBook(Long id) {
        Optional<Book> bookOpt = bookRepository.findById(id);
        if (bookOpt.isPresent()) {
            Book book = bookOpt.get();
            book.setStatus(Book.BookStatus.DELETED);
            bookRepository.save(book);
            return true;
        }
        return false;
    }

    @Transactional
    public boolean hardDeleteBook(Long id) {
        if (bookRepository.existsById(id)) {
            bookRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public Page<Book> searchBooks(String keyword, Pageable pageable) {
        return bookRepository.searchBooks(keyword, pageable);
    }

    public Page<Book> findByCategory(Book.BookCategory category, Pageable pageable) {
        return bookRepository.findByCategory(category, pageable);
    }

    public Page<Book> findByCondition(Book.BookCondition condition, Pageable pageable) {
        return bookRepository.findByCondition(condition, pageable);
    }

    public Page<Book> findByListingType(Book.ListingType listingType, Pageable pageable) {
        return bookRepository.findByListingType(listingType, pageable);
    }

    public Page<Book> findByLocation(String location, Pageable pageable) {
        return bookRepository.findByLocation(location, pageable);
    }

    public Page<Book> searchBooksWithFilters(String keyword, Book.BookCategory category, 
                                           Book.BookCondition condition, Book.ListingType listingType, 
                                           String location, Pageable pageable) {
        return bookRepository.searchBooksWithFilters(keyword, category, condition, listingType, location, pageable);
    }

    @Transactional
    public void incrementViewCount(Long bookId) {
        Optional<Book> bookOpt = bookRepository.findById(bookId);
        if (bookOpt.isPresent()) {
            Book book = bookOpt.get();
            book.incrementViewCount();
            bookRepository.save(book);
        }
    }

    @Transactional
    public void incrementInterestCount(Long bookId) {
        Optional<Book> bookOpt = bookRepository.findById(bookId);
        if (bookOpt.isPresent()) {
            Book book = bookOpt.get();
            book.incrementInterestCount();
            bookRepository.save(book);
        }
    }

    @Transactional
    public boolean updateBookStatus(Long bookId, Book.BookStatus status) {
        Optional<Book> bookOpt = bookRepository.findById(bookId);
        if (bookOpt.isPresent()) {
            Book book = bookOpt.get();
            book.setStatus(status);
            bookRepository.save(book);
            return true;
        }
        return false;
    }

    public Long countBooksCreatedAfter(LocalDateTime date) {
        return bookRepository.countBooksCreatedAfter(date);
    }

    public Long countBooksByStatus(Book.BookStatus status) {
        return bookRepository.countBooksByStatus(status);
    }
}
