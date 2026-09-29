/**
 * -----------------------------------------------------------------------------
 * Autor / Author: Ignacio Gutiérrez Serrera
 * GitHub: https://github.com/Ignacio0407 (@Ignacio0407)
 * Contact / Email: ignaciogutierrezserrera@gmail.com
 * 
 * 🇬🇧 NON-COMMERCIAL & EDUCATIONAL LICENSE:
 * This software was developed by Ignacio Gutiérrez Serrera.
 * Permission is granted to use, copy, modify, and study this software for 
 * non-commercial, personal, academic, and educational purposes, provided 
 * that this copyright notice, author attribution, and contact details are 
 * preserved in all copies or substantial portions of the software.
 * 
 * Any commercial use, integration into proprietary software, or closed-source 
 * distribution is strictly prohibited without prior written permission and an 
 * express commercial license granted by the author.
 * 
 * 🇪🇸 LICENCIA PARA FINES NO LUCRATIVOS Y EDUCATIVOS:
 * Este software ha sido desarrollado por Ignacio Gutiérrez Serrera.
 * Se autoriza su uso, copia, modificación y estudio exclusivamente para 
 * fines personales, académicos, de investigación y educativos no lucrativos, 
 * con la condición obligatoria de mantener intacto este aviso de copyright, 
 * la atribución de autoría y los datos de contacto en todas las copias.
 * 
 * Queda terminantemente prohibido cualquier uso comercial, integración en 
 * software privativo o distribución con fines lucrativos sin la autorización 
 * previa por escrito del autor y la adquisición de una licencia comercial.
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
  
  const renderInput = () => {
    const registerOptions = { 
      required: isRequired ? `The ${tag} is required` : false,
      ...validators 
    };

    if (type === 'select') {
      return (
        <select id={name} className="class-form-input" disabled={disabled} defaultValue={defaultValue}
          {...register(name, registerOptions)}
        >
          {values?.map((option) => (
            <option key={String(option)} value={option}>
              {option}
            </option>
          ))}
        </select>
      );
    }

    if (type === 'textarea') {
      return (
        <textarea id={name} className="class-form-input" disabled={disabled} defaultValue={defaultValue}
          {...register(name, registerOptions)}
        />
      );
    }

    if (type === 'date') {
      return (
        <input id={name} className="class-form-input" type="date" disabled={disabled} defaultValue={defaultValue}
          {...register(name, registerOptions)}
        />
      );
    }

    return (
      <input id={name} className="class-form-input" type={type} disabled={disabled} defaultValue={defaultValue}
        {...register(name, registerOptions)}
      />
    );
  };

  return (
    <div className={`class-form-group ${hasError ? "class-error-form" : ""}`} 
         style={numberOfColumns > 1 ? { width: `${100/numberOfColumns-3}%` } : {}}>
      
      {renderInput()}
      
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