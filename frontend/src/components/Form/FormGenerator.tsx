import { useForm, SubmitHandler, FieldValues, RegisterOptions } from 'react-hook-form';
import { FormInput } from './FormInput.tsx';

interface FieldDef {tag: string, name: string, type: string, isRequired?: boolean, validators?: RegisterOptions, 
  defaultValue?: string, values?: any[], min?: number, max?: number, disabled?: boolean, numberOfColumns?: number;
}

interface FormGeneratorProps<T extends FieldValues> {fields: FieldDef[], defaultValues: Partial<T>, onSubmit: SubmitHandler<T>,
  submitText?: string;       // Text when not submitted.
  loadingText?: string;      // Text uppon sending.
  className?: string;        // Optional css for the form.
  extraBeforeSubmit?: React.ReactNode;
}

export function FormGenerator<T extends FieldValues> ({fields, defaultValues, onSubmit, submitText = 'Submit',
  loadingText = 'Loading...', className, extraBeforeSubmit}: FormGeneratorProps<T>) {
  
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