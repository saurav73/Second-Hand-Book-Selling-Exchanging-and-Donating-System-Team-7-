import React, { useState } from 'react';
import Navbar from './Navbar';
import Footer from './Footer';
import { Form, Input, Button, Typography, message } from 'antd';
import {
  MailOutlined,
  EyeOutlined,
  LockOutlined,
} from '@ant-design/icons';
import logo from '../Component/assests/hand-keep-book-read-source-600nw-1127076767-removebg-preview.png';
import { login } from '../services/api'; // Adjust path if needed
import { toast } from 'react-toastify';

const { Title, Text, Link } = Typography;

const Login = () => {
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);

  const onFinish = async (values) => {
    console.log('Form submitted with values:', values);
    setLoading(true);
    try {
      const data = await login(values.email, values.password);
      console.log('API response:', data);

      // ✅ Store complete user info including userType
      localStorage.setItem('user', JSON.stringify({
        token: data.token,
        userId: data.user.id,
        email: data.user.email,
        fullName: data.user.fullName,
        userType: data.user.userType, // Key for role-based Navbar
      }));

      toast.success('Login successful!');
      setTimeout(() => {
        window.location.href = '/search';
      }, 1200);
    } catch (error) {
      console.error('Login error:', error);
      toast.error(error.message || 'Login failed. Please check your credentials.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <Navbar />
      <div className="login-container">
        <div className="login-content">
          <div className="books-section">
            <div className="books-stack" />
          </div>

          <div className="login-form-section">
            <div className="login-header">
              <Title level={2} className="welcome-title">
                Welcome to Book Bridge!
                <img src={logo} alt="Book Bridge Logo" className="logo-image" />
              </Title>
              <Text className="welcome-description">
                Discover a seamless way to sell your books and unlock exclusive benefits.
                Enjoy a hassle-free experience, save valuable time, and take advantage of our amazing offers.
              </Text>
            </div>

            <Title level={3} className="login-subtitle">
              Login to Your Account!
            </Title>

            <Form
              form={form}
              name="login_form"
              className="login-form"
              onFinish={onFinish}
              layout="vertical"
            >
              <Form.Item
                name="email"
                rules={[{ required: true, message: 'Please enter your email!' }]}
              >
                <Input
                  placeholder="Enter Email"
                  className="login-input"
                  prefix={<MailOutlined className="input-icon" />}
                />
              </Form.Item>

              <Form.Item
                name="password"
                rules={[{ required: true, message: 'Please enter your password!' }]}
              >
                <Input.Password
                  placeholder="Enter Password"
                  className="login-input"
                  prefix={<LockOutlined className="input-icon" />}
                  iconRender={(visible) => <EyeOutlined className="eye-icon" />}
                />
              </Form.Item>

              <div className="form-actions">
                <Link href="/forget-password" className="forgot-password">
                  Forgot Password?
                </Link>
              </div>

              <Button
                type="primary"
                htmlType="submit"
                loading={loading}
                className="login-button"
              >
                LOGIN
              </Button>

              <div className="account-prompt">
                <Text>Don't you have an account?</Text>
                <Link href="/signup" className="create-account-link">
                  Create an account
                </Link>
              </div>
            </Form>
          </div>
        </div>

        {/* Inline Styles */}
        <style jsx>{`
          .login-container {
            display: flex;
            justify-content: center;
            align-items: center;
            min-height: 100vh;
            background-color: #f7faff;
            padding: 20px;
          }

          .login-content {
            display: flex;
            width: 100%;
            max-width: 1100px;
            background-color: #fff;
            border-radius: 12px;
            overflow: hidden;
            box-shadow: 0 10px 30px rgba(0, 0, 0, 0.08);
            border: 1px solid #e6e9f0;
          }

          .books-section {
            flex: 1;
            display: flex;
            justify-content: center;
            align-items: center;
            background-color: #fff;
            padding: 40px;
          }

          .books-stack {
            width: 100%;
            height: 100%;
            max-width: 400px;
            background-image: url('https://t3.ftcdn.net/jpg/03/41/64/12/360_F_341641286_3PsasOhbu2STmLNAfTwXx5dsmxRgZ3qT.jpg');
            background-size: contain;
            background-position: center;
            background-repeat: no-repeat;
          }

          .login-form-section {
            flex: 1.2;
            padding: 50px;
            display: flex;
            flex-direction: column;
            background-color: #f7faff;
          }

          .login-header {
            margin-bottom: 30px;
            text-align: left;
          }

          .welcome-title {
            color: #16A2F1;
            margin-bottom: 12px;
            font-size: 2rem;
            font-weight: 700;
            display: flex;
            align-items: center;
          }

          .logo-image {
            width: 40px;
            height: 40px;
            margin-left: 15px;
          }

          .welcome-description {
            color: #555;
            font-size: 1rem;
            line-height: 1.6;
            max-width: 450px;
          }

          .login-subtitle {
            color: #16A2F1;
            margin-bottom: 24px;
            text-align: left;
            font-size: 1.5rem;
            font-weight: 600;
          }

          .login-form {
            width: 100%;
          }

          .login-input {
            height: 50px;
            background-color: #e3f2fd;
            border: 1px solid #bbdefb;
            border-radius: 8px;
            font-size: 1rem;
            padding: 0 15px;
          }

          .login-input .ant-input {
            background-color: transparent !important;
          }

          .input-icon,
          .eye-icon {
            color: #90caf9;
          }

          .form-actions {
            text-align: right;
            margin-bottom: 20px;
          }

          .forgot-password {
            color: #555;
            font-size: 0.9rem;
          }

          .account-prompt {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-top: 20px;
          }

          .create-account-link {
            color: #007bff;
            font-weight: 600;
            font-size: 0.9rem;
          }

          .login-button {
            background-color: #28a745;
            border-color: #28a745;
            height: 40px;
            border-radius: 8px;
            font-size: 0.9rem;
            font-weight: 600;
            margin-top: 8px;
            width: 100%;
          }

          .login-button:hover {
            background-color: #218838;
            border-color: #1e7e34;
          }
        `}</style>
      </div>
      <Footer />
    </div>
  );
};

export default Login;
