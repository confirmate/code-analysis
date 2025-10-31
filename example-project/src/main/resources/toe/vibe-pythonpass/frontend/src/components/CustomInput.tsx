import React, { InputHTMLAttributes } from "react";

interface CustomInputProps extends InputHTMLAttributes<HTMLInputElement> {
  label: string;
}

const CustomInput: React.FC<CustomInputProps> = ({ label, ...props }) => {
  return (
    <div>
      <label
        className="block text-sm font-medium text-gray-700"
        htmlFor={props.id}
      >
        {label}
      </label>
      <div className="mt-1">
        <input
          className="appearance-none block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm placeholder-gray-400 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
          {...props}
        />
      </div>
    </div>
  );
};

export default CustomInput;
