import { useEffect, useState } from 'react';
import { request } from './api/client';

export default function App() {
  const [status, setStatus] = useState('checking...');

  useEffect(() => {
    request('/api/health')
      .then((data) => setStatus(data.status))
      .catch(() => setStatus('DOWN'));
  }, []);

  return (
    <div className="container">
      <h1>Hello World</h1>
      <p className="muted">Backend: {status}</p>
    </div>
  );
}
