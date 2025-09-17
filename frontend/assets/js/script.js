// Base backend URL - change if your backend runs on a different host/port
const backendBaseUrl = 'http://localhost:8080/api';

// Utility to show simple alerts
function alertMsg(msg) {
  alert(msg);
}

/* ------------------ Event Listeners for Page Load ------------------ */
document.addEventListener('DOMContentLoaded', () => {
  // Attach event listener for the login form if it exists
  const loginForm = document.getElementById('loginForm');
  if (loginForm) {
    loginForm.addEventListener('submit', handleLogin);
  }

  // Attach event listener for the registration form if it exists
  const registerForm = document.getElementById('registerForm');
  if (registerForm) {
    registerForm.addEventListener('submit', handleRegister);
  }

  // Attach event listeners for all subscription forms
  ['subscribeForm', 'subscribeForm2', 'subscribeForm3', 'subscribeForm4', 'subscribeForm5'].forEach(id => {
    const form = document.getElementById(id);
    if (form) {
      form.addEventListener('submit', (e) => {
        e.preventDefault();
        alert('Thanks for subscribing! (This is a demo feature)');
      });
    }
  });

  // Page-specific initializations
  const pathname = document.location.pathname;

  if (pathname.endsWith('home.html')) {
    // Optional: Protect the home page from non-logged-in users
    if (!localStorage.getItem('userId')) {
      // You could redirect them to the login page
      // window.location.href = 'index.html';
      console.warn('Not logged in. Allowing access for demo purposes.');
    }
  }

  if (pathname.endsWith('form.html')) {
    initializeResumeForm();
  }

  if (pathname.endsWith('preview.html')) {
    initializePreviewPage();
  }
});

/* ------------------ Authentication Handlers ------------------ */
async function handleLogin(e) {
  e.preventDefault();
  const username = document.getElementById('username').value.trim();
  const password = document.getElementById('password').value.trim();

  try {
    const res = await fetch(`${backendBaseUrl}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, password })
    });
    const data = await res.json();
    if (res.ok && data.status === 'ok') {
      localStorage.setItem('userId', data.userId);
      localStorage.setItem('userName', data.userName);
      window.location.href = 'home.html';
    } else {
      alertMsg(data.error || 'Login failed. Please check your credentials.');
    }
  } catch (err) {
    alertMsg('Server error: ' + err.message);
  }
}

async function handleRegister(e) {
  e.preventDefault();
  const fullName = document.getElementById('fullName').value.trim();
  const email = document.getElementById('email').value.trim();
  const mobile = document.getElementById('mobile').value.trim();
  const userId = document.getElementById('userId').value.trim();
  const password = document.getElementById('regPassword').value.trim();

  const payload = { fullName, email, mobile, userId, password };

  try {
    const res = await fetch(`${backendBaseUrl}/auth/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });
    const data = await res.json();
    if (res.ok && data.status === 'ok') {
      alertMsg('Registration successful! Please log in.');
      window.location.href = 'index.html';
    } else {
      alertMsg(data.error || 'Registration failed.');
    }
  } catch (err) {
    alertMsg('Server error: ' + err.message);
  }
}

/* ------------------ Page Initializers ------------------ */
function initializeResumeForm() {
  const params = new URLSearchParams(location.search);
  const template = params.get('template') || 'template1';
  const templateIdInput = document.getElementById('templateId');
  if (templateIdInput) {
    templateIdInput.value = template;
  }

  const resumeForm = document.getElementById('resumeForm');
  if (resumeForm) {
    resumeForm.addEventListener('submit', async (ev) => {
      ev.preventDefault();
      await submitResume();
    });
  }
}

function initializePreviewPage() {
  const params = new URLSearchParams(location.search);
  const resumeId = params.get('id');
  if (!resumeId) {
    alertMsg('No resume ID provided.');
    return;
  }
  loadPreview(resumeId);

  const downloadBtn = document.getElementById('downloadPdf');
  if (downloadBtn) {
    downloadBtn.addEventListener('click', () => {
      window.open(`${backendBaseUrl}/resumes/${resumeId}/export`, '_blank');
    });
  }

  const editBtn = document.getElementById('editBtn');
  if (editBtn) {
    // The href will be set after the resume data is loaded in loadPreview
  }
}

/* ------------------ Core Resume Logic ------------------ */

/**
 * Converts a file to a Base64 encoded string.
 * @param {File} file The file to convert.
 * @returns {Promise<string|null>} The Base64 string or null on error.
 */
