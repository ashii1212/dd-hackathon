import { useEffect, useState } from 'react';
import { itemApi } from './api/itemApi';

const EMPTY_FORM = { name: '', description: '' };

export default function App() {
  const [items, setItems] = useState([]);
  const [search, setSearch] = useState('');
  const [form, setForm] = useState(EMPTY_FORM);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');

  const loadItems = async (query) => {
    try {
      setIsLoading(true);
      setError('');
      setItems(await itemApi.getAll(query));
    } catch (err) {
      setError(err.message);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    const timeout = setTimeout(() => loadItems(search), 250);
    return () => clearTimeout(timeout);
  }, [search]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!form.name.trim()) {
      setError('Name is required');
      return;
    }
    try {
      await itemApi.create(form);
      setForm(EMPTY_FORM);
      loadItems(search);
    } catch (err) {
      setError(err.message);
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this item?')) return;
    try {
      await itemApi.remove(id);
      loadItems(search);
    } catch (err) {
      setError(err.message);
    }
  };

  return (
    <div className="container">
      <h1>Items</h1>

      <form className="card form" onSubmit={handleSubmit}>
        <input
          placeholder="Name"
          value={form.name}
          maxLength={100}
          onChange={(e) => setForm({ ...form, name: e.target.value })}
        />
        <input
          placeholder="Description (optional)"
          value={form.description}
          maxLength={500}
          onChange={(e) => setForm({ ...form, description: e.target.value })}
        />
        <button type="submit">Add</button>
      </form>

      {error && <div className="error">{error}</div>}

      <input
        className="search"
        placeholder="Search by name..."
        value={search}
        onChange={(e) => setSearch(e.target.value)}
      />

      {isLoading ? (
        <p className="muted">Loading...</p>
      ) : items.length === 0 ? (
        <p className="muted">No items yet.</p>
      ) : (
        <ul className="list">
          {items.map((item) => (
            <li key={item.id} className="card row">
              <div>
                <strong>{item.name}</strong>
                {item.description && <div className="muted">{item.description}</div>}
              </div>
              <button className="danger" onClick={() => handleDelete(item.id)}>Delete</button>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
