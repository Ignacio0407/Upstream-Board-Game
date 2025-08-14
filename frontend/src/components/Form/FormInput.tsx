import React from 'react';
import { UseFormRegister, FieldErrors, RegisterOptions } from 'react-hook-form';
import './FormInput.css';

interface FormInputProps {tag: string; name: string; type: string; register: UseFormRegister<any>; errors: FieldErrors;
  defaultValue?: string; isRequired?: boolean; validators?: RegisterOptions; values?: any[];
  min?: number; max?: number; disabled?: boolean; numberOfColumns?: number;}

export const FormInput: React.FC<FormInputProps> = ({tag, name, type, register, errors, defaultValue = '', isRequired = false,
  validators = {}, values, min, max, disabled = false, numberOfColumns = 1
}) => {
  const hasError = errors[name];
  
  return (
    <div className={`class-form-group ${hasError ? "class-error-form" : ""}`} 
         style={numberOfColumns > 1 ? { width: `${100/numberOfColumns-3}%` } : {}}>
      
      {type === 'select' ? (
        <select className="class-form-input" disabled={disabled} defaultValue={defaultValue}
          {...register(name, { 
            required: isRequired ? 'This field is required' : false,
            ...validators 
          })}
        >
          {values?.map((option, index) => (
            <option key={index} value={option}>
              {option}
            </option>
          ))}
        </select>
      ) : type === 'textarea' ? (
        <textarea className="class-form-input" disabled={disabled} defaultValue={defaultValue}
          {...register(name, { 
            required: isRequired ? 'This field is required' : false,
            ...validators 
          })}
        />
      ) : type === 'date' ? (
        <input className="class-form-input" type="date" disabled={disabled} defaultValue={defaultValue}
          {...register(name, { 
            required: isRequired ? 'This field is required' : false,
            ...validators 
          })}
        />
      ) : (
        <input className="class-form-input" type={type} disabled={disabled} defaultValue={defaultValue}
          {...register(name, { 
            required: isRequired ? 'This field is required' : false,
            ...validators 
          })}
        />
      )}
      
      <label htmlFor={name} className="class-form-label">
        {tag}{isRequired && ' *'}
      </label>
      
      {hasError && (
        <span className="class-error-message">
          {typeof hasError.message === 'string' ? hasError.message : 'This field is invalid'}
        </span>
      )}
    </div>
  );
};