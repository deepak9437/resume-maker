// Base backend URL - THIS MUST BE THE FULL URL
const backendBaseUrl = 'http://localhost:8080/api';

// Utility to show simple alerts
    function alertMsg(msg, isError = false) {
      console.log(msg);
      let alertBox = document.getElementById('customAlertBox');
      if (!alertBox) {
        alertBox = document.createElement('div');
        alertBox.id = 'customAlertBox';
        alertBox.style.position = 'fixed';
        alertBox.style.top = '20px';
        alertBox.style.left = '50%';
        alertBox.style.transform = 'translateX(-50%)';
        alertBox.style.padding = '16px';
        alertBox.style.borderRadius = '8px';
        alertBox.style.boxShadow = '0 4px 12px rgba(0,0,0,0.15)';
        alertBox.style.zIndex = '9999';
        alertBox.style.fontWeight = '500';
        alertBox.style.minWidth = '300px';
        alertBox.style.textAlign = 'center';
        alertBox.style.opacity = '0';
        alertBox.style.transition = 'opacity 0.3s ease';
        document.body.appendChild(alertBox);
      }

      alertBox.textContent = msg;
      if (isError) {
        alertBox.style.backgroundColor = '#f8d7da';
        alertBox.style.color = '#721c24';
      } else {
        alertBox.style.backgroundColor = '#d4edda';
        alertBox.style.color = '#155724';
      }

      setTimeout(() => { alertBox.style.opacity = '1'; }, 10);
      setTimeout(() => { alertBox.style.opacity = '0'; }, 3000);
    }

    /* ------------------ Event Listeners for Page Load ------------------ */
    document.addEventListener('DOMContentLoaded', () => {
      const loginForm = document.getElementById('loginForm');
      if (loginForm) loginForm.addEventListener('submit', handleLogin);

      const registerForm = document.getElementById('registerForm');
      if (registerForm) registerForm.addEventListener('submit', handleRegister);

      ['subscribeForm', 'subscribeForm2', 'subscribeForm3', 'subscribeForm4', 'subscribeForm5'].forEach(id => {
        const form = document.getElementById(id);
        if (form) form.addEventListener('submit', (e) => { e.preventDefault(); alertMsg('Thanks for subscribing!'); });
      });

      const pathname = document.location.pathname;
      if (pathname.endsWith('home.html')) { if (!localStorage.getItem('userId')) console.warn('Not logged in.'); }
      if (pathname.endsWith('form.html')) initializeResumeForm();
      if (pathname.endsWith('preview.html')) initializePreviewPage();
      if (pathname.endsWith('forgot-password.html')) initializeForgotPassword();
    });

    /* ------------------ Authentication Handlers ------------------ */
    async function handleLogin(e) {
      e.preventDefault();
      const username = document.getElementById('username').value.trim();
      const password = document.getElementById('password').value.trim();
      try {
        const res = await fetch(`${backendBaseUrl}/auth/login`, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ username, password }) });
        const data = await res.json();
        if (res.ok && data.status === 'ok') { localStorage.setItem('userId', data.userId); localStorage.setItem('userName', data.userName); window.location.href = 'home.html'; }
        else alertMsg(data.error || 'Login failed. Please check your credentials.', true);
      } catch (err) { alertMsg('Server error. Failed to fetch.', true); console.error(err); }
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
        const res = await fetch(`${backendBaseUrl}/auth/register`, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload) });
        const data = await res.json();
        if (res.ok && data.status === 'ok') { alertMsg('Registration successful! Please log in.'); window.location.href = 'index.html'; }
        else alertMsg(data.error || 'Registration failed.', true);
      } catch (err) { alertMsg('Server error. Failed to fetch.', true); console.error(err); }
    }

    /* ------------------ Forgot Password Handlers ------------------ */
    function initializeForgotPassword() {
      const sendOtpForm = document.getElementById('sendOtpForm');
      const resetPasswordForm = document.getElementById('resetPasswordForm');
      if (sendOtpForm) sendOtpForm.addEventListener('submit', handleSendOtp);
      if (resetPasswordForm) resetPasswordForm.addEventListener('submit', handleResetPassword);
    }

    async function handleSendOtp(e) {
      e.preventDefault();
      const mobile = document.getElementById('mobile').value.trim();
      const sendOtpSection = document.getElementById('send-otp-section');
      const resetSection = document.getElementById('reset-section');
      const otpHint = document.getElementById('otp-hint');
      if (!mobile) { alertMsg('Please enter a mobile number.', true); return; }
      try {
        const res = await fetch(`${backendBaseUrl}/auth/forgot-password/send-otp`, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ mobile }) });
        const data = await res.json();
        if (res.ok && data.otp) {
          document.getElementById('resetMobile').value = mobile;
          if (otpHint) { otpHint.innerHTML = `Demo OTP (for testing): <strong>${data.otp}</strong>`; otpHint.style.display = 'block'; }
          if (sendOtpSection) sendOtpSection.style.display = 'none';
          if (resetSection) resetSection.style.display = 'block';
        } else { alertMsg(data.error || 'Mobile number not found.', true); }
      } catch (err) { alertMsg('Server error. Failed to fetch.', true); console.error(err); }
    }

    async function handleResetPassword(e) {
      e.preventDefault();
      const mobile = document.getElementById('resetMobile').value;
      const otp = document.getElementById('otp').value.trim();
      const newPassword = document.getElementById('newPassword').value.trim();
      const otpHint = document.getElementById('otp-hint');
      const payload = { mobile, otp, newPassword };
      try {
        const res = await fetch(`${backendBaseUrl}/auth/forgot-password/reset`, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload) });
        const data = await res.json();
        if (res.ok && data.status === 'ok') { alertMsg('Password reset successful! Please log in.'); if (otpHint) otpHint.style.display = 'none'; window.location.href = 'index.html'; }
        else alertMsg(data.error || 'Invalid or expired OTP.', true);
      } catch (err) { alertMsg('Server error. Failed to fetch.', true); console.error(err); }
    }

    /* ------------------ Page Initializers (Existing) ------------------ */
    function initializeResumeForm() {
      const params = new URLSearchParams(location.search);
      const template = params.get('template') || 'template1';
      const templateIdInput = document.getElementById('templateId'); if (templateIdInput) templateIdInput.value = template;
      const resumeForm = document.getElementById('resumeForm'); if (resumeForm) resumeForm.addEventListener('submit', async (ev) => { ev.preventDefault(); await submitResume(); });
    }

    function initializePreviewPage() {
      const params = new URLSearchParams(location.search);
      const resumeId = params.get('id'); if (!resumeId) { alertMsg('No resume ID provided.', true); return; }
      loadPreview(resumeId);
      const downloadBtn = document.getElementById('downloadPdf'); if (downloadBtn) downloadBtn.addEventListener('click', () => { window.open(`${backendBaseUrl}/resumes/${resumeId}/export`, '_blank'); });
    }

    /* ------------------ Core Resume Logic (Existing) ------------------ */
    async function fileToBase64(file) {
      if (!file) return null;
      if (file.size > 1_000_000) { alertMsg('Image is too large. Please use an image under 1 MB.', true); return null; }
      return new Promise((resolve, reject) => { const reader = new FileReader(); reader.onload = () => { const base64String = reader.result.split(',')[1]; resolve(base64String); }; reader.onerror = (error) => reject(error); reader.readAsDataURL(file); });
    }

    // Build preview UI (used by loadPreview)
    function buildPreviewUI(r) {
      const area = document.getElementById('previewArea'); if (!area) return; area.innerHTML = '';
      const createSection = (title, content) => { const section = document.createElement('div'); section.className = 'mb-3'; const h5 = document.createElement('h5'); h5.className = 'text-primary'; h5.textContent = title; const p = document.createElement('p'); p.style.whiteSpace = 'pre-wrap'; p.textContent = content || ''; section.appendChild(h5); section.appendChild(p); return section; };
      const leftCol = document.createElement('div'); leftCol.className = 'col-md-9'; const header = document.createElement('h2'); header.textContent = r.fullName || ''; leftCol.appendChild(header);
      const contactInfo = document.createElement('p'); contactInfo.className = 'text-muted'; const emailStrong = document.createElement('strong'); emailStrong.textContent = 'Email:'; const mobileStrong = document.createElement('strong'); mobileStrong.textContent = 'Mobile:'; contactInfo.append(emailStrong, ` ${r.email || ''} | `, mobileStrong, ` ${r.mobile || ''}`); leftCol.appendChild(contactInfo); leftCol.appendChild(document.createElement('hr'));
      leftCol.appendChild(createSection('Profile', r.summary)); leftCol.appendChild(createSection('Skills', r.skills)); leftCol.appendChild(createSection('Education', r.education)); leftCol.appendChild(createSection('Experience', r.experience)); leftCol.appendChild(createSection('Hobbies', r.hobbies));
      const rightCol = document.createElement('div'); rightCol.className = 'col-md-3 text-center'; if (r.imageBase64) { const img = document.createElement('img'); img.src = `data:image/jpeg;base64,${r.imageBase64}`; img.className = 'img-fluid rounded-circle shadow-sm'; img.style.width = '150px'; img.style.height = '150px'; img.style.objectFit = 'cover'; img.alt = 'Profile Picture'; rightCol.appendChild(img); }
      const row = document.createElement('div'); row.className = 'row'; row.appendChild(leftCol); row.appendChild(rightCol); area.appendChild(row);
    }

    // Improved submitResume with detailed error handling
    async function submitResume() {
      const userId = localStorage.getItem('userId'); if (!userId) { alertMsg('You must be logged in to create a resume.', true); return; }
      const imageInput = document.getElementById('profileImage'); let imageBase64 = null; if (imageInput && imageInput.files && imageInput.files[0]) { imageBase64 = await fileToBase64(imageInput.files[0]); if (!imageBase64) return; }

      const payload = { userId, templateId: document.getElementById('templateId').value, fullName: document.getElementById('fullName').value.trim(), mobile: document.getElementById('mobile').value.trim(), email: document.getElementById('email').value.trim(), summary: document.getElementById('summary').value.trim(), skills: document.getElementById('skills').value.trim(), education: document.getElementById('education').value.trim(), experience: document.getElementById('experience').value.trim(), hobbies: document.getElementById('hobbies').value.trim(), imageBase64 };

      try {
        const res = await fetch(`${backendBaseUrl}/resumes/create`, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload) });

        // Parse body (JSON or text)
        let responseBody = null; const contentType = res.headers.get('content-type') || ''; if (contentType.includes('application/json')) { responseBody = await res.json(); } else { responseBody = await res.text(); }

        if (res.ok) {
          const resumeId = responseBody && responseBody.resumeId ? responseBody.resumeId : null;
          if (resumeId) { localStorage.setItem('tempResumeData', JSON.stringify(payload)); window.location.href = `preview.html?id=${resumeId}`; }
          else { alertMsg('Resume created but server did not return an ID.', false); }
        } else {
          const serverMessage = (responseBody && responseBody.error) ? responseBody.error : (typeof responseBody === 'string' ? responseBody : JSON.stringify(responseBody));
          alertMsg(`Failed to create resume. Status: ${res.status} ${res.statusText}. ${serverMessage}`, true);
          console.error('Create resume failed', res.status, res.statusText, responseBody);
        }
      } catch (err) {
        // Likely network/CORS error
        alertMsg(`Network error: ${err.message}. If you're opening the HTML files directly (file://), serve the frontend over http:// to avoid CORS issues.`, true);
        console.error('Network error while creating resume', err);
      }
    }

    async function loadPreview(id) {
      const cachedData = localStorage.getItem('tempResumeData');
      if (cachedData) {
        try { localStorage.removeItem('tempResumeData'); const r = JSON.parse(cachedData); buildPreviewUI(r); const editBtn = document.getElementById('editBtn'); if (editBtn) editBtn.href = `form.html?template=${r.templateId}&edit=${id}`; return; } catch (e) { console.error('Failed to parse cached data, falling back to fetch.', e); }
      }

      try {
        const res = await fetch(`${backendBaseUrl}/resumes/${id}`);
        if (!res.ok) { alertMsg('Failed to fetch resume data for preview.', true); return; }
        const r = await res.json(); buildPreviewUI(r); const editBtn = document.getElementById('editBtn'); if (editBtn) editBtn.href = `form.html?template=${r.templateId}&edit=${id}`;
      } catch (err) { alertMsg('Server error. Failed to fetch.', true); console.error(err); }
    }
