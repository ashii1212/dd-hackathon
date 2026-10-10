import { useEffect, useState } from 'react';
import { surplusApi } from '../api/surplusApi';
import { formatTime, laterOf } from '../time';
import ErrorBox from './ErrorBox';
import StatusBadge from './StatusBadge';

function ClaimForm({ volunteer, donation, now, onClaimed }) {
  const [kg, setKg] = useState('');
  const [plannedTime, setPlannedTime] = useState(laterOf(donation.pickupFrom, now));
  const [error, setError] = useState(null);

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      setError(null);
      await surplusApi.claim(donation.id, { volunteerId: volunteer.id, kg: Number(kg), plannedTime });
      setKg('');
      onClaimed();
    } catch (err) {
      setError(err);
    }
  };

  return (
    <form className="claim-form" onSubmit={handleSubmit}>
      <input type="number" min="1" step="1" placeholder="kg" value={kg} onChange={(e) => setKg(e.target.value)} />
      <input type="datetime-local" value={plannedTime} onChange={(e) => setPlannedTime(e.target.value)} />
      <button type="submit">Claim</button>
      <ErrorBox error={error} />
    </form>
  );
}

export default function VolunteerView({ volunteer, now, refreshKey, onChanged }) {
  const [donations, setDonations] = useState([]);
  const [claims, setClaims] = useState([]);
  const [error, setError] = useState(null);

  useEffect(() => {
    Promise.all([surplusApi.getOpenDonations(volunteer.id), surplusApi.getVolunteerClaims(volunteer.id)])
      .then(([openDonations, myClaims]) => {
        setDonations(openDonations);
        setClaims(myClaims);
        setError(null);
      })
      .catch(setError);
  }, [volunteer.id, refreshKey]);

  const act = async (action, claimId) => {
    try {
      setError(null);
      await action(volunteer.id, claimId);
      onChanged();
    } catch (err) {
      setError(err);
    }
  };

  return (
    <div>
      <h2>{volunteer.name} <span className="muted">(capacity {volunteer.capacityKg} kg · zones {volunteer.zones.join(', ')})</span></h2>

      <h3>Open donations in my zones</h3>
      {donations.length === 0 && <p className="muted">No open donations right now.</p>}
      {donations.map((donation) => (
        <div key={`${donation.id}-${refreshKey}`} className="card">
          <strong>{donation.id} · {donation.description}</strong>{' '}
          <span className="muted">from {donation.donorName} ({donation.zone})</span>
          <div className="muted">
            {donation.remainingKg} of {donation.quantityKg} kg remaining · pickup {formatTime(donation.pickupFrom)} – {formatTime(donation.pickupUntil)}
          </div>
          <ClaimForm volunteer={volunteer} donation={donation} now={now} onClaimed={onChanged} />
        </div>
      ))}

      <h3>My claims</h3>
      <ErrorBox error={error} />
      {claims.length === 0 ? (
        <p className="muted">No claims yet.</p>
      ) : (
        <table className="card">
          <thead>
            <tr><th>Claim</th><th>Donation</th><th>Kg</th><th>Planned pickup</th><th>Window</th><th>Status</th><th></th></tr>
          </thead>
          <tbody>
            {claims.map((claim) => (
              <tr key={claim.id}>
                <td>#{claim.id}</td>
                <td>{claim.donationId} · {claim.donationDescription} ({claim.donorName})</td>
                <td>{claim.kg}</td>
                <td>{formatTime(claim.plannedTime)}</td>
                <td>{formatTime(claim.pickupFrom)} – {formatTime(claim.pickupUntil)}</td>
                <td><StatusBadge status={claim.status} /></td>
                <td>
                  {claim.status === 'ACTIVE' && (
                    <div className="actions">
                      <button onClick={() => act(surplusApi.collectClaim, claim.id)}>Collect</button>
                      <button className="danger" onClick={() => act(surplusApi.cancelClaim, claim.id)}>Cancel</button>
                    </div>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
