import React from "react";
import Navbar from './Navbar';
import Footer from './Footer';
import "./Aboutus.css";

const AboutUs = () => {
  return (
    <div className="aboutus-container">
      <Navbar />

      {/* Hero Image */}
      <div className="aboutus-hero">
        <img
          src="https://img.freepik.com/premium-photo/open-book-with-blue-background-words-open_847439-9006.jpg"
          alt="Open Book"
          className="aboutus-hero-img"
        />
      </div>

      {/* About Us Content */}
      <div className="aboutus-content">
        <h2 className="aboutus-title">About Us</h2>
        <p className="aboutus-desc">
          Welcome to Book Bridge – a community-driven platform where books find a second life and readers discover hidden gems.
        </p>
        <p className="aboutus-desc">
          We believe that stories and knowledge should never gather dust. That's why we created a space where anyone can exchange, sell, or donate used books with ease and purpose. Whether you're a student looking to swap textbooks, a reader hoping to declutter your shelf, or someone with a generous heart wanting to donate books to others – you're in the right place.
        </p>
        <div className="aboutus-mission">
          <p>Our mission is simple:</p>
          <ul>
            <li>📚 Make books accessible and affordable for everyone</li>
            <li>🌱 Promote sustainability through reuse</li>
            <li>❤️ Foster a culture of sharing and lifelong learning</li>
          </ul>
        </div>
        <h3 className="aboutus-commitment-title">Our Commitment</h3>
        <p className="aboutus-commitment-desc">
          At Book Bridge, we're dedicated to making books accessible, affordable, and reusable. We promote a sustainable reading culture by encouraging second-hand exchanges, sales, and donations. Our goal is to build a trusted, community-driven space that supports learning, sharing, and positive social impact.
        </p>
      </div>

      {/* Stats Section */}
      <div className="aboutus-stats-section">
        <div className="aboutus-stats-img">
          <img
            src="https://img.freepik.com/premium-photo/stack-books-blue-background_847439-9007.jpg"
            alt="Stack of Books"
          />
        </div>
        <div className="aboutus-stats-info">
          <h3>
            Your favorite <span className="aboutus-stats-highlight">Reads</span>
            <br />
            <span className="aboutus-stats-highlight2">Are Here!</span>
          </h3>
          <p>
            Buy your favorite books online with ease! Enjoy exclusive offers and discounts on selected titles. Dive into our collection and find special deals that make reading more affordable. Shop now and unlock more savings with every purchase!
          </p>
          <div className="aboutus-stats-numbers">
            <div>
              <span className="aboutus-stats-number">800+</span>
              <span className="aboutus-stats-label">Book Listing</span>
            </div>
            <div>
              <span className="aboutus-stats-number">1K+</span>
              <span className="aboutus-stats-label">Registered Members</span>
            </div>
            <div>
              <span className="aboutus-stats-number">50+</span>
              <span className="aboutus-stats-label">Branch Count</span>
            </div>
          </div>
        </div>
      </div>

      <Footer />
    </div>
  );
};

export default AboutUs;