async function fileToBase64(file) {
  if (!file) return null;
  // Limit file size to 1MB
  if (file.size > 1_000_000) {
    alertMsg('Image is too large. Please use an image under 1 MB.');
    return null;
  }
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = () => {
      // Result format is "data:image/jpeg;base64,XXXX..."
      // We only want the part after the comma
      const base64String = reader.result.split(',')[1];
      resolve(base64String);
    };
    reader.onerror = (error) => reject(error);
    reader.readAsDataURL(file);
  });
}

async function submitResume() {
  const userId = localStorage.getItem('userId');
  if (!userId) {
    alertMsg('You must be logged in to create a resume.');
    return;
  }

  const imageInput = document.getElementById('profileImage');
  let imageBase64 = null;
  if (imageInput.files && imageInput.files[0]) {
    imageBase64 = await fileToBase64(imageInput.files[0]);
    if (!imageBase64) return; // Stop if file conversion failed
  }

  const payload = {
    userId,
    templateId: document.getElementById('templateId').value,
    fullName: document.getElementById('fullName').value.trim(),
    mobile: document.getElementById('mobile').value.trim(),
    email: document.getElementById('email').value.trim(),
    summary: document.getElementById('summary').value.trim(),
    skills: document.getElementById('skills').value.trim(),
    education: document.getElementById('education').value.trim(),
    experience: document.getElementById('experience').value.trim(),
    hobbies: document.getElementById('hobbies').value.trim(),
    imageBase64
  };

  try {
    const res = await fetch(`${backendBaseUrl}/resumes/create`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });
    const data = await res.json();
    if (res.ok) {
      // Redirect to the preview page with the new resume ID
      window.location.href = `preview.html?id=${data.resumeId}`;
    } else {
      alertMsg(data.error || 'Failed to create resume.');
    }
  } catch (err) {
    alertMsg('Server error: ' + err.message);
  }
}

/**
 * **SECURITY-FIXED VERSION**
 * Fetches resume data and safely populates the preview area.
 * @param {string} id The ID of the resume to load.
 */
async function loadPreview(id) {
  try {
    const res = await fetch(`${backendBase_url}/resumes/${id}`);
    if (!res.ok) {
      alertMsg('Failed to fetch resume data for preview.');
      return;
    }
    const r = await res.json();
    const area = document.getElementById('previewArea');
    area.innerHTML = ''; // Clear previous content

    // Safely create and append elements to prevent XSS
    const createSection = (title, content) => {
      const section = document.createElement('div');
      const h5 = document.createElement('h5');
      h5.textContent = title;
      const p = document.createElement('p');
      p.textContent = content || '';
      section.appendChild(h5);
      section.appendChild(p);
      return section;
    };

    const leftCol = document.createElement('div');
    leftCol.className = 'col-md-9';

    const header = document.createElement('h2');
    header.textContent = r.fullName || '';
    leftCol.appendChild(header);

    const contactInfo = document.createElement('p');
    const emailStrong = document.createElement('strong');
    emailStrong.textContent = 'Email:';
    const mobileStrong = document.createElement('strong');
    mobileStrong.textContent = 'Mobile:';
    contactInfo.append(emailStrong, ` ${r.email || ''} | `, mobileStrong, ` ${r.mobile || ''}`);
    leftCol.appendChild(contactInfo);

    leftCol.appendChild(createSection('Profile', r.summary));
    leftCol.appendChild(createSection('Skills', r.skills));
    leftCol.appendChild(createSection('Education', r.education));
    leftCol.appendChild(createSection('Experience', r.experience));
    leftCol.appendChild(createSection('Hobbies', r.hobbies));

    const rightCol = document.createElement('div');
    rightCol.className = 'col-md-3';

    if (r.imageBase64) {
      const img = document.createElement('img');
      img.src = `data:image/jpeg;base64,${r.imageBase64}`;
      img.className = 'img-fluid rounded';
      img.alt = 'Profile Picture';
      rightCol.appendChild(img);
    }

    const row = document.createElement('div');
    row.className = 'row';
    row.appendChild(leftCol);
    row.appendChild(rightCol);

    area.appendChild(row);

    // Set the edit button link
    const editBtn = document.getElementById('editBtn');
    if (editBtn) {
      editBtn.href = `form.html?template=${r.templateId}&edit=${id}`;
    }

  } catch (err) {
    alertMsg('Server error: ' + err.message);
  }
}