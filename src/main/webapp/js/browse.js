/**
 * Rajalakshmi Engineering College - Lost & Found Feed Script
 * Mobile-first card feed with direct phone, WhatsApp, and email contact
 */

document.addEventListener('DOMContentLoaded', async () => {
  const urlParams = new URLSearchParams(window.location.search);

  // State
  let currentType = (urlParams.get('type') || 'ALL').toUpperCase();
  let searchQuery = urlParams.get('query') || '';

  // DOM Elements
  const grid = document.getElementById('items-grid');
  const searchInput = document.getElementById('search-input');
  const pillAll = document.getElementById('pill-all');
  const pillLost = document.getElementById('pill-lost');
  const pillFound = document.getElementById('pill-found');
  const countAll = document.getElementById('count-all');
  const countLost = document.getElementById('count-lost');
  const countFound = document.getElementById('count-found');

  // Pre-fill search from URL
  if (searchQuery && searchInput) searchInput.value = searchQuery;

  // Set active pill
  function setActivePill(type) {
    currentType = type;
    document.querySelectorAll('.pill-btn').forEach(b => b.classList.remove('active'));
    const activeBtn = document.querySelector(`.pill-btn[data-type="${type}"]`);
    if (activeBtn) activeBtn.classList.add('active');
    renderFeed();
  }

  // Pill click handlers
  if (pillAll) pillAll.addEventListener('click', () => setActivePill('ALL'));
  if (pillLost) pillLost.addEventListener('click', () => setActivePill('LOST'));
  if (pillFound) pillFound.addEventListener('click', () => setActivePill('FOUND'));

  // Debounced search
  let debounceTimeout;
  if (searchInput) {
    searchInput.addEventListener('input', (e) => {
      clearTimeout(debounceTimeout);
      debounceTimeout = setTimeout(() => {
        searchQuery = e.target.value.trim();
        renderFeed();
      }, 250);
    });
  }

  // Render the feed
  async function renderFeed() {
    if (!grid) return;
    grid.innerHTML = `
      <div style="grid-column: 1 / -1; text-align: center; padding: 48px 20px;">
        <div class="spinner"></div>
        <p style="font-size: 13px; color: var(--text-muted); margin-top: 12px;">Loading campus feed...</p>
      </div>
    `;

    try {
      const filters = {
        type: currentType,
        query: searchQuery,
        sortBy: 'newest'
      };

      const [items, allItems] = await Promise.all([
        API.getItems(filters),
        API.getItems({})
      ]);

      // Update pill counts
      const lostCount = allItems.filter(i => (i.type || i.itemType) === 'LOST').length;
      const foundCount = allItems.filter(i => (i.type || i.itemType) === 'FOUND').length;
      if (countAll) countAll.textContent = allItems.length;
      if (countLost) countLost.textContent = lostCount;
      if (countFound) countFound.textContent = foundCount;

      if (!items || items.length === 0) {
        grid.innerHTML = `
          <div style="grid-column: 1 / -1; text-align: center; padding: 60px 20px; background: #fff; border-radius: var(--border-radius-lg); border: 1px solid var(--border-color);">
            <div style="width: 56px; height: 56px; border-radius: 50%; background: var(--bg-secondary); color: var(--text-muted); display: flex; align-items: center; justify-content: center; margin: 0 auto 14px auto;">
              <svg xmlns="http://www.w3.org/2000/svg" width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="11" cy="11" r="8"></circle>
                <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
              </svg>
            </div>
            <h3 style="font-size: 17px; font-weight: 700; color: var(--text-primary); margin-bottom: 6px;">No Items Found</h3>
            <p style="font-size: 13px; color: var(--text-muted); margin-bottom: 20px; max-width: 380px; margin-left: auto; margin-right: auto;">
              ${searchQuery ? `No reports matched "${UI.escapeHTML(searchQuery)}". Try searching for a different keyword.` : 'There are currently no reported lost or found items in this category.'}
            </p>
            <a href="report.html" class="btn btn-primary btn-sm">Report a Lost or Found Item</a>
          </div>
        `;
        return;
      }

      // Current authenticated session
      let currentUser = await API.checkSession();

      grid.innerHTML = items.map(item => {
        const phone = item.contactPhone || (item.user && item.user.phone) || '';
        const email = item.contactEmail || (item.user && item.user.email) || '';
        const hasPhoto = item.imageUrl && item.imageUrl.trim() !== '';
        const isLost = (item.type || item.itemType) === 'LOST';
        const typeClass = isLost ? 'badge-lost' : 'badge-found';
        const typeLabel = isLost ? 'Lost' : 'Found';
        const spot = UI.escapeHTML(item.specificSpot || item.location || 'REC Campus');
        const desc = item.description ? UI.escapeHTML(item.description) : '';
        const cleanPhone = phone.replace(/[^0-9]/g, '');

        // Only the student who posted this item (or an admin) can see the delete button
        const isOwner = !!(currentUser && (
          (currentUser.id && item.userId && Number(currentUser.id) === Number(item.userId)) ||
          (currentUser.userId && item.userId && Number(currentUser.userId) === Number(item.userId)) ||
          (currentUser.email && item.contactEmail && currentUser.email.toLowerCase() === item.contactEmail.toLowerCase()) ||
          (currentUser.email && item.user && item.user.email && currentUser.email.toLowerCase() === item.user.email.toLowerCase())
        ));
        const isAdmin = !!(currentUser && currentUser.role === 'ADMIN');
        const canDelete = isOwner || isAdmin;

        return `
          <div class="mobile-item-card">
            <div class="card-photo-wrapper">
              ${hasPhoto 
                ? `<img src="${item.imageUrl}" alt="${UI.escapeHTML(item.title)}" class="card-photo-img" loading="lazy" onerror="this.onerror=null;this.parentElement.innerHTML='<div class=\\'card-photo-empty\\'>Image unavailable</div>'">`
                : `<div class="card-photo-empty">
                    <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><rect x="3" y="3" width="18" height="18" rx="2"></rect><circle cx="8.5" cy="8.5" r="1.5"></circle><polyline points="21 15 16 10 5 21"></polyline></svg>
                    <span>No Photo Provided</span>
                  </div>`
              }
              <div style="position: absolute; top: 10px; left: 10px;">
                <span class="badge ${typeClass}">${typeLabel}</span>
              </div>
              <div style="position: absolute; top: 10px; right: ${canDelete ? '78px' : '10px'}; font-size: 11px; font-weight: 600; background: rgba(0,0,0,0.65); color: #fff; padding: 3px 8px; border-radius: 6px;">
                ${UI.formatDate(item.date)}
              </div>
              ${canDelete ? `
                <button type="button" class="card-delete-icon-btn" onclick="handleFeedItemDelete(${item.id}, event)" title="Delete your post">
                  <svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><polyline points="3 6 5 6 21 6"></polyline><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path><line x1="10" y1="11" x2="10" y2="17"></line><line x1="14" y1="11" x2="14" y2="17"></line></svg>
                  <span>Delete</span>
                </button>
              ` : ''}
            </div>

            <div class="card-body">
              <h3 class="card-title">${UI.escapeHTML(item.title)}</h3>
              
              <div class="card-spot">
                <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="color: var(--accent-primary);"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path><circle cx="12" cy="10" r="3"></circle></svg>
                <span>${spot}</span>
              </div>

              ${desc ? `
                <p style="font-size: 12px; color: var(--text-muted); line-height: 1.5; margin-bottom: 12px; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;">
                  ${desc}
                </p>
              ` : ''}

              <div class="card-contact-box" style="margin-top: 14px; padding-top: 10px; border-top: 1px solid var(--border-color);">
                <div style="font-size: 11px; font-weight: 700; color: var(--text-muted); text-transform: uppercase; letter-spacing: 0.5px; margin-bottom: 6px;">Direct Contact</div>
                <div style="display: flex; gap: 8px;">
                  ${phone ? `
                    <a href="tel:${UI.escapeHTML(phone)}" class="contact-action-btn btn-call" style="flex: 1;">
                      <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"></path></svg>
                      <span>Call</span>
                    </a>
                    <a href="https://wa.me/91${cleanPhone}?text=Hi,%20I%20am%20contacting%20you%20regarding%20the%20${encodeURIComponent(item.title)}%20listed%20on%20REC%20CampusFind" target="_blank" class="contact-action-btn btn-whatsapp" style="flex: 1.2;">
                      <svg xmlns="http://www.w3.org/2000/svg" width="15" height="15" viewBox="0 0 24 24" fill="currentColor"><path d="M.057 24l1.687-6.163c-1.041-1.804-1.588-3.849-1.587-5.946.003-6.556 5.338-11.891 11.893-11.891 3.181.001 6.167 1.24 8.413 3.488 2.245 2.248 3.481 5.236 3.48 8.414-.003 6.557-5.338 11.892-11.893 11.892-1.99-.001-3.951-.5-5.688-1.448l-6.305 1.654zm6.597-3.807c1.676.995 3.276 1.591 5.392 1.592 5.448 0 9.886-4.434 9.889-9.885.002-5.462-4.415-9.89-9.881-9.892-5.452 0-9.887 4.434-9.889 9.884-.001 2.225.651 3.891 1.746 5.634l-.999 3.648 3.742-.981zm11.387-5.464c-.074-.124-.272-.198-.57-.347-.297-.149-1.758-.868-2.031-.967-.272-.099-.47-.149-.669.149-.198.297-.768.967-.941 1.165-.173.198-.347.223-.644.074-.297-.149-1.255-.462-2.39-1.475-.883-.788-1.48-1.761-1.653-2.059-.173-.297-.018-.458.13-.606.134-.133.297-.347.446-.521.151-.172.2-.296.3-.495.099-.198.05-.372-.025-.521-.075-.148-.669-1.611-.916-2.206-.242-.579-.487-.501-.669-.51l-.57-.01c-.198 0-.52.074-.792.372s-1.04 1.016-1.04 2.479 1.065 2.876 1.213 3.074c.149.198 2.095 3.2 5.076 4.487.709.306 1.263.489 1.694.626.712.226 1.36.194 1.872.118.571-.085 1.758-.719 2.006-1.413.248-.695.248-1.29.173-1.414z"/></svg>
                      <span>WhatsApp</span>
                    </a>
                  ` : ''}
                  ${email ? `
                    <a href="mailto:${UI.escapeHTML(email)}?subject=REC CampusFind: Regarding ${encodeURIComponent(item.title)}" class="contact-action-btn btn-mail" style="flex: 1;">
                      <svg xmlns="http://www.w3.org/2000/svg" width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"></path><polyline points="22,6 12,13 2,6"></polyline></svg>
                      <span>Email</span>
                    </a>
                  ` : ''}
                </div>
              </div>

              ${canDelete ? `
                <div style="margin-top: 12px; padding-top: 8px; border-top: 1px dashed var(--border-color); display: flex; justify-content: space-between; align-items: center;">
                  <span style="font-size: 11px; color: var(--success); font-weight: 600; display: inline-flex; align-items: center; gap: 4px;">
                    <svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"></polyline></svg>
                    Your Listing
                  </span>
                  <button type="button" class="btn-delete-card-pill" onclick="handleFeedItemDelete(${item.id}, event)" title="Delete this post">
                    <svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"></polyline><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path></svg>
                    Delete Post
                  </button>
                </div>
              ` : ''}
            </div>
          </div>
        `;
      }).join('');

    } catch (err) {
      console.error('renderFeed error:', err);
      grid.innerHTML = `
        <div style="grid-column: 1 / -1; text-align: center; padding: 40px; color: var(--text-muted);">
          <p>Unable to load listings right now.</p>
          <button class="btn btn-secondary btn-sm" onclick="location.reload()" style="margin-top: 10px;">Retry</button>
        </div>
      `;
    }
  }

  // Global handler for deleting a feed item
  window.handleFeedItemDelete = function(itemId, event) {
    if (event) {
      event.preventDefault();
      event.stopPropagation();
    }

    UI.confirmDialog({
      title: 'Delete Listing?',
      message: 'This item will be permanently removed from the campus feed if you click delete.',
      confirmText: 'Yes, Delete',
      cancelText: 'Cancel',
      isDestructive: true,
      onConfirm: async () => {
        try {
          await API.deleteItem(itemId);
          UI.showToast('Listing Removed', 'The item has been removed from the campus feed.', 'success');
          setTimeout(() => {
            renderFeed();
          }, 350);
        } catch (err) {
          UI.showToast('Delete Failed', err.message || 'Unable to delete listing.', 'error');
        }
      }
    });
  };

  // Initial render
  setActivePill(currentType);
});

