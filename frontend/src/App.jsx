import { useEffect, useState } from 'react';
import { surplusApi } from './api/surplusApi';
import ClockControl from './components/ClockControl';
import Dashboard from './components/Dashboard';
import DonorView from './components/DonorView';
import ErrorBox from './components/ErrorBox';
import VolunteerView from './components/VolunteerView';

export default function App() {
  const [people, setPeople] = useState(null);
  const [now, setNow] = useState(null);
  const [actingAs, setActingAs] = useState('');
  const [tab, setTab] = useState('person');
  const [refreshKey, setRefreshKey] = useState(0);
  const [error, setError] = useState(null);

  useEffect(() => {
    Promise.all([surplusApi.getPeople(), surplusApi.getClock()])
      .then(([peopleData, clock]) => {
        setPeople(peopleData);
        setNow(clock.now);
      })
      .catch(setError);
  }, []);

  const refresh = () => setRefreshKey((key) => key + 1);

  const handleClockChange = (newNow) => {
    setNow(newNow);
    refresh();
  };

  if (error) return <div className="container"><ErrorBox error={error} /></div>;
  if (!people || !now) return <div className="container muted">Loading...</div>;

  const [type, id] = actingAs.split(':');
  const donor = type === 'donor' ? people.donors.find((d) => d.id === id) : null;
  const volunteer = type === 'volunteer' ? people.volunteers.find((v) => v.id === id) : null;

  return (
    <div className="container">
      <header className="header">
        <div>
          <h1>SurplusLink</h1>
          <p className="tagline">Rescuing surplus restaurant food, one pickup at a time</p>
        </div>
        <ClockControl now={now} onChange={handleClockChange} />
      </header>

      <div className="toolbar">
        <label>
          Acting as{' '}
          <select value={actingAs} onChange={(e) => { setActingAs(e.target.value); setTab('person'); }}>
            <option value="">Choose a person...</option>
            <optgroup label="Donors">
              {people.donors.map((d) => <option key={d.id} value={`donor:${d.id}`}>{d.name} ({d.zone})</option>)}
            </optgroup>
            <optgroup label="Volunteers">
              {people.volunteers.map((v) => <option key={v.id} value={`volunteer:${v.id}`}>{v.name}</option>)}
            </optgroup>
          </select>
        </label>
        <div className="tabs">
          <button className={tab === 'person' ? '' : 'secondary'} onClick={() => setTab('person')}>My view</button>
          <button className={tab === 'dashboard' ? '' : 'secondary'} onClick={() => setTab('dashboard')}>Dashboard</button>
        </div>
      </div>

      {tab === 'dashboard' && <Dashboard refreshKey={refreshKey} />}
      {tab === 'person' && !donor && !volunteer && <p className="muted">Choose who you are acting as to get started.</p>}
      {tab === 'person' && donor && <DonorView donor={donor} now={now} refreshKey={refreshKey} onChanged={refresh} />}
      {tab === 'person' && volunteer && (
        <VolunteerView volunteer={volunteer} now={now} refreshKey={refreshKey} onChanged={refresh} />
      )}
    </div>
  );
}
