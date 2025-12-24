import { useForm, FieldValues, DefaultValues } from 'react-hook-form';
import { FormInput } from './FormInput.tsx';
import { FieldDef, FormGeneratorProps } from './FormGenerator.tsx';

interface EditFormGeneratorProps<T extends FieldValues> extends Omit<FormGeneratorProps<T>, 'fields' | 'defaultValues'> {
  entityToEdit: T, selectOptions?: Record<string, string[]>
}

export function EditFormGenerator<T extends FieldValues>({entityToEdit, onSubmit, submitText = 'Submit', loadingText = 'Loading...',
  className, extraBeforeSubmit, selectOptions = {}}: EditFormGeneratorProps<T>) {

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