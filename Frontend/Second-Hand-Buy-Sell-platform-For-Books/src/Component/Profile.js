import React, { useEffect, useState } from 'react';
import Navbar from './Navbar';
import Footer from './Footer';
import './HomePage.css';
import { fetchBooksByUser, deleteBook, updateBook } from '../services/api';
import { toast } from 'react-toastify';

const Profile = () => {
  const [user] = useState(() => {
    try {
      return JSON.parse(localStorage.getItem('user'));
    } catch (e) {
      return null;
    }
  });

  const [userBooks, setUserBooks] = useState([]);
  const [loading, setLoading] = useState(false);
  const [editModalOpen, setEditModalOpen] = useState(false);
  const [editBook, setEditBook] = useState(null);
  const [editForm, setEditForm] = useState({});

  useEffect(() => {
    const loadBooks = async () => {
      if (user && user.userId) {
        setLoading(true);
        try {
          const books = await fetchBooksByUser(user.userId);
          setUserBooks(books);
        } catch (e) {
          setUserBooks([]);
        } finally {
          setLoading(false);
        }
      }
    };
    loadBooks();
  }, [user]);

  // Delete handler
  const handleDelete = async (bookId) => {
    if (!window.confirm('Are you sure you want to delete this book?')) return;
    try {
      const token = user?.token;
      await deleteBook(bookId, token);
      setUserBooks(userBooks.filter(b => b.id !== bookId));
      toast.success('Book deleted successfully!');
    } catch (err) {
      toast.error('Failed to delete book: ' + err.message);
    }
  };

  // Edit handlers
  const openEditModal = (book) => {
    setEditBook(book);
    setEditForm({
      title: book.title || '',
      author: book.author || '',
      description: book.description || '',
      price: book.price || '',
      condition: book.condition || '',
      category: book.category || '',
      location: book.location || '',
      isbn: book.isbn || '',
      edition: book.edition || '',
      listingType: book.listingType || '',
    });
    setEditModalOpen(true);
  };

  const closeEditModal = () => {
    setEditModalOpen(false);
    setEditBook(null);
    setEditForm({});
  };

  const handleEditChange = (e) => {
    const { name, value } = e.target;
    setEditForm(prev => ({ ...prev, [name]: value }));
  };

  const handleEditSubmit = async (e) => {
    e.preventDefault();
    if (!editBook) return;
    try {
      const token = user?.token;
      console.log('Token used for update:', token);
      const data = new FormData();
      Object.entries(editForm).forEach(([key, value]) => data.append(key, value));
      // Optionally handle image update here
      const updated = await updateBook(editBook.id, data, token);
      setUserBooks(userBooks.map(b => b.id === editBook.id ? { ...b, ...editForm } : b));
      toast.success('Book updated successfully!');
      closeEditModal();
    } catch (err) {
      toast.error('Failed to update book: ' + err.message);
    }
  };

  return (
    <div>
      <Navbar />
      <div style={{ maxWidth: 400, margin: '40px auto', background: '#fff', borderRadius: 12, padding: 32, boxShadow: '0 4px 16px rgba(0,0,0,0.08)' }}>
        <h2 style={{ textAlign: 'center', marginBottom: 24 }}>User Profile</h2>
        {user ? (
          <div style={{ textAlign: 'center' }}>
            <div style={{ fontSize: 80, marginBottom: 16 }}>👤</div>
            <div style={{ fontSize: 20, fontWeight: 600, marginBottom: 8 }}>{user.name || user.fullName || user.email}</div>
            {user.email && <div style={{ color: '#888', marginBottom: 16 }}>{user.email}</div>}
          </div>
        ) : (
          <div style={{ textAlign: 'center', color: '#d32f2f' }}>
            <p>You are not logged in.</p>
            <a href="/login" style={{ color: '#1976d2', textDecoration: 'underline' }}>Login</a>
          </div>
        )}
      </div>
      {/* User's listed books */}
      {user && (
        <div style={{ maxWidth: 800, margin: '32px auto', background: '#fff', borderRadius: 12, padding: 32, boxShadow: '0 4px 16px rgba(0,0,0,0.08)' }}>
          <h3 style={{ marginBottom: 20 }}>Books You've Listed</h3>
          {loading ? (
            <div>Loading your books...</div>
          ) : userBooks.length === 0 ? (
            <div style={{ color: '#888' }}>You haven't listed any books yet.</div>
          ) : (
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))', gap: 20 }}>
              {userBooks.map(book => {
                const imgSrc =
                  book.bookImage
                    ? book.bookImage.startsWith('http')
                      ? book.bookImage
                      : `http://localhost:8080/${book.bookImage.replace(/^\/?/, '')}`
                    : book.imageUrl
                      ? book.imageUrl
                      : book.image
                        ? book.image
                        : 'https://via.placeholder.com/120x120?text=No+Image';
                return (
                  <div key={book.id} style={{
                    border: '1px solid #eee',
                    borderRadius: 12,
                    padding: 20,
                    background: '#fafbfc',
                    textAlign: 'center',
                    boxShadow: '0 2px 8px rgba(46,139,87,0.07)',
                    display: 'flex',
                    flexDirection: 'column',
                    alignItems: 'center',
                    position: 'relative',
                    minHeight: 320
                  }}>
                    <div style={{ fontWeight: 600, fontSize: 18, marginBottom: 8, color: '#222', textAlign: 'center', width: '100%' }}>{book.title}</div>
                    <img
                      src={imgSrc}
                      alt={book.title}
                      style={{ width: 120, height: 120, objectFit: 'cover', borderRadius: 8, marginBottom: 12, border: '1px solid #e0e0e0', background: '#fff' }}
                    />
                    <div style={{ fontWeight: 700, fontSize: 16, marginBottom: 4 }}>{book.title}</div>
                    <div style={{ color: '#666', fontSize: 14, marginBottom: 4 }}>{book.author}</div>
                    <div style={{ color: '#444', fontSize: 13, marginBottom: 8 }}>{book.description}</div>
                    <div style={{ color: '#2E8B57', fontWeight: 500, marginBottom: 12 }}>Rs. {book.price}</div>
                    <div style={{ display: 'flex', gap: 10, justifyContent: 'center', marginTop: 'auto' }}>
                      <button
                        style={{ background: '#2E8B57', color: '#fff', border: 'none', borderRadius: 6, padding: '6px 16px', fontWeight: 600, cursor: 'pointer', transition: 'background 0.2s' }}
                        onClick={() => openEditModal(book)}
                      >Edit</button>
                      <button
                        style={{ background: '#fff', color: '#d32f2f', border: '1px solid #d32f2f', borderRadius: 6, padding: '6px 16px', fontWeight: 600, cursor: 'pointer', transition: 'background 0.2s' }}
                        onClick={() => handleDelete(book.id)}
                      >Delete</button>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>
      )}
      {/* Edit Modal */}
      {editModalOpen && (
        <div style={{
          position: 'fixed', top: 0, left: 0, width: '100vw', height: '100vh', background: 'rgba(0,0,0,0.25)', zIndex: 1000,
          display: 'flex', alignItems: 'center', justifyContent: 'center'
        }}>
          <div style={{ background: '#fff', borderRadius: 12, padding: 32, minWidth: 320, maxWidth: 400, boxShadow: '0 4px 16px rgba(0,0,0,0.15)' }}>
            <h3 style={{ marginBottom: 16 }}>Edit Book</h3>
            <form onSubmit={handleEditSubmit}>
              <div style={{ marginBottom: 12 }}>
                <input name="title" value={editForm.title} onChange={handleEditChange} placeholder="Title" style={{ width: '100%', padding: 8, borderRadius: 6, border: '1px solid #ccc' }} required />
              </div>
              <div style={{ marginBottom: 12 }}>
                <input name="author" value={editForm.author} onChange={handleEditChange} placeholder="Author" style={{ width: '100%', padding: 8, borderRadius: 6, border: '1px solid #ccc' }} required />
              </div>
              <div style={{ marginBottom: 12 }}>
                <textarea name="description" value={editForm.description} onChange={handleEditChange} placeholder="Description" style={{ width: '100%', padding: 8, borderRadius: 6, border: '1px solid #ccc' }} rows={3} required />
              </div>
              <div style={{ marginBottom: 12 }}>
                <input name="price" value={editForm.price} onChange={handleEditChange} placeholder="Price" type="number" style={{ width: '100%', padding: 8, borderRadius: 6, border: '1px solid #ccc' }} required />
              </div>
              <div style={{ marginBottom: 12 }}>
                <select name="condition" value={editForm.condition} onChange={handleEditChange} style={{ width: '100%', padding: 8, borderRadius: 6, border: '1px solid #ccc' }} required>
                  <option value="">Select Condition</option>
                  <option value="EXCELLENT">Excellent</option>
                  <option value="GOOD">Good</option>
                  <option value="FAIR">Fair</option>
                  <option value="POOR">Poor</option>
                </select>
              </div>
              <div style={{ marginBottom: 12 }}>
                <select name="category" value={editForm.category} onChange={handleEditChange} style={{ width: '100%', padding: 8, borderRadius: 6, border: '1px solid #ccc' }} required>
                  <option value="">Select Category</option>
                  <option value="TECHNOLOGY">Technology</option>
                  <option value="FICTION">Fiction</option>
                  <option value="NONFICTION">Nonfiction</option>
                  <option value="SCIENCE">Science</option>
                  <option value="HISTORY">History</option>
                  <option value="OTHER">Other</option>
                </select>
              </div>
              <div style={{ marginBottom: 12 }}>
                <input name="location" value={editForm.location} onChange={handleEditChange} placeholder="Location" style={{ width: '100%', padding: 8, borderRadius: 6, border: '1px solid #ccc' }} required />
              </div>
              <div style={{ marginBottom: 12 }}>
                <input name="isbn" value={editForm.isbn} onChange={handleEditChange} placeholder="ISBN" style={{ width: '100%', padding: 8, borderRadius: 6, border: '1px solid #ccc' }} />
              </div>
              <div style={{ marginBottom: 12 }}>
                <input name="edition" value={editForm.edition} onChange={handleEditChange} placeholder="Edition" style={{ width: '100%', padding: 8, borderRadius: 6, border: '1px solid #ccc' }} />
              </div>
              <div style={{ marginBottom: 12 }}>
                <select name="listingType" value={editForm.listingType} onChange={handleEditChange} style={{ width: '100%', padding: 8, borderRadius: 6, border: '1px solid #ccc' }} required>
                  <option value="">Select Listing Type</option>
                  <option value="SELL">Sell</option>
                  <option value="RENT">Rent</option>
                </select>
              </div>
              <div style={{ display: 'flex', gap: 10, justifyContent: 'flex-end', marginTop: 16 }}>
                <button type="button" onClick={closeEditModal} style={{ background: '#eee', color: '#333', border: 'none', borderRadius: 6, padding: '6px 16px', fontWeight: 600, cursor: 'pointer' }}>Cancel</button>
                <button type="submit" style={{ background: '#2E8B57', color: '#fff', border: 'none', borderRadius: 6, padding: '6px 16px', fontWeight: 600, cursor: 'pointer' }}>Save</button>
              </div>
            </form>
          </div>
        </div>
      )}
      <Footer />
    </div>
  );
};

export default Profile; 