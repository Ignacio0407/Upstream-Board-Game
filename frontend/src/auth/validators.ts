const validators = {
  username: {
    required: 'Username is required',
    pattern: {
      value: /^[a-zA-Z0-9_]{3,20}$/,
      message: 'Username must be 3-20 characters, letters, numbers, or underscore'
    }
  },
  password: {
    required: 'Password is required',
    minLength: {
      value: 3,
      message: 'Password must be at least 3 characters'
    }
  },
  authority: {
    required: 'Role is required'
  }
};

export default validators;