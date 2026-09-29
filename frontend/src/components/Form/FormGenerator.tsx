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

import { useForm, SubmitHandler, FieldValues, RegisterOptions } from 'react-hook-form';
import { FormInput } from './FormInput.tsx';
import { BaseUser } from '../../interfaces/User.ts';
import { navigateToUnauthorized } from "../DoNotHackMyApp/DoNotHackMyAppHelper.ts";
import { useLocation, useNavigate } from 'react-router-dom';

export interface FieldDef {tag: string, name: string, type: string, isRequired?: boolean, validators?: RegisterOptions, 
  defaultValue?: string, values?: any[], min?: number, max?: number, disabled?: boolean, numberOfColumns?: number;
}

export interface FormGeneratorProps<T extends FieldValues> {readonly fields: readonly FieldDef[], readonly defaultValues: Partial<T>, readonly onSubmit: SubmitHandler<T>,
  readonly submitText?: string;       // Text when not submitted.
  readonly loadingText?: string;      // Text uppon sending.
  readonly className?: string;        // Optional css for the form.
  readonly extraBeforeSubmit?: React.ReactNode;
  readonly cancelButtonActivated?: boolean;
  readonly navigateOnCancel?: string;
  readonly actualUser?: BaseUser, readonly requiredAuth?: string, readonly checkAuth?: Function | boolean, readonly onUnauthorizedRedirect?: string, readonly deactivateSecurity?: boolean
}

export function FormGenerator<T extends FieldValues> ({fields, defaultValues, onSubmit, submitText = 'Submit',
  loadingText = 'Loading...', className, extraBeforeSubmit, actualUser, requiredAuth, checkAuth, 
  cancelButtonActivated = false, navigateOnCancel = '', onUnauthorizedRedirect = '/do-not-hack-my-app', deactivateSecurity = false}: Readonly<FormGeneratorProps<T>>) {
  
  const location = useLocation();
  const navigate = useNavigate();
  if (!deactivateSecurity) {
    navigateToUnauthorized(checkAuth, actualUser, requiredAuth, navigate, onUnauthorizedRedirect);
  }

  let navigatePath = navigateOnCancel;
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<T>({...defaultValues});
  if (navigateOnCancel === '' && cancelButtonActivated)
    navigatePath = location.pathname.split('/').slice(0, -1).join('/') || '/';

  return (
    <form onSubmit={handleSubmit(onSubmit)} className={className}>
      {fields.map((field) => (
        <FormInput key={field.name} {...field} register={register} errors={errors} />
      ))}

      {extraBeforeSubmit}

      <div className="custom-button-row">
        {cancelButtonActivated && <button type="button" className="auth-button" onClick={() => navigate(navigatePath)}> Cancel</button>}
        <button type="submit" className="auth-button" disabled={isSubmitting}>
          {isSubmitting ? loadingText : submitText}
        </button>
      </div>
    </form>
  );
}