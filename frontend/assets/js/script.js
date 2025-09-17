// PLACEHOLDER: frontend JS
// Base backend URL - change if your backend runs on different host/port
const backendBaseUrl = 'http://localhost:8080/api';

// Utility to show alerts (simple)
function alertMsg(msg) {
  alert(msg);
}

/* ------------------ AUTH: Login & Register ------------------ */
document.addEventListener('DOMContentLoaded', () => {
  const loginForm = document.getElementById('loginForm');
  if (loginForm) {
    loginForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      const username = document.getElementById('username').value.trim();
      const password = document.getElementById('password').value.trim();
      try {
        const res = await fetch(`${backendBaseUrl}/auth/login`, {
          method: 'POST',
          headers: {'Content-Type':'application/json'},
          body: JSON.stringify({ username, password })
        });
        const data = await res.json();
        if (res.ok) {
          localStorage.setItem('userId', data.userId);
          localStorage.setItem('userName', data.userName);
          window.location.href = 'home.html';
        } else {
          alertMsg(data.error || 'Login failed');
        }
      } catch (err) {
        alertMsg('Server error: ' + err.message);
      }
    });
  }

  const registerForm = document.getElementById('registerForm');
  if (registerForm) {
    registerForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      const fullName = document.getElementById('fullName').value.trim();
      const email = document.getElementById('email').value.trim();
      const mobile = document.getElementById('mobile').value.trim();
      const userId = document.getElementById('userId').value.trim();
      const password = document.getElementById('regPassword').value.trim();
      try {
        const res = await fetch(`${backendBaseUrl}/auth/register`, {
          method: 'POST',
          headers: {'Content-Type':'application/json'},
          body: JSON.stringify({ fullName, email, mobile, userId, password })
        });
        const data = await res.json();
        if (res.ok) {
          alertMsg('Register success. Please login.');
          window.location.href = 'index.html';
        } else {
          alertMsg(data.error || 'Registration failed');
        }
      } catch (err) {
        alertMsg('Server error: ' + err.message);
      }
    });
  }

  // subscribe forms (all subscribe forms have similar ids)
  ['subscribeForm','subscribeForm2','subscribeForm3','subscribeForm4','subscribeForm5'].forEach(id => {
    const f = document.getElementById(id);
    if (f) {
      f.addEventListener('submit', (e) => {
        e.preventDefault();
        alert('Thanks for subscribing! (demo only)');
      });
    }
  });

  // Home -> set a welcome or guard
  if (document.location.pathname.endsWith('home.html')) {
    if (!localStorage.getItem('userId')) {
      // not logged in
      // you may redirect to login
      // window.location.href = 'index.html';
    }
  }

  // Form page: populate templateId
  if (document.location.pathname.endsWith('form.html')) {
    const params = new URLSearchParams(location.search);
    const template = params.get('template') || 'template1';
    const templateIdInput = document.getElementById('templateId');
    if (templateIdInput) templateIdInput.value = template;

    const resumeForm = document.getElementById('resumeForm');
    if (resumeForm) {
      const submitBtn = document.getElementById('submitResume');
      submitBtn.addEventListener('click', async (ev) => {
        ev.preventDefault();
        await submitResume();
      });
    }
  }

  // Preview page
  if (document.location.pathname.endsWith('preview.html')) {
    const params = new URLSearchParams(location.search);
    const id = params.get('id');
    if (!id) {
      alertMsg('No resume id provided');
    } else {
      loadPreview(id);
      const dl = document.getElementById('downloadPdf');
      if (dl) dl.addEventListener('click', () => {
        window.open(`${backendBaseUrl}/resumes/${id}/export`, '_blank');
      });
    }
  }
});

/* ------------------ Helpers ------------------ */
async function fileToBase64(file) {
  if (!file) return null;
  if (file.size > 1_000_000) { // 1MB limit
    alertMsg('Image too large. Please use under 1 MB.');
    return null;
  }
  return await new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = () => {
      const result = reader.result; // data:*/*;base64,XXXX
      const parts = result.split(',');
      resolve(parts[1]); // base64 only
    };
    reader.onerror = (e) => reject(e);
    reader.readAsDataURL(file);
  });
}

/* ------------------ Submit Resume ------------------ */
async function submitResume() {
  const userId = localStorage.getItem('userId');
  if (!userId) {
    alertMsg('Please login first.');
    return;
  }
  const templateId = document.getElementById('templateId').value;
  const fullName = document.getElementById('fullName').value.trim();
  const mobile = document.getElementById('mobile').value.trim();
  const email = document.getElementById('email').value.trim();
  const summary = document.getElementById('summary').value.trim();
  const skills = document.getElementById('skills').value.trim();
  const education = document.getElementById('education').value.trim();
  const experience = document.getElementById('experience').value.trim();
  const hobbies = document.getElementById('hobbies').value.trim();
  const fileInput = document.getElementById('profileImage');
  let imageBase64 = null;
  if (fileInput && fileInput.files && fileInput.files[0]) {
    imageBase64 = await fileToBase64(fileInput.files[0]);
    if (!imageBase64) return; // size or error
  }

  const payload = {
    userId,
    templateId,
    fullName,
    mobile,
    email,
    summary,
    skills,
    education,
    experience,
    hobbies,
    imageBase64
  };

  try {
    const res = await fetch(`${backendBaseUrl}/resumes/create`, {
      method: 'POST',
      headers: {'Content-Type':'application/json'},
      body: JSON.stringify(payload)
    });
    const data = await res.json();
    if (res.ok) {
      const resumeId = data.resumeId;
      // redirect to preview with id
      window.location.href = `preview.html?id=${resumeId}`;
    } else {
      alertMsg(data.error || 'Failed to create resume');
    }
  } catch (err) {
    alertMsg('Server error: ' + err.message);
  }
}

/* ------------------ Load Preview ------------------ */
async function loadPreview(id) {
  try {
    const res = await fetch(`${backendBaseUrl}/resumes/${id}`);
    if (!res.ok) {
      alertMsg('Failed to fetch resume for preview');
      return;
    }
    const r = await res.json();
    const area = document.getElementById('previewArea');
    area.innerHTML = `
      <div class="row">
        <div class="col-md-9">
          <h2>${r.fullName || ''}</h2>
          <p><strong>Email:</strong> ${r.email || ''} | <strong>Mobile:</strong> ${r.mobile || ''}</p>
          <h5>Profile</h5><p>${r.summary || ''}</p>
          <h5>Skills</h5><p>${r.skills || ''}</p>
          <h5>Education</h5><p>${r.education || ''}</p>
          <h5>Experience</h5><p>${r.experience || ''}</p>
          <h5>Hobbies</h5><p>${r.hobbies || ''}</p>
        </div>
        <div class="col-md-3">
          ${r.imageBase64 ? `<img src="data:image/*;base64,${r.imageBase64}" class="img-fluid rounded" alt="profile">` : ''}
        </div>
      </div>
    `;
    const editBtn = document.getElementById('editBtn');
    if (editBtn) {
      editBtn.href = `form.html?template=${r.templateId}&edit=${id}`;
    }
  } catch (err) {
    alertMsg('Server error: ' + err.message);
  }
}
