const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '';

export class ApiError extends Error {
  constructor(message, details) {
    super(message);
    this.details = details;
  }
}

export async function request(path, { method = 'GET', body } = {}) {
  const res = await fetch(`${API_BASE_URL}${path}`, {
    method,
    headers: body ? { 'Content-Type': 'application/json' } : undefined,
    body: body ? JSON.stringify(body) : undefined
  });

  if (res.status === 204) return null;

  const data = await res.json().catch(() => null);
  if (!res.ok) {
    const details = data?.violations || (data?.fieldErrors ? Object.values(data.fieldErrors) : []);
    throw new ApiError(data?.message || `Request failed (${res.status})`, details);
  }
  return data;
}
