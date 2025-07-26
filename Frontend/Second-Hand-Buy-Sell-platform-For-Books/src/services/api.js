// api.js
const BASE_URL = 'http://localhost:8080/api';

const handleResponse = async (response) => {
  const data = await response.json();
  if (!response.ok) {
    throw new Error(data.message || 'An error occurred');
  }
  return data;
};

export const login = async (email, password) => {
  console.log('Attempting login with email:', email);
  try {
    const response = await fetch(`${BASE_URL}/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password }),
    });
    console.log('Fetch response status:', response.status);
    return handleResponse(response);
  } catch (error) {
    console.error('Fetch error:', error);
    throw error;
  }
};

// ✅ NEW: Register function
export const register = async (accountType, formData) => {
  try {
    const response = await fetch(`${BASE_URL}/register/${accountType}`, {
      method: 'POST',
      body: formData, // FormData includes the file and all fields
    });
    return handleResponse(response);
  } catch (error) {
    console.error('Registration API error:', error);
    throw error;
  }
};

// Password reset (send verification link/code to email)
export const requestPasswordReset = async (email) => {
  try {
    const response = await fetch(`${BASE_URL}/password/reset`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email }),
    });
    return handleResponse(response);
  } catch (error) {
    console.error('Password reset API error:', error);
    throw error;
  }
};

// Fetch all books
export const fetchBooks = async () => {
  try {
    const response = await fetch(`${BASE_URL}/books`);
    return handleResponse(response);
  } catch (error) {
    console.error('Fetch books API error:', error);
    throw error;
  }
};

// Add a new book (with optional image upload)
export const addBook = async (bookData) => {
  try {
    const userData = localStorage.getItem('user');
    const token = userData ? JSON.parse(userData).token : null;
    const response = await fetch(`${BASE_URL}/books`, {
      method: 'POST',
      body: bookData,
      headers: token ? { Authorization: 'Bearer ' + token } : {},
    });
    return handleResponse(response);
  } catch (error) {
    console.error('Add book API error:', error);
    throw error;
  }
};

export const fetchBooksByUser = async (userId) => {
  try {
    const response = await fetch(`${BASE_URL}/books/user/${userId}`);
    return handleResponse(response);
  } catch (error) {
    console.error('Fetch books by user API error:', error);
    throw error;
  }
};

export const deleteBook = async (bookId, token) => {
  const response = await fetch(`${BASE_URL}/books/${bookId}`, {
    method: 'DELETE',
    headers: token ? { Authorization: 'Bearer ' + token } : {},
  });
  if (!response.ok) {
    const data = await response.json().catch(() => ({}));
    throw new Error(data.message || 'Failed to delete book');
  }
  return true;
};

export const updateBook = async (bookId, bookData, token) => {
  const response = await fetch(`${BASE_URL}/books/${bookId}`, {
    method: 'PUT',
    body: bookData,
    headers: token ? { Authorization: 'Bearer ' + token } : {},
  });
  if (!response.ok) {
    const data = await response.json().catch(() => ({}));
    throw new Error(data.message || 'Failed to update book');
  }
  return response.json();
};

// Initiate payment for an order (get eSewa payment URL or token)
export const initiatePayment = async (orderId, amount) => {
  try {
    const response = await fetch(`${BASE_URL}/payments/initiate`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ orderId, amount }),
    });
    return handleResponse(response);
  } catch (error) {
    console.error('Initiate payment API error:', error);
    throw error;
  }
};

// Verify payment status (after redirect/callback)
export const verifyPayment = async (orderId, paymentData) => {
  try {
    const response = await fetch(`${BASE_URL}/payments/verify`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ orderId, ...paymentData }),
    });
    return handleResponse(response);
  } catch (error) {
    console.error('Verify payment API error:', error);
    throw error;
  }
};
