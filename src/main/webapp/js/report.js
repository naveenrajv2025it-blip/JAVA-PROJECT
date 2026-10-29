/**
 * REC CampusFind - Lost & Found Report Script
 * Streamlined: Item name, Where lost/found, Date, Direct Phone & Email, Photo upload
 * Auto-creates student session seamlessly if not signed in!
 */

document.addEventListener('DOMContentLoaded', async () => {
  // Check if session exists
  const user = await API.checkSession();

  const urlParams = new URLSearchParams(window.location.search);
  const initialType = (urlParams.get('type') || 'LOST').toUpperCase();

  // Elements
  const form = document.getElementById('report-item-form');
  const typeInput = document.getElementById('report-type');
  const toggleLostBtn = document.getElementById('toggle-lost-btn');
  const toggleFoundBtn = document.getElementById('toggle-found-btn');
  const pageHeading = document.getElementById('page-heading');
  const submitBtn = document.getElementById('submit-report-btn');

  const titleInput = document.getElementById('item-title');
  const spotInput = document.getElementById('item-spot');
  const dateInput = document.getElementById('item-date');
  const phoneInput = document.getElementById('item-phone');
  const emailInput = document.getElementById('item-email');
  const descInput = document.getElementById('item-desc');
  const photoBox = document.getElementById('photo-box');
  const fileInput = document.getElementById('file-input');
  const imageUrlInput = document.getElementById('item-image-url');
  const uploadedPhotoPreview = document.getElementById('uploaded-photo-preview');
  const photoStatusText = document.getElementById('photo-status-text');

  // If user is already authenticated, prefill and lock email
  if (user && user.email) {
    if (emailInput) {
      emailInput.value = user.email;
      emailInput.readOnly = true;
      emailInput.style.backgroundColor = '#F1F5F9';
      emailInput.style.cursor = 'not-allowed';
    }
    if (phoneInput && user.phone && user.phone !== 'Not provided') {
      phoneInput.value = user.phone;
    }
  }

  // Set today's date default
  const today = new Date().toISOString().split('T')[0];
  if (dateInput) {
    dateInput.value = today;
    dateInput.max = today;
  }

  // Toggle Function: Lost vs Found
  function setType(type) {
    const isLost = type === 'LOST';
    typeInput.value = type;

    if (isLost) {
      toggleLostBtn.className = 'type-toggle-btn active lost';
      toggleFoundBtn.className = 'type-toggle-btn';
      pageHeading.textContent = 'Report a Lost Belonging';
      submitBtn.textContent = 'Publish Lost Report';
      submitBtn.style.backgroundColor = '#E11D48';
    } else {
      toggleFoundBtn.className = 'type-toggle-btn active found';
      toggleLostBtn.className = 'type-toggle-btn';
      pageHeading.textContent = 'Report a Found Belonging';
      submitBtn.textContent = 'Publish Found Item Report';
      submitBtn.style.backgroundColor = '#059669';
    }
  }

  toggleLostBtn.addEventListener('click', () => setType('LOST'));
  toggleFoundBtn.addEventListener('click', () => setType('FOUND'));

  // Initialize with URL parameter
  setType(initialType === 'FOUND' ? 'FOUND' : 'LOST');

  // Photo Upload Handler (Click & Drag-Drop)
  if (photoBox && fileInput) {
    photoBox.addEventListener('click', () => fileInput.click());

    photoBox.addEventListener('dragover', (e) => {
      e.preventDefault();
      photoBox.style.borderColor = 'var(--accent-primary)';
      photoBox.style.backgroundColor = 'var(--accent-light)';
    });

    photoBox.addEventListener('dragleave', () => {
      photoBox.style.borderColor = '';
      photoBox.style.backgroundColor = '';
    });

    photoBox.addEventListener('drop', (e) => {
      e.preventDefault();
      photoBox.style.borderColor = '';
      photoBox.style.backgroundColor = '';
      if (e.dataTransfer.files && e.dataTransfer.files[0]) {
        handleImageFile(e.dataTransfer.files[0]);
      }
    });

    fileInput.addEventListener('change', (e) => {
      if (e.target.files && e.target.files[0]) {
        handleImageFile(e.target.files[0]);
      }
    });
  }

  function handleImageFile(file) {
    if (!file.type.startsWith('image/')) {
      UI.showToast('Invalid File', 'Please select a valid image file (JPG, PNG, WEBP).', 'error');
      return;
    }

    if (file.size > 5 * 1024 * 1024) {
      UI.showToast('Image Too Large', 'Please choose an image under 5MB.', 'error');
      return;
    }

    const reader = new FileReader();
    reader.onload = function(evt) {
      const dataUrl = evt.target.result;
      imageUrlInput.value = dataUrl;
      
      uploadedPhotoPreview.src = dataUrl;
      uploadedPhotoPreview.style.display = 'block';
      photoStatusText.textContent = `Attached: ${file.name}`;
      photoStatusText.style.color = '#059669';

      UI.showToast('Photo Attached', 'Photo ready to be published with listing.', 'success');
    };
    reader.readAsDataURL(file);
  }

  // Form Validation & Seamless Submission
  form.addEventListener('submit', async (e) => {
    e.preventDefault();

    const title = titleInput.value.trim();
    const spot = spotInput.value.trim();
    const date = dateInput.value;
    const phone = phoneInput.value.trim();
    const email = emailInput.value.trim().toLowerCase();
    const desc = descInput.value.trim();
    const imageUrl = imageUrlInput.value.trim();

    if (!title) {
      UI.showToast('Item Name Required', 'Please type what item was lost or found.', 'error');
      titleInput.focus();
      return;
    }

    if (!spot) {
      UI.showToast('Location Required', 'Please specify where on campus it was lost or found.', 'error');
      spotInput.focus();
      return;
    }

    if (!phone || phone.length < 7) {
      UI.showToast('Phone Number Required', 'Please provide a valid phone or WhatsApp number.', 'error');
      phoneInput.focus();
      return;
    }

    if (!email) {
      UI.showToast('Email Required', 'Please enter your student email.', 'error');
      emailInput.focus();
      return;
    }

    if (!email.endsWith('@rajalakshmi.edu.in')) {
      UI.showToast('Invalid College Email', 'Please use your official email ending with @rajalakshmi.edu.in', 'error');
      emailInput.focus();
      return;
    }

    submitBtn.disabled = true;
    submitBtn.textContent = 'Publishing to Campus Feed...';

    // Auto-authenticate student if not currently logged in
    try {
      if (!API.isAuthenticated()) {
        await API.login(email);
      }
    } catch (authErr) {
      UI.showToast('Sign In Error', authErr.message || 'Could not verify student email.', 'error');
      submitBtn.disabled = false;
      submitBtn.textContent = typeInput.value === 'LOST' ? 'Publish Lost Report' : 'Publish Found Item Report';
      return;
    }

    const itemPayload = {
      type: typeInput.value,
      title: title,
      specificSpot: spot,
      location: spot,
      date: date,
      description: desc || `Reported at: ${spot}. Direct contact: ${phone} / ${email}`,
      imageUrl: imageUrl,
      contactPhone: phone,
      contactEmail: email
    };

    try {
      await API.createItem(itemPayload);
      UI.showToast('Report Published!', 'Your item is live on the campus feed.', 'success');
      
      setTimeout(() => {
        window.location.href = 'browse.html';
      }, 600);

    } catch (err) {
      UI.showToast('Submission Failed', err.message || 'Could not publish report. Please try again.', 'error');
      submitBtn.disabled = false;
      submitBtn.textContent = typeInput.value === 'LOST' ? 'Publish Lost Report' : 'Publish Found Item Report';
    }
  });
});
