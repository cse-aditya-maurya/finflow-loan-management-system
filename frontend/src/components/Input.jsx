import React from 'react';
import './Input.css';

// Reusable Input Component
function Input({
  label,                 // Input label text
  type = 'text',         // Input type (text, email, password, number, etc.)
  name,                  // Input name attribute
  value,                 // Input value
  onChange,              // Change handler function
  placeholder,           // Placeholder text
  error,                 // Error message to display
  required = false,      // Is field required
  disabled = false,      // Is field disabled
  min,                   // Min value for number inputs
  max,                   // Max value for number inputs
  step,                  // Step value for number inputs
  id                     // ID attribute
}) {
  
  return (
    <div className="input-group">
      {/* Label */}
      {label && (
        <label htmlFor={name} className="input-label">
          {label}
          {required && <span className="required-star">*</span>}
        </label>
      )}

      {/* Input Field */}
      <input
        type={type}
        id={id || name}
        name={name}
        value={value}
        onChange={onChange}
        placeholder={placeholder}
        required={required}
        disabled={disabled}
        min={min}
        max={max}
        step={step}
        className={`input-field ${error ? 'input-error' : ''}`}
      />

      {/* Error Message */}
      {error && (
        <span className="error-message">{error}</span>
      )}
    </div>
  );
}

export { Input };
