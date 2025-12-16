import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import PasswordList from '../components/PasswordList';
import AddPasswordForm from '../components/AddPasswordForm';
import { Password } from '../types/password';

function Dashboard() {
  const [passwords, setPasswords] = useState<Password[]>([]);
  const navigate = useNavigate();

  useEffect(() => {
    fetchPasswords();
  }, []);

  const fetchPasswords = async () => {
    try {
      const token = localStorage.getItem('token');
      const response = await fetch('http://localhost:5000/passwords', {
        headers: { Authorization: token || '' }
      });
      if (!response.ok) {
        throw new Error('Failed to fetch passwords');
      }
      const data: Password[] = await response.json();
      setPasswords(data);
    } catch (error) {
      console.error('Error fetching passwords:', error);
      navigate('/login');
    }
  };

  const handleAddPassword = async (newPassword: Omit<Password, 'id'>) => {
    try {
      const token = localStorage.getItem('token');
      const response = await fetch('http://localhost:5000/passwords', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: token || ''
        },
        body: JSON.stringify(newPassword)
      });
      if (!response.ok) {
        throw new Error('Failed to add password');
      }
      fetchPasswords();
    } catch (error) {
      console.error('Error adding password:', error);
    }
  };

  const handleEditPassword = async (editedPassword: Password) => {
    try {
      const token = localStorage.getItem('token');
      const response = await fetch(`http://localhost:5000/passwords/${editedPassword.id}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          Authorization: token || ''
        },
        body: JSON.stringify(editedPassword)
      });
      if (!response.ok) {
        throw new Error('Failed to edit password');
      }
      fetchPasswords();
    } catch (error) {
      console.error('Error editing password:', error);
    }
  };

  return (
    <div className="min-h-screen bg-gray-100">
      <nav className="bg-white shadow-sm">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between h-16">
            <div className="flex">
              <div className="flex-shrink-0 flex items-center">
                <h1 className="text-xl font-bold">Password Manager</h1>
              </div>
            </div>
          </div>
        </div>
      </nav>

      <main className="max-w-7xl mx-auto py-6 sm:px-6 lg:px-8">
        <div className="px-4 py-6 sm:px-0">
          <PasswordList passwords={passwords} onEditPassword={handleEditPassword} />
          <AddPasswordForm onAddPassword={handleAddPassword} />
        </div>
      </main>
    </div>
  );
}

export default Dashboard;