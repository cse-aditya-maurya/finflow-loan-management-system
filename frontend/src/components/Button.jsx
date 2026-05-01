import React from 'react';
import './Button.css';

// Reusable Button Component
function Button({ 
  children,              // Button text or content
  onClick,               // Click handler function
  type = 'button',       // button, submit, reset
  variant = 'primary',   // primary, secondary, danger
  disabled = false,      // Disabled state
  loading = false,       // Loading state
  fullWidth = false      // Full width button
}) {
  
  // Build CSS class names based on props
  const buttonClass = `
    btn 
    btn-${variant} 
    ${fullWidth ? 'btn-full-width' : ''} 
    ${disabled || loading ? 'btn-disabled' : ''}
  `.trim();

  return (
    <button
      type={type}
      className={buttonClass}
      onClick={onClick}
      disabled={disabled || loading}
    >
      {loading ? (
        <span className="btn-loading">
          <span className="spinner"></span>
          Loading...
        </span>
      ) : (
        children
      )}
    </button>
  );
}

export { Button };
