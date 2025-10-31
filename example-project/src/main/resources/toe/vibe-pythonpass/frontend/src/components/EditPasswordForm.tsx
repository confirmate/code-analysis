import React, { useState } from 'react';
import { Password } from '../types/password';
import CustomInput from './CustomInput';
import CustomButton from './CustomButton';

interface EditPasswordFormProps {
  password: Password;
  onEditComplete: (editedPassword: Password) => void;
}

function EditPasswordForm({ password, onEditComplete }: EditPasswordFormProps) {
  const [editedPassword, setEditedPassword] = useState<Password>({
    id: password.id,
    website: password.website,
    username: password.username,
    password: password.password
  });

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setEditedPassword(prev => ({ ...prev, [name]: value }));
  };

  const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    onEditComplete(editedPassword);
  };

  return (
    <form onSubmit={handleSubmit} className="bg-white shadow-md rounded px-8 pt-6 pb-8 mb-4">
      <CustomInput
        label="Website"
        type="text"
        name="website"
        id="website"
        value={editedPassword.website}
        onChange={handleChange}
        required
      />
      <CustomInput
        label="Username"
        type="text"
        name="username"
        id="username"
        value={editedPassword.username}
        onChange={handleChange}
        required
      />
      <CustomInput
        label="Password"
        type="password"
        name="password"
        id="password"
        value={editedPassword.password}
        onChange={handleChange}
        required
      />
      <div className="flex items-center justify-between">
        <CustomButton type="submit">
          Save Changes
        </CustomButton>
        <CustomButton 
          type="button" 
          onClick={() => onEditComplete(password)} 
          variant="secondary"
        >
          Cancel
        </CustomButton>
      </div>
    </form>
  );
}

export default EditPasswordForm;