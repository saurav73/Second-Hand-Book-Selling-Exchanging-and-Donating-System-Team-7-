import React, { useState, useEffect } from 'react';
import Navbar from './Navbar';
import Footer from './Footer';
import './HomePage.css';
import { fetchBooks } from '../services/api';

const Shop = () => {
  const [books, setBooks] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const getBooks = async () => {
      setLoading(true);
      try {
        const data = await fetchBooks();
        setBooks(data.content || data);
      } catch (error) {
        setBooks([]);
      } finally {
        setLoading(false);
      }
    };
    getBooks();
  }, []);

  return (
    <div className="shop-page">
      <Navbar />
      <main>
  <div className="shop-header-center">
    <h1>Explore All Books Here</h1>
    <div className="filters">
      <label className="filter-option">
        <input type="checkbox" checked readOnly />
        <span>All</span>
      </label>
      <label className="filter-option">
        <input type="checkbox" readOnly />
        <span>Novel</span>
      </label>
      <label className="filter-option">
        <input type="checkbox" readOnly />
        <span>Translations</span>
      </label>
      <label className="filter-option">
        <input type="checkbox" readOnly />
        <span>Kids' Stories</span>
      </label>
    </div>
  </div>
        <div className="book-grid">
          {loading ? (
            <div>Loading books...</div>
          ) : books.length === 0 ? (
            <div>No books found.</div>
          ) : (
            books.map((book, idx) => (
              <div className="book-card" key={book.id || idx}>
            <img
                  src={book.imageUrl || book.image}
                  alt={book.title}
              className="book-image"
            />
                <h3>{book.title}</h3>
                <p className="author">{book.author}</p>
                <p className="price">Rs. {book.price}/-</p>
            <button className="add-to-cart">
              <span className="cart-icon">🛒</span> Add to Cart
            </button>
          </div>
            ))
          )}
        </div>
      </main>
      <Footer />
    </div>
  );
};

export default Shop;