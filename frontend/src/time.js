const pad = (n) => String(n).padStart(2, '0');

export function addMinutes(dateTime, minutes) {
  const [date, time] = dateTime.split('T');
  const [year, month, day] = date.split('-').map(Number);
  const [hour, minute] = time.split(':').map(Number);
  const d = new Date(year, month - 1, day, hour, minute + minutes);
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
}

export function formatTime(dateTime) {
  return dateTime ? dateTime.replace('T', ' ') : '';
}

export function laterOf(a, b) {
  return a > b ? a : b;
}
