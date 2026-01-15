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

import { useForm, FieldValues, DefaultValues } from 'react-hook-form';
import { useNavigate } from "react-router-dom";
import { FormInput } from './FormInput.tsx';
import { FieldDef, FormGeneratorProps } from './FormGenerator.tsx';
import { navigateToUnauthorized } from "../DoNotHackMyApp/DoNotHackMyAppHelper.ts";
import { BaseUser } from '../../interfaces/User.ts';

interface EditFormGeneratorProps<T extends FieldValues> extends Omit<FormGeneratorProps<T>, 'fields' | 'defaultValues'> {
  entityToEdit: T, selectOptions?: Record<string, string[]>, actualUser?: BaseUser, requiredAuth?: string, checkAuth?: Function | boolean,
  onUnauthorizedRedirect?: string, deactivateSecurity?: boolean
}

export function EditFormGenerator<T extends FieldValues>({entityToEdit, onSubmit, submitText = 'Submit', loadingText = 'Loading...',
  className, extraBeforeSubmit, selectOptions, actualUser, requiredAuth, checkAuth, onUnauthorizedRedirect = '/do-not-hack-my-app', deactivateSecurity = false}: EditFormGeneratorProps<T>) {

  const navigate = useNavigate();
  if (!deactivateSecurity) {  
    navigateToUnauthorized(checkAuth, actualUser, requiredAuth, navigate, onUnauthorizedRedirect);
  }
  const fields: FieldDef[] = buildFieldsFromEntity(entityToEdit, selectOptions);

  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<T>({
    defaultValues: entityToEdit as DefaultValues<T>});

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

function buildFieldsFromEntity(entity: object, selectOptions: Record<string, string[]> = {}, fieldConfig: Record<string, Partial<FieldDef>> = {}): FieldDef[] {
  return Object.keys(entity)
    .filter((key) => key !== 'id')
    .map((key) => {
      const value = (entity as any)[key];

      let type: string;
      let values: string[] | undefined;

      if (selectOptions[key]) {
        type = 'select';
        values = selectOptions[key];
      } else if (typeof value === 'number') {
        type = 'number';
      } else if (typeof value === 'boolean') {
        type = 'checkbox';
      } else if (value instanceof Date) {
        type = 'date';
      } else if (Array.isArray(value)) {
        type = 'select';
        values = value;
      } else if (key.toLowerCase().includes('password')) {
        type = 'password';
      } else if (key.toLowerCase().includes('email')) {
        type = 'email';
      } else {
        type = 'text';
      }

      return {
        tag: key.charAt(0).toUpperCase() + key.slice(1),
        name: key,
        type,
        defaultValue: type === 'checkbox' ? String(value) : String(value ?? ''),
        values,
        ...fieldConfig[key],
      } as FieldDef;
    });
}