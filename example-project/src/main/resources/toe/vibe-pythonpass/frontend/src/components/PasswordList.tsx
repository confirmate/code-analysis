import React from 'react';
import PasswordItem from './PasswordItem';
import { Password } from '../types/password';

interface PasswordListProps {
  passwords: Password[];
  onEditPassword: (editedPassword: Password) => void;
}

function PasswordList({ passwords, onEditPassword }: PasswordListProps) {
  return (
    <div>
      <h2 className="text-2xl font-semibold text-gray-800 mb-4">Your Passwords</h2>
      <div className="bg-white shadow overflow-hidden sm:rounded-md">
        <ul className="divide-y divide-gray-200">
          {passwords.map((password) => (
            <PasswordItem 
              key={password.id} 
              password={password} 
              onEditPassword={onEditPassword} 
            />
          ))}
        </ul>
      </div>
    </div>
  );
}

export default PasswordList;