const express = require('express');
const path = require('path');
const app = express();
app.use(express.json());

// Serve static frontend
app.use(express.static(path.join(__dirname, 'public')));

// In-memory data store
const users = new Map(); // email -> {email,password,role}
const events = [
  { id: 1, title: 'Summer Football Cup', date: '2024-06-20' },
  { id: 2, title: 'City League', date: '2024-05-05' },
  { id: 3, title: 'Training Session', date: '2024-06-12' }
];
const teams = [
  { id: 1, name: 'Red Strikers', coach: 'Coach A' },
  { id: 2, name: 'Blue Rangers', coach: 'Coach B' }
];
const registrations = new Map(); // email -> Set(eventId)

function authFromHeader(req) {
  const header = req.get('authorization');
  if (!header) return null;
  // token is just the email for this prototype
  const token = header.replace(/^Bearer\s+/, '');
  return users.get(token) || null;
}

app.post('/api/register', (req, res) => {
  const { email, password, role } = req.body;
  if (!email || !password) return res.status(400).json({ success: false, message: 'email and password required' });
  if (users.has(email)) return res.status(409).json({ success: false, message: 'email already registered' });
  users.set(email, { email, password, role: role || 'Player' });
  return res.json({ success: true });
});

app.post('/api/login', (req, res) => {
  const { email, password } = req.body;
  const u = users.get(email);
  if (!u || u.password !== password) return res.status(401).json({ success: false, message: 'invalid credentials' });
  // token = email (simple)
  return res.json({ success: true, token: email, user: { email: u.email, role: u.role } });
});

app.get('/api/events', (req, res) => {
  return res.json(events);
});

app.get('/api/teams', (req, res) => {
  return res.json(teams);
});

app.post('/api/events/:id/register', (req, res) => {
  const user = authFromHeader(req);
  if (!user) return res.status(401).json({ success: false, message: 'unauthorized' });
  const id = parseInt(req.params.id, 10);
  if (!events.find(e => e.id === id)) return res.status(404).json({ success: false, message: 'event not found' });
  const set = registrations.get(user.email) || new Set();
  if (set.has(id)) return res.status(409).json({ success: false, message: 'already registered' });
  set.add(id);
  registrations.set(user.email, set);
  return res.json({ success: true });
});

app.post('/api/events/:id/unregister', (req, res) => {
  const user = authFromHeader(req);
  if (!user) return res.status(401).json({ success: false, message: 'unauthorized' });
  const id = parseInt(req.params.id, 10);
  const set = registrations.get(user.email);
  if (!set || !set.has(id)) return res.status(404).json({ success: false, message: 'not registered' });
  set.delete(id);
  if (set.size === 0) registrations.delete(user.email);
  else registrations.set(user.email, set);
  return res.json({ success: true });
});

app.get('/api/profile', (req, res) => {
  const user = authFromHeader(req);
  if (!user) return res.status(401).json({ success: false, message: 'unauthorized' });
  const userRegs = Array.from(registrations.get(user.email) || []);
  const myEvents = events.filter(e => userRegs.includes(e.id));
  return res.json({ success: true, user: { email: user.email, role: user.role }, registrations: myEvents });
});

// Fallback to index.html for client-side routing
app.get('*', (req, res) => {
  res.sendFile(path.join(__dirname, 'public', 'index.html'));
});

const port = process.env.PORT || 3000;
app.listen(port, () => console.log('Server started on http://localhost:' + port));
