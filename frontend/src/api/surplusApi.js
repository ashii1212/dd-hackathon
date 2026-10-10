import { request } from './client';

export const surplusApi = {
  getPeople: () => request('/api/people'),
  getClock: () => request('/api/clock'),
  moveClock: (now) => request('/api/clock', { method: 'PUT', body: { now } }),

  getDonorDonations: (donorId) => request(`/api/donors/${donorId}/donations`),
  postDonation: (donorId, donation) =>
    request(`/api/donors/${donorId}/donations`, { method: 'POST', body: donation }),
  cancelDonation: (donorId, donationId) =>
    request(`/api/donors/${donorId}/donations/${donationId}/cancel`, { method: 'POST' }),

  getOpenDonations: (volunteerId) => request(`/api/volunteers/${volunteerId}/donations`),
  getVolunteerClaims: (volunteerId) => request(`/api/volunteers/${volunteerId}/claims`),
  claim: (donationId, claim) => request(`/api/donations/${donationId}/claims`, { method: 'POST', body: claim }),
  collectClaim: (volunteerId, claimId) =>
    request(`/api/volunteers/${volunteerId}/claims/${claimId}/collect`, { method: 'POST' }),
  cancelClaim: (volunteerId, claimId) =>
    request(`/api/volunteers/${volunteerId}/claims/${claimId}/cancel`, { method: 'POST' }),

  getDashboard: () => request('/api/dashboard')
};
