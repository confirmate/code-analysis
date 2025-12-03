import React, { useState } from 'react';
import EditPasswordForm from './EditPasswordForm';
import { Password } from '../types/password';
import { usePasswordVisibility } from '../hooks/usePasswordVisibility';

interface PasswordItemProps {
  password: Password;
  onEditPassword: (editedPassword: Password) => void;
}

function PasswordItem({ password, onEditPassword }: PasswordItemProps) {
  const { isVisible, countdown, togglePasswordVisibility, hidePassword } = usePasswordVisibility();
  const [isEditing, setIsEditing] = useState(false);

  const copyToClipboard = () => {
    navigator.clipboard.writeText(password.password).then(() => {
      alert('Password copied to clipboard!');
    }, (err) => {
      console.error('Could not copy password: ', err);
    });
  };

  const startEditing = () => {
    setIsEditing(true);
    hidePassword();
  };

  const handleEditComplete = (editedPassword: Password) => {
    onEditPassword(editedPassword);
    setIsEditing(false);
  };

  if (isEditing) {
    return <EditPasswordForm password={password} onEditComplete={handleEditComplete} />;
  }

  return (
    <li className="px-6 py-4 relative">
      <div className="flex items-center justify-between">
        <div>
          <h3 className="text-lg font-medium text-gray-800">{password.website}</h3>
          <p className="text-sm text-gray-600">{password.username}</p>
        </div>
        <div className="flex items-center">
          <span className="px-3 py-1 bg-gray-100 text-gray-800 rounded-full text-sm mr-2">
            {isVisible ? password.password : '••••••••'}
          </span>
          <button
            onClick={togglePasswordVisibility}
            className="text-sm text-blue-500 hover:text-blue-700 focus:outline-none mr-2"
          >
            {isVisible ? 'Hide' : 'Show'}
          </button>
          <button
            onClick={copyToClipboard}
            className="text-sm bg-green-500 text-white px-2 py-1 rounded hover:bg-green-600 focus:outline-none mr-2"
          >
            Copy
          </button>
          <button
            onClick={startEditing}
            className="text-sm bg-yellow-500 text-white px-2 py-1 rounded hover:bg-yellow-600 focus:outline-none"
          >
            Edit
          </button>
        </div>
      </div>
      {isVisible && (
        <div className="mt-2">
          <div className="w-full bg-gray-200 rounded-full h-2.5 dark:bg-gray-700">
            <div 
              className="bg-blue-600 h-2.5 rounded-full transition-all duration-100 ease-linear" 
              style={{ width: `${(countdown / 30) * 100}%` }}
            ></div>
          </div>
          <p className="text-xs text-gray-500 mt-1">
            Password will be hidden in {Math.ceil(countdown)} seconds
          </p>
        </div>
      )}
    </li>
  );
}

export default PasswordItem;