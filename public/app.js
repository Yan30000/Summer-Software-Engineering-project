/* Simple single-file frontend for SEMP prototype */
const api = {
  json: (path, opts={}) => {
    const token = localStorage.getItem('semp_token');
    opts.headers = opts.headers || {};
    if (token) opts.headers['Authorization'] = 'Bearer ' + token;
    return fetch(path, Object.assign({headers: {'Content-Type':'application/json'}}, opts))
      .then(r => r.json().catch(()=>({})).then(body => ({ status: r.status, body })));
  }
};

const content = document.getElementById('content');
const sidebar = document.getElementById('sidebar');
const navList = document.getElementById('nav-list');
const logoutBtn = document.getElementById('logoutBtn');

logoutBtn.addEventListener('click', () => {
  localStorage.removeItem('semp_token');
  localStorage.removeItem('semp_user');
  sidebar.classList.add('d-none');
  showRegistration();
});

function showRegistration(){
  sidebar.classList.add('d-none');
  content.innerHTML = `
    <div class="row justify-content-center align-items-center vh-100">
      <div class="col-md-6">
        <div class="card p-4 card-compact">
          <h4>Create account</h4>
          <input id="regEmail" class="form-control my-2" placeholder="Email">
          <input id="regPass" type="password" class="form-control my-2" placeholder="Password">
          <select id="regRole" class="form-select my-2"><option>Player</option><option>Coach</option></select>
          <div class="d-flex gap-2">
            <button id="doRegister" class="btn btn-primary">Register</button>
            <button id="toLogin" class="btn btn-outline-secondary">Already have account</button>
          </div>
        </div>
      </div>
    </div>`;

  document.getElementById('toLogin').onclick = showLogin;
  document.getElementById('doRegister').onclick = () => {
    const email = document.getElementById('regEmail').value.trim();
    const password = document.getElementById('regPass').value;
    const role = document.getElementById('regRole').value;
    if (!email || !password) return alert('Email + password required');
    api.json('/api/register', { method: 'POST', body: JSON.stringify({email,password,role}) })
      .then(res => {
        if (res.status === 200 && res.body.success) { alert('Registered — please log in'); showLogin(); }
        else alert(res.body.message || 'Registration error');
      });
  };
}

function showLogin(){
  sidebar.classList.add('d-none');
  content.innerHTML = `
    <div class="row justify-content-center align-items-center vh-100">
      <div class="col-md-6">
        <div class="card p-4 card-compact">
          <h4>Sign in</h4>
          <input id="loginEmail" class="form-control my-2" placeholder="Email">
          <input id="loginPass" type="password" class="form-control my-2" placeholder="Password">
          <div class="d-flex gap-2">
            <button id="doLogin" class="btn btn-primary">Sign in</button>
            <button id="toReg" class="btn btn-outline-secondary">Create account</button>
          </div>
        </div>
      </div>
    </div>`;

  document.getElementById('toReg').onclick = showRegistration;
  document.getElementById('doLogin').onclick = () => {
    const email = document.getElementById('loginEmail').value.trim();
    const password = document.getElementById('loginPass').value;
    if (!email || !password) return alert('Email + password required');
    api.json('/api/login', { method: 'POST', body: JSON.stringify({email,password}) })
      .then(res => {
        if (res.status === 200 && res.body.success) {
          localStorage.setItem('semp_token', res.body.token);
          localStorage.setItem('semp_user', JSON.stringify(res.body.user));
          initSidebar();
          showDashboard();
        } else {
          alert(res.body.message || 'Login failed');
        }
      });
  };
}

function initSidebar(){
  sidebar.classList.remove('d-none');
  navList.innerHTML = '';
  const items = [
    { id: 'dash', label: 'Dashboard', fn: showDashboard },
    { id: 'events', label: 'Events', fn: showEvents },
    { id: 'teams', label: 'Teams', fn: showTeams },
    { id: 'profile', label: 'Profile', fn: showProfile }
  ];
  items.forEach(it => {
    const li = document.createElement('li'); li.className = 'nav-item';
    const a = document.createElement('a'); a.href = '#'; a.className = 'nav-link text-white'; a.textContent = it.label;
    a.onclick = (e) => { e.preventDefault(); it.fn(); };
    li.appendChild(a); navList.appendChild(li);
  });
}

