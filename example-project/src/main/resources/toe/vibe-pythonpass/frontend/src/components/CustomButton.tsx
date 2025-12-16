import React from 'react';

interface CustomButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  children: React.ReactNode;
  variant?: 'primary' | 'secondary';
}

function CustomButton({ children, variant = 'primary', ...props }: CustomButtonProps) {
  const baseClasses = "flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium focus:outline-none focus:ring-2 focus:ring-offset-2";
  
  const variantClasses = variant === 'primary'
    ? "text-white bg-indigo-600 hover:bg-indigo-700 focus:ring-indigo-500"
    : "text-gray-700 bg-gray-200 hover:bg-gray-300 focus:ring-gray-500 border-gray-300";

  return (
    <button
      className={`${baseClasses} ${variantClasses}`}
      {...props}
    >
      {children}
    </button>
  );
}

export default CustomButton;