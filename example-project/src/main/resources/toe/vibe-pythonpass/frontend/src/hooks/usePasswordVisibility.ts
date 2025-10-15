import { useState, useEffect, useCallback } from 'react';

interface UsePasswordVisibilityResult {
  isVisible: boolean;
  countdown: number;
  togglePasswordVisibility: () => void;
  hidePassword: () => void;
}

export function usePasswordVisibility(duration: number = 30): UsePasswordVisibilityResult {
  const [isVisible, setIsVisible] = useState(false);
  const [countdown, setCountdown] = useState(duration);

  const hidePassword = useCallback(() => {
    setIsVisible(false);
    setCountdown(duration);
  }, [duration]);

  useEffect(() => {
    let intervalId: NodeJS.Timeout | null = null;

    if (isVisible) {
      const startTime = Date.now();
      intervalId = setInterval(() => {
        const elapsedTime = (Date.now() - startTime) / 1000;
        const newCountdown = Math.max(duration - elapsedTime, 0);
        setCountdown(newCountdown);

        if (newCountdown === 0) {
          hidePassword();
        }
      }, 100); // Update every 100ms for smooth animation
    }

    return () => {
      if (intervalId) {
        clearInterval(intervalId);
      }
    };
  }, [isVisible, hidePassword, duration]);

  const togglePasswordVisibility = useCallback(() => {
    setIsVisible(prev => !prev);
    setCountdown(duration);
  }, [duration]);

  return {
    isVisible,
    countdown,
    togglePasswordVisibility,
    hidePassword
  };
}