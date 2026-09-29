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

import { useForm, FieldValues, DefaultValues } from 'react-hook-form';
import { useLocation, useNavigate } from "react-router-dom";
import { FormInput } from './FormInput.tsx';
import { FieldDef, FormGeneratorProps } from './FormGenerator.tsx';
import { navigateToUnauthorized } from "../DoNotHackMyApp/DoNotHackMyAppHelper.ts";
import { BaseUser } from '../../interfaces/User.ts';
import { get } from '../../util/fetchers.ts';
import tokenService from '../../services/token.service.ts';
import { useEffect, useState } from 'react';

interface EditFormGeneratorProps<T extends FieldValues> extends Omit<FormGeneratorProps<T>, 'fields' | 'defaultValues'> {
  readonly entityToEdit: T, readonly cancelButtonActivated?: boolean, readonly selectOptions?: Record<string, string[]>, readonly actualUser?: BaseUser, readonly requiredAuth?: string, readonly checkAuth?: Function | boolean,
  readonly onUnauthorizedRedirect?: string, readonly deactivateSecurity?: boolean, readonly baseUriToFetchEntity?: string
}

export function EditFormGenerator<T extends FieldValues>({entityToEdit, onSubmit, submitText = 'Submit', loadingText = 'Loading...',
  className, cancelButtonActivated=true, selectOptions, extraBeforeSubmit, actualUser, requiredAuth, checkAuth, 
  onUnauthorizedRedirect = '/do-not-hack-my-app', deactivateSecurity = false, baseUriToFetchEntity = "/api/v1"}: Readonly<EditFormGeneratorProps<T>>) {
  const [entity, setEntity] = useState<T | null>(entityToEdit ?? null);
  const [fields, setFields] = useState<FieldDef[] | null>(buildFieldsFromEntity(entity!, selectOptions) ?? null);
  const { register, handleSubmit, reset, formState: { errors, isSubmitting } } = useForm<T>({defaultValues: entity as DefaultValues<T>});
  
  const jwt = tokenService.getLocalAccessToken();
  const location = useLocation();
  useEffect(() => {
    if (entity == null) {
      get(baseUriToFetchEntity + location.pathname, jwt).then(res => res.json())
        .then(data => {
          console.log('Fetched entity to edit:', data);
          setEntity(data as T);
          reset(data);
          setFields(buildFieldsFromEntity(data, selectOptions));
        });
    } else {
      reset(entity);
    }
  }, [entity, jwt, location.pathname, reset]);

  const navigate = useNavigate();
  if (!deactivateSecurity) {  
    navigateToUnauthorized(checkAuth, actualUser, requiredAuth, navigate, onUnauthorizedRedirect);
  }

  const parentPath = location.pathname.split('/').slice(0, -1).join('/') || '/';

  return (
    <form onSubmit={handleSubmit(onSubmit)} className={className}>
      {fields?.map((field) => (
        <FormInput key={field.name} {...field} register={register} errors={errors} />
      ))}

      {extraBeforeSubmit}

      <div className="custom-button-row">
        {cancelButtonActivated && <button type="button" className="auth-button" onClick={() => navigate(parentPath)}> Cancel</button>}
        <button type="submit" className="auth-button" disabled={isSubmitting}>
          {isSubmitting ? loadingText : submitText}
        </button>
      </div>
    </form>
  );
}

function buildFieldsFromEntity(entity: object | null, selectOptions: Record<string, string[]> = {}, fieldConfig: Record<string, Partial<FieldDef>> = {}): FieldDef[] {
  if (entity == null) {
    return [];
  }
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