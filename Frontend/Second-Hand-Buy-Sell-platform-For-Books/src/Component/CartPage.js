import React from 'react';
import Navbar from './Navbar';
import Footer from './Footer';
import { useCart } from './CartContext';
import './HomePage.css';
import { toast } from 'react-toastify';

const CartPage = () => {
  const { cart, updateQuantity, removeFromCart } = useCart();

  const total = cart.reduce((sum, item) => sum + item.price * item.quantity, 0);

  const handleRemove = (id) => {
    removeFromCart(id);
    toast.info('Book removed from cart.');
  };

  return (
    <div className="cart-page">
      <Navbar />
      <main style={{ maxWidth: 800, margin: '40px auto', background: '#fff', borderRadius: 12, padding: 32 }}>
        <h2 style={{ textAlign: 'center', marginBottom: 32 }}>Your Cart</h2>
        {cart.length === 0 ? (
          <p style={{ textAlign: 'center' }}>Your cart is empty.</p>
        ) : (
          <table style={{ width: '100%', borderCollapse: 'collapse' }}>
            <thead>
              <tr style={{ borderBottom: '2px solid #eee' }}>
                <th style={{ textAlign: 'left', padding: 8 }}>Book</th>
                <th style={{ textAlign: 'left', padding: 8 }}>Author</th>
                <th style={{ textAlign: 'center', padding: 8 }}>Quantity</th>
                <th style={{ textAlign: 'right', padding: 8 }}>Price</th>
                <th style={{ textAlign: 'center', padding: 8 }}>Remove</th>
              </tr>
            </thead>
            <tbody>
              {cart.map(item => (
                <tr key={item.id} style={{ borderBottom: '1px solid #eee' }}>
                  <td style={{ padding: 8, display: 'flex', alignItems: 'center', gap: 12 }}>
                    <img src={item.image} alt={item.title} style={{ width: 50, height: 70, objectFit: 'cover', borderRadius: 6 }} />
                    <span>{item.title}</span>
                  </td>
                  <td style={{ padding: 8 }}>{item.author}</td>
                  <td style={{ padding: 8, textAlign: 'center' }}>
                    <input
                      type="number"
                      min="1"
                      value={item.quantity}
                      onChange={e => updateQuantity(item.id, parseInt(e.target.value) || 1)}
                      style={{ width: 50, textAlign: 'center' }}
                    />
                  </td>
                  <td style={{ padding: 8, textAlign: 'right' }}>Rs. {item.price * item.quantity}/-</td>
                  <td style={{ padding: 8, textAlign: 'center' }}>
                    <button
                      onClick={() => handleRemove(item.id)}
                      style={{
                        background: '#d32f2f',
                        color: '#fff',
                        border: 'none',
                        borderRadius: 4,
                        padding: '4px 10px',
                        cursor: 'pointer'
                      }}
                    >
                      Remove
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
        {cart.length > 0 && (
          <div style={{ textAlign: 'right', marginTop: 24, fontWeight: 'bold', fontSize: 18 }}>
            Total: Rs. {total}/-
          </div>
        )}
      </main>
      <Footer />
    </div>
  );
};

export default CartPage;