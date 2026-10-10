import { useEffect, useState } from 'react';
import { surplusApi } from '../api/surplusApi';
import { formatTime } from '../time';
import ErrorBox from './ErrorBox';
import StatusBadge from './StatusBadge';

const emptyForm = (now) => ({ description: '', quantityKg: '', pickupFrom: now, pickupUntil: now });

export default function DonorView({ donor, now, refreshKey, onChanged }) {
  const [donations, setDonations] = useState([]);
  const [form, setForm] = useState(() => emptyForm(now));
  const [formError, setFormError] = useState(null);
  const [listError, setListError] = useState(null);

  useEffect(() => {
    surplusApi.getDonorDonations(donor.id)
      .then((data) => {
        setDonations(data);
        setListError(null);
      })
      .catch(setListError);
  }, [donor.id, refreshKey]);

  useEffect(() => setForm(emptyForm(now)), [donor.id]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      setFormError(null);
      await surplusApi.postDonation(donor.id, { ...form, quantityKg: Number(form.quantityKg) });
      setForm(emptyForm(now));
      onChanged();
    } catch (err) {
      setFormError(err);
    }
  };

  const handleCancel = async (donationId) => {
    try {
      setListError(null);
      await surplusApi.cancelDonation(donor.id, donationId);
      onChanged();
    } catch (err) {
      setListError(err);
    }
  };

  const update = (field) => (e) => setForm({ ...form, [field]: e.target.value });

  return (
    <div>
      <h2>{donor.name} <span className="muted">({donor.zone})</span></h2>

      <form className="card form" onSubmit={handleSubmit}>
        <h3>Post surplus food</h3>
        <div className="form-grid">
          <label>Description<input value={form.description} onChange={update('description')} /></label>
          <label>Quantity (kg)<input type="number" min="1" step="1" value={form.quantityKg} onChange={update('quantityKg')} /></label>
          <label>Pickup from<input type="datetime-local" value={form.pickupFrom} onChange={update('pickupFrom')} /></label>
          <label>Pickup until<input type="datetime-local" value={form.pickupUntil} onChange={update('pickupUntil')} /></label>
        </div>
        <button type="submit">Post donation</button>
        <ErrorBox error={formError} />
      </form>

      <h3>My donations</h3>
      <ErrorBox error={listError} />
      {donations.length === 0 && <p className="muted">No donations yet.</p>}
      {donations.map((donation) => (
        <div key={donation.id} className="card">
          <div className="row">
            <div>
              <strong>{donation.id} · {donation.description}</strong> <StatusBadge status={donation.status} />
              <div className="muted">
                {donation.remainingKg} of {donation.quantityKg} kg remaining · pickup {formatTime(donation.pickupFrom)} – {formatTime(donation.pickupUntil)}
              </div>
            </div>
            {donation.status === 'OPEN' && (
              <button className="danger" onClick={() => handleCancel(donation.id)}>Cancel</button>
            )}
          </div>
          {donation.claims.length > 0 && (
            <table>
              <thead>
                <tr><th>Claim</th><th>Volunteer</th><th>Kg</th><th>Planned pickup</th><th>Status</th></tr>
              </thead>
              <tbody>
                {donation.claims.map((claim) => (
                  <tr key={claim.id}>
                    <td>#{claim.id}</td>
                    <td>{claim.volunteerName}</td>
                    <td>{claim.kg}</td>
                    <td>{formatTime(claim.plannedTime)}</td>
                    <td><StatusBadge status={claim.status} /></td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      ))}
    </div>
  );
}