function showDashboard(){
  content.innerHTML = `<div class="d-flex justify-content-between align-items-center mb-3">
    <h3>Dashboard</h3>
  </div><div id="cards" class="row g-3"></div>`;
  // load events summary
  api.json('/api/events').then(res => {
    const events = res.body || [];
    const cards = document.getElementById('cards');
    cards.innerHTML = '';
    events.slice(0,3).forEach(ev => {
      const col = document.createElement('div'); col.className = 'col-md-4';
      col.innerHTML = `<div class="card p-3 card-compact"><div class="card-body">
        <div class="event-title">${ev.title}</div>
        <div class="small-muted">${ev.date}</div>
        <div class="mt-3"><button class="btn btn-sm btn-register">View</button></div>
      </div></div>`;
      cards.appendChild(col);
    });
  });
}

function showEvents(){
  content.innerHTML = `<div class="d-flex justify-content-between align-items-center mb-3">
    <h3>Events</h3>
  </div><div id="eventsList"></div>`;
  const list = document.getElementById('eventsList');
  api.json('/api/events').then(res => {
    const evs = res.body || [];
    list.innerHTML = '';
    evs.forEach(ev => {
      const row = document.createElement('div'); row.className = 'card mb-2 p-3 d-flex align-items-center';
      const title = document.createElement('div'); title.className = 'me-auto';
      title.innerHTML = `<div class="event-title">${ev.title}</div><div class="small-muted">${ev.date}</div>`;
      const btn = document.createElement('button'); btn.className = 'btn btn-register'; btn.textContent = 'Register';

      // check registration state
      api.json('/api/profile').then(pr => {
        const regs = (pr.body && pr.body.registrations) || [];
        const registered = regs.find(r => r.id === ev.id);
        if (registered) { btn.textContent = 'Registered'; btn.disabled = true; }
      });

      btn.onclick = () => {
        const token = localStorage.getItem('semp_token');
        if (!token) return alert('Please log in');
        btn.disabled = true;
        fetch('/api/events/' + ev.id + '/register', {method:'POST', headers: {'Authorization':'Bearer '+token}})
          .then(r => r.json()).then(j => { if (j.success) { btn.textContent='Registered'; alert('Registered'); } else { btn.disabled=false; alert(j.message||'Error'); } });
      };

      row.appendChild(title); row.appendChild(btn);
      row.style.display = 'flex'; row.style.justifyContent = 'space-between'; row.style.alignItems = 'center';
      list.appendChild(row);
    });
  });
}

function showTeams(){
  content.innerHTML = `<h3>Teams</h3><div id="teamsList"></div>`;
  api.json('/api/teams').then(res => {
    const list = document.getElementById('teamsList');
    list.innerHTML = '';
    (res.body || []).forEach(t => {
      const card = document.createElement('div'); card.className='card mb-2 p-3';
      card.innerHTML = `<div><strong>${t.name}</strong> <div class="small-muted">Coach: ${t.coach}</div></div>`;
      list.appendChild(card);
    });
  });
}

function showProfile(){
  content.innerHTML = `<h3>Profile</h3><div id="profileArea"></div>`;
  api.json('/api/profile').then(res => {
    if (res.status === 401) return alert('Not logged in');
    const area = document.getElementById('profileArea');
    const user = res.body.user || {};
    area.innerHTML = `<div><b>Email:</b> ${user.email}</div><div><b>Role:</b> ${user.role}</div><h5 class="mt-3">Your registrations</h5><div id="yourRegs"></div>`;
    const regs = res.body.registrations || [];
    const yourRegs = document.getElementById('yourRegs');
    if (!regs.length) yourRegs.innerHTML = '<div class="small-muted">No registrations</div>';
    regs.forEach(ev => {
      const row = document.createElement('div'); row.className='d-flex align-items-center card p-2 mb-2';
      row.innerHTML = `<div>${ev.title}<div class="small-muted">${ev.date}</div></div>`;
      const btn = document.createElement('button'); btn.className='btn btn-outline-danger ms-auto'; btn.textContent='Unregister';
      btn.onclick = () => {
        const token = localStorage.getItem('semp_token');
        fetch('/api/events/' + ev.id + '/unregister', {method:'POST', headers:{'Authorization':'Bearer '+token}})
          .then(r => r.json()).then(j => { if (j.success) { alert('Unregistered'); showProfile(); } else alert(j.message||'Error'); });
      };
      row.appendChild(btn); yourRegs.appendChild(row);
    });
  });
}

// Start: always show registration first (blueprint requirement). If already logged in, go to dashboard
if (localStorage.getItem('semp_token')) {
  initSidebar();
  showDashboard();
} else {
  showRegistration();
}