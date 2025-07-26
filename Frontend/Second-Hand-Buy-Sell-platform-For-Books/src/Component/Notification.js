import React from 'react';
import Navbar from './Navbar';
import Footer from './Footer';
import './HomePage.css';

// Example notifications data (replace with real data source if available)
const notifications = [
  {
    id: 1,
    type: 'Donate',
    book: 'The Book Thief',
    message: 'A new book is available for donation!'
  },
  {
    id: 2,
    type: 'Sell',
    book: 'Dune',
    message: 'A new book is available for sale!'
  },
  {
    id: 3,
    type: 'Exchange',
    book: 'David Copperfield',
    message: 'A new book is available for exchange!'
  }
];

const typeColor = {
  Donate: '#43a047',
  Sell: '#1976d2',
  Exchange: '#fbc02d'
};

const Notification = () => {
  return (
    <div>
      <Navbar />
      <div style={{ maxWidth: 600, margin: '40px auto', background: '#fff', borderRadius: 12, padding: 32, boxShadow: '0 4px 16px rgba(0,0,0,0.08)' }}>
        <h2 style={{ textAlign: 'center', marginBottom: 24 }}>Notifications</h2>
        {notifications.length === 0 ? (
          <div style={{ textAlign: 'center', color: '#888' }}>
            <p>No notifications at the moment.</p>
          </div>
        ) : (
          <ul style={{ listStyle: 'none', padding: 0 }}>
            {notifications.map((notif) => (
              <li key={notif.id} style={{
                marginBottom: 20,
                padding: 16,
                borderRadius: 8,
                background: '#f5faff',
                borderLeft: `6px solid ${typeColor[notif.type] || '#1976d2'}`
              }}>
                <div style={{ fontWeight: 600, color: typeColor[notif.type] || '#1976d2', marginBottom: 4 }}>
                  {notif.type} Update
                </div>
                <div style={{ fontSize: 18, marginBottom: 2 }}>{notif.book}</div>
                <div style={{ color: '#555' }}>{notif.message}</div>
              </li>
            ))}
          </ul>
        )}
      </div>
      <Footer />
    </div>
  );
};

export default Notification; 