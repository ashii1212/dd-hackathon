import { request } from './client';

export const itemApi = {
  getAll: (search = '') =>
    request(`/api/items${search.trim() ? `?search=${encodeURIComponent(search.trim())}` : ''}`),
  create: (item) => request('/api/items', { method: 'POST', body: item }),
  update: (id, item) => request(`/api/items/${id}`, { method: 'PUT', body: item }),
  remove: (id) => request(`/api/items/${id}`, { method: 'DELETE' })
};
