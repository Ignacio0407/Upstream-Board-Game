import { useEffect, useState } from 'react';

export default function useForceReload() {
    const [value, setValue] = useState<boolean>(false);

    useEffect(() => {}, [value]);

    return () => setValue(!value);
}