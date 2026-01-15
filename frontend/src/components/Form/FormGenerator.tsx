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

import { useForm, SubmitHandler, FieldValues, RegisterOptions } from 'react-hook-form';
import { FormInput } from './FormInput.tsx';
import { BaseUser } from '../../interfaces/User.ts';
import { navigateToUnauthorized } from "../DoNotHackMyApp/DoNotHackMyAppHelper.ts";
import { useNavigate } from 'react-router-dom';

export interface FieldDef {tag: string, name: string, type: string, isRequired?: boolean, validators?: RegisterOptions, 
  defaultValue?: string, values?: any[], min?: number, max?: number, disabled?: boolean, numberOfColumns?: number;
}

export interface FormGeneratorProps<T extends FieldValues> {fields: FieldDef[], defaultValues: Partial<T>, onSubmit: SubmitHandler<T>,
  submitText?: string;       // Text when not submitted.
  loadingText?: string;      // Text uppon sending.
  className?: string;        // Optional css for the form.
  extraBeforeSubmit?: React.ReactNode;
  actualUser?: BaseUser, requiredAuth?: string, checkAuth?: Function | boolean, onUnauthorizedRedirect?: string, deactivateSecurity?: boolean
}

export function FormGenerator<T extends FieldValues> ({fields, defaultValues, onSubmit, submitText = 'Submit',
  loadingText = 'Loading...', className, extraBeforeSubmit, actualUser, requiredAuth, checkAuth, onUnauthorizedRedirect = '/do-not-hack-my-app', deactivateSecurity = false}: FormGeneratorProps<T>) {
  
  const navigate = useNavigate();
  if (!deactivateSecurity) {
    navigateToUnauthorized(checkAuth, actualUser, requiredAuth, navigate, onUnauthorizedRedirect);
  }

  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<T>({...defaultValues});

  return (
    <form onSubmit={handleSubmit(onSubmit)} className={className}>
      {fields.map((field, idx) => (
        <FormInput key={idx} {...field} register={register} errors={errors} />
      ))}

      {extraBeforeSubmit}

      <div className="custom-button-row">
        <button type="submit" className="auth-button" disabled={isSubmitting}>
          {isSubmitting ? loadingText : submitText}
        </button>
      </div>
    </form>
  );
}