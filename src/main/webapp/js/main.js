/**
 * REC CampusFind - Core Application Script
 * Clean student state, zero mock role switchers, responsive navigation, and toast notifications.
 */
try {
  localStorage.removeItem('campus_lf_user');
  localStorage.removeItem('campus_lf_role');
  localStorage.removeItem('currentUser');
} catch (e) {}

// SVG Icon Library
const Icons = {
  search: '<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="11" cy="11" r="8"></circle><line x1="21" y1="21" x2="16.65" y2="16.65"></line></svg>',
  mapPin: '<svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path><circle cx="12" cy="10" r="3"></circle></svg>',
  calendar: '<svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect><line x1="16" y1="2" x2="16" y2="6"></line><line x1="8" y1="2" x2="8" y2="6"></line><line x1="3" y1="10" x2="21" y2="10"></line></svg>',
  phone: '<svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"></path></svg>',
  mail: '<svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"></path><polyline points="22,6 12,13 2,6"></polyline></svg>',
  checkCircle: '<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path><polyline points="22 4 12 14.01 9 11.01"></polyline></svg>',
  alertTriangle: '<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"></path><line x1="12" y1="9" x2="12" y2="13"></line><line x1="12" y1="17" x2="12.01" y2="17"></line></svg>',
  info: '<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="16" x2="12" y2="12"></line><line x1="12" y1="8" x2="12.01" y2="8"></line></svg>',
  close: '<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="6" x2="6" y2="18"></line><line x1="6" y1="6" x2="18" y2="18"></line></svg>'
};

