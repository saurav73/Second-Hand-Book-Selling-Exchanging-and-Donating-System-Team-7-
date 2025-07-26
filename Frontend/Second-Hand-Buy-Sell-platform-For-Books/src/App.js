import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Login from './Component/Login'
import Signup from './Component/Signup'
import HomePage from './Component/HomePage';
import Aboutus from './Component/Aboutus'; 
import Shop from './Component/Shop'; 
import CartPage from './Component/CartPage';
import BookSell from './Component/BookSell';
import BookExchange from './Component/BookExchange';
import BookDonate from './Component/BookDonate';
import RegisterOrganization from './Component/RegisterOrganization';
import RequestBook from './Component/RequestBook';
import BookList from './Component/Booklist';
import SearchPage from './Component/SearchPage';
import { CartProvider } from './Component/CartContext'; 
import ForgetPassword from './Component/ForgetPassword';

import ChangePassword from './Component/ChangePassword';
import ViewDetails from './Component/ViewDetails';
import Registration from './Component/Registration';
import Profile from './Component/Profile';
import { ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import ResetPassword from './Component/ResetPassword';

function App() {
  return (
    <CartProvider> 
      <Router>
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/registration" element={<Registration/>}/>
          <Route path="/login" element={<Login />} />
          <Route path="/signup" element={<Signup/>} />
          <Route path="/about" element={<Aboutus />} />
          <Route path="/shop" element={<Shop />} />
          <Route path="/cart" element={<CartPage />} /> 
          <Route path="/book-sell" element={<BookSell />} />
          <Route path="/book-exchange" element={<BookExchange />} />
          <Route path="/book-donate" element={<BookDonate />} />
          <Route path="/register-organization" element={<RegisterOrganization />} />
          <Route path="/request-book" element={<RequestBook />} />
          <Route path="/book-list" element={<BookList />} />
          <Route path="/search" element={<SearchPage />} />
          <Route path="/forget-password" element={<ForgetPassword />} />
          <Route path="/reset-password" element={<ResetPassword/>} />
          <Route path="/change-password" element={<ChangePassword />} />
          <Route path="/view-details" element={<ViewDetails />} />
          <Route path="/profile" element={<Profile/>} />  
        
          {/* Add more routes as needed */}
        </Routes>
      </Router>
      <ToastContainer position="top-right" autoClose={3000} />
    </CartProvider>
  );
}

export default App;
