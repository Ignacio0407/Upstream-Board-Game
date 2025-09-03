/**
 * -----------------------------------------------------------------------------
 * Autor / Author: Ignacio Gutiérrez Serrera
 * 
 * 🇬🇧 This software was developed by Ignacio Gutiérrez Serrera.
 * It may be used, modified, and distributed for personal or commercial 
 * purposes, provided that explicit credit is given to the original author.
 * 
 * If this code is used as-is or with minor to moderate modifications 
 * as part of another software, proper attribution must be given to 
 * Ignacio Gutiérrez Serrera.
 * (e.g., credits, documentation, or user interface).
 * 
 * This license notice must not be removed in any redistribution 
 * of the source code or binaries.
 * 
 * 🇪🇸 Este software ha sido desarrollado por Ignacio Gutiérrez Serrera.
 * Se permite su uso, modificación y distribución con fines personales o 
 * comerciales, siempre que se mantenga el reconocimiento explícito al autor.
 * 
 * Si este código se utiliza tal cual, o con modificaciones leves o medias 
 * como parte de otro software, se debe otorgar el crédito correspondiente 
 * a Ignacio Gutiérrez Serrera.
 * (por ejemplo, en los créditos, documentación o interfaz del producto).
 * 
 * No se permite eliminar este aviso de licencia en ninguna redistribución 
 * del código fuente o binario.
 * -----------------------------------------------------------------------------
 */

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