// UI Manager
const UI = {
  escapeHTML: function(str) {
    if (str === null || str === undefined) return '';
    return String(str)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#39;');
  },

  showToast: function(title, message, type = 'info') {
    let container = document.getElementById('toast-container');
    if (!container) {
      container = document.createElement('div');
      container.id = 'toast-container';
      container.className = 'toast-container';
      document.body.appendChild(container);
    }

    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    
    let iconSvg = Icons.info;
    if (type === 'success') iconSvg = Icons.checkCircle;
    if (type === 'error') iconSvg = Icons.alertTriangle;

    toast.innerHTML = `
      <div class="toast-icon">${iconSvg}</div>
      <div class="toast-content">
        <div class="toast-title">${title}</div>
        <div class="toast-message">${message}</div>
      </div>
    `;

    container.appendChild(toast);

    requestAnimationFrame(() => {
      toast.classList.add('show');
    });

    setTimeout(() => {
      toast.classList.remove('show');
      setTimeout(() => toast.remove(), 300);
    }, 4000);
  },

  renderStatusBadge: function(status) {
    const s = (status || '').toUpperCase();
    if (s === 'OPEN') return `<span class="badge badge-open"><span class="badge-dot"></span>Active</span>`;
    if (s === 'CLAIM_PENDING' || s === 'PENDING') return `<span class="badge badge-pending"><span class="badge-dot"></span>In Contact</span>`;
    if (s === 'RESOLVED' || s === 'APPROVED') return `<span class="badge badge-resolved"><span class="badge-dot"></span>Reunited</span>`;
    return `<span class="badge badge-neutral">${status}</span>`;
  },

  renderTypeBadge: function(type) {
    const t = (type || '').toUpperCase();
    if (t === 'LOST') return `<span class="badge badge-lost">Lost</span>`;
    return `<span class="badge badge-found">Found</span>`;
  },

  formatDate: function(dateStr) {
    if (!dateStr) return '';
    const d = new Date(dateStr);
    return d.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
  },

  // In-app modal confirmation dialog (replaces browser confirm)
  confirmDialog: function(options) {
    const {
      title = 'Delete Listing?',
      message = 'This item will be permanently removed from the campus feed if you click delete.',
      confirmText = 'Yes, Delete',
      cancelText = 'Cancel',
      isDestructive = true,
      onConfirm = () => {}
    } = options || {};

    const existing = document.getElementById('custom-confirm-modal');
    if (existing) existing.remove();

    const overlay = document.createElement('div');
    overlay.id = 'custom-confirm-modal';
    overlay.className = 'custom-modal-overlay';
    overlay.innerHTML = `
      <div class="custom-modal-dialog">
        <div class="custom-modal-icon ${isDestructive ? 'danger' : 'primary'}">
          <svg xmlns="http://www.w3.org/2000/svg" width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <polyline points="3 6 5 6 21 6"></polyline>
            <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
            <line x1="10" y1="11" x2="10" y2="17"></line>
            <line x1="14" y1="11" x2="14" y2="17"></line>
          </svg>
        </div>
        <div class="custom-modal-body">
          <h3 class="custom-modal-title">${UI.escapeHTML(title)}</h3>
          <p class="custom-modal-desc">${UI.escapeHTML(message)}</p>
        </div>
        <div class="custom-modal-actions">
          <button type="button" class="btn btn-secondary custom-modal-cancel">${UI.escapeHTML(cancelText)}</button>
          <button type="button" class="btn btn-danger custom-modal-confirm">${UI.escapeHTML(confirmText)}</button>
        </div>
      </div>
    `;

    document.body.appendChild(overlay);

    const close = () => {
      overlay.classList.remove('active');
      setTimeout(() => overlay.remove(), 250);
    };

    overlay.querySelector('.custom-modal-cancel').addEventListener('click', close);
    overlay.addEventListener('click', (e) => {
      if (e.target === overlay) close();
    });

    overlay.querySelector('.custom-modal-confirm').addEventListener('click', () => {
      close();
      if (typeof onConfirm === 'function') {
        onConfirm();
      }
    });

    requestAnimationFrame(() => {
      overlay.classList.add('active');
    });
  },

  // Navbar Setup: Pure session-based state
  initNavbar: async function() {
    // Check server session
    const user = await API.checkSession();
    const navActions = document.getElementById('nav-actions');

    // Clean out any legacy admin/demo links from all nav bars
    document.querySelectorAll('.nav-link').forEach(link => {
      const href = link.getAttribute('href');
      if (href === 'admin.html' || href === 'my-items.html') {
        if (!user && href === 'my-items.html') link.remove();
        if (href === 'admin.html') link.remove();
      }
    });

    if (navActions) {
      if (user && user.email) {
        navActions.innerHTML = `
          <div class="user-profile-badge">
            <div class="user-avatar-circle">${UI.escapeHTML(user.avatar || 'REC')}</div>
            <div class="user-meta-info">
              <span class="user-display-name">${UI.escapeHTML(user.fullName || user.name || 'Student')}</span>
              <span class="user-email-text">${UI.escapeHTML(user.email)}</span>
            </div>
            <button id="logout-btn" class="btn btn-outline btn-sm logout-trigger" title="Sign out">
              Logout
            </button>
          </div>
        `;

        const logoutBtn = document.getElementById('logout-btn');
        if (logoutBtn) {
          logoutBtn.addEventListener('click', async () => {
            await API.logout();
            UI.showToast('Signed Out', 'You have been signed out.', 'info');
            setTimeout(() => window.location.href = 'index.html', 400);
          });
        }
      } else {
        navActions.innerHTML = `
          <div style="display: flex; align-items: center; gap: 8px;">
            <a href="login.html" class="btn btn-primary btn-sm">
              Sign In
            </a>
          </div>
        `;
      }
    }

    // Highlight current page nav link
    const currentPath = window.location.pathname.split('/').pop() || 'index.html';
    document.querySelectorAll('.nav-link').forEach(link => {
      const href = link.getAttribute('href');
      if (href && (href === currentPath || (currentPath === '' && href === 'index.html'))) {
        link.classList.add('active');
      } else {
        link.classList.remove('active');
      }
    });

    // Mobile Hamburger Toggle
    const mobileToggle = document.getElementById('mobile-nav-toggle');
    const navLinks = document.getElementById('nav-links');
    if (mobileToggle && navLinks) {
      mobileToggle.addEventListener('click', () => {
        navLinks.classList.toggle('show');
      });
    }
  }
};

// Auto-run on DOMContentLoaded
document.addEventListener('DOMContentLoaded', () => {
  UI.initNavbar();
});

window.UI = UI;
window.Icons = Icons;
