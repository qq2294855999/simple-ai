import { useCallback, useState } from "react";

export function usePreventDoubleClickHook<T extends unknown[]>(handler: (...args: T) => Promise<void>) {
  const [loading, setLoading] = useState(false);

  const onClick = useCallback(async (...args: T) => {
    if (loading) {
      return;
    }
    setLoading(true);
    try {
      await handler(...args);
    } finally {
      setLoading(false);
    }
  }, [handler, loading]);

  return { onClick, loading };
}
