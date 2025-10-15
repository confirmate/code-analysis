import React, { useState } from "react";
import CustomInput from "./CustomInput";
import CustomButton from "./CustomButton";

interface AddPasswordFormProps {
  onAddPassword: (newPassword: {
    website: string;
    username: string;
    password: string;
  }) => void;
}

function AddPasswordForm({ onAddPassword }: AddPasswordFormProps) {
  const [newPassword, setNewPassword] = useState({
    website: "",
    username: "",
    password: "",
  });

  const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    onAddPassword(newPassword);
    setNewPassword({ website: "", username: "", password: "" });
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setNewPassword((prev) => ({ ...prev, [name]: value }));
  };

  return (
    <div className="mt-8 sm:mx-auto sm:w-full sm:max-w-md">
      <div className="bg-white py-8 px-4 shadow sm:rounded-lg sm:px-10">
        <form className="space-y-6" onSubmit={handleSubmit}>
          <div className="">
            <CustomInput
              label="Website"
              id="new-website"
              name="website"
              type="text"
              required
              value={newPassword.website}
              onChange={handleChange}
            />
            <CustomInput
              label="Username"
              id="new-username"
              name="username"
              type="text"
              required
              value={newPassword.username}
              onChange={handleChange}
            />
            <CustomInput
              label="Password"
              id="new-password"
              name="password"
              type="password"
              required
              value={newPassword.password}
              onChange={handleChange}
            />
          </div>
          <div>
            <CustomButton type="submit" variant="primary" className="w-full">
              Add Password
            </CustomButton>
          </div>
        </form>
      </div>
    </div>
  );
}

export default AddPasswordForm;
