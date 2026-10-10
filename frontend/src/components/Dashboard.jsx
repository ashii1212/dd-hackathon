import { useEffect, useState } from 'react';
import { surplusApi } from '../api/surplusApi';
import ErrorBox from './ErrorBox';

const FIGURES = [
  ['listedKg', 'Listed'],
  ['collectedKg', 'Collected'],
  ['missedKg', 'Missed'],
  ['wastedKg', 'Wasted'],
  ['inProgressKg', 'In progress'],
  ['stillOpenKg', 'Still open']
];

export default function Dashboard({ refreshKey }) {
  const [data, setData] = useState(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    surplusApi.getDashboard().then(setData).catch(setError);
  }, [refreshKey]);

  if (error) return <ErrorBox error={error} />;
  if (!data) return <p className="muted">Loading...</p>;

  const { totals } = data;

  return (
    <div>
      <h2>Dashboard</h2>
      <div className="stats">
        {FIGURES.map(([key, label]) => (
          <div key={key} className="card stat">
            <div className="muted">{label}</div>
            <div className="stat-value">{totals[key]} kg</div>
          </div>
        ))}
      </div>

      <div className={`card ${totals.balanced ? 'ok' : 'error'}`}>
        Listed {totals.listedKg} = {totals.collectedKg} + {totals.missedKg} + {totals.wastedKg} + {totals.inProgressKg} + {totals.stillOpenKg}
        {' '}<strong>{totals.balanced ? '✓ balanced' : '✗ not balanced'}</strong>
      </div>

      <div className="two-columns">
        <table className="card">
          <thead><tr><th>Donor</th><th>Collected kg</th></tr></thead>
          <tbody>
            {data.donors.map((donor) => (
              <tr key={donor.id}><td>{donor.name}</td><td>{donor.collectedKg}</td></tr>
            ))}
          </tbody>
        </table>
        <table className="card">
          <thead><tr><th>Volunteer</th><th>Collected kg</th><th>Missed claims</th></tr></thead>
          <tbody>
            {data.volunteers.map((volunteer) => (
              <tr key={volunteer.id}>
                <td>{volunteer.name}</td><td>{volunteer.collectedKg}</td><td>{volunteer.missedClaims}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
