import { onCLS, onINP, onFCP, onLCP, onTTFB, Metric } from 'web-vitals'; // Replace get by on in the methods

type OnPerfEntry = (metric: Metric) => void;

const reportWebVitals = (onPerfEntry?: OnPerfEntry) => {
  if (!onPerfEntry) return;

  onCLS(onPerfEntry);
  onINP(onPerfEntry);
  onFCP(onPerfEntry);
  onLCP(onPerfEntry);
  onTTFB(onPerfEntry);
};

export default reportWebVitals;