import { useEffect, useState } from 'react';
import { surplusApi } from '../api/surplusApi';
import { addMinutes, formatTime } from '../time';
import ErrorBox from './ErrorBox';

export default function ClockControl({ now, onChange }) {
  const [target, setTarget] = useState(now);
  const [error, setError] = useState(null);

  useEffect(() => setTarget(now), [now]);

  const move = async (time) => {
    try {
      setError(null);
      const result = await surplusApi.moveClock(time);
      onChange(result.now);
    } catch (err) {
      setError(err);
    }
  };

  return (
    <div className="clock">
      <div>
        <span className="muted">Now</span> <strong>{formatTime(now)}</strong>
      </div>
      <div className="clock-actions">
        <button className="secondary" onClick={() => move(addMinutes(now, 30))}>+30 min</button>
        <button className="secondary" onClick={() => move(addMinutes(now, 60))}>+1 h</button>
        <input type="datetime-local" value={target} min={now} onChange={(e) => setTarget(e.target.value)} />
        <button onClick={() => move(target)} disabled={!target}>Set</button>
      </div>
      <ErrorBox error={error} />
    </div>
  );
}
