/**
 * Rajalakshmi Engineering College - Item Details Script
 * Shows item photo, details, direct contact, and claim workflow
 */

document.addEventListener('DOMContentLoaded', async () => {
  const urlParams = new URLSearchParams(window.location.search);
  const itemId = urlParams.get('id');
  const container = document.getElementById('details-container');
  const currentUser = await API.checkSession();

  if (!itemId) {
    showNotFound('Invalid item reference ID.');
    return;
  }

  const item = await API.getItemById(itemId);
  if (!item) {
    showNotFound('The requested campus listing does not exist or has been removed.');
    return;
  }

  // Pre-fill modal user info
  const claimantInfo = document.getElementById('claimant-info-display');
  if (claimantInfo && currentUser) {
    claimantInfo.value = `${currentUser.name || currentUser.fullName || 'Student'} - ${currentUser.email || ''}`;
  }

  // Render Page Content
  await renderItemView(item);

  async function renderItemView(item) {
    const isOwner = !!(currentUser && (
      (currentUser.id && item.userId && Number(currentUser.id) === Number(item.userId)) ||
      (currentUser.userId && item.userId && Number(currentUser.userId) === Number(item.userId)) ||
      (currentUser.id && item.user && item.user.id && Number(currentUser.id) === Number(item.user.id)) ||
      (currentUser.userId && item.user && item.user.id && Number(currentUser.userId) === Number(item.user.id)) ||
      (currentUser.email && item.contactEmail && currentUser.email.toLowerCase() === item.contactEmail.toLowerCase()) ||
      (currentUser.email && item.user && item.user.email && currentUser.email.toLowerCase() === item.user.email.toLowerCase())
    ));
    const isAdmin = !!(currentUser && currentUser.role === 'ADMIN');
    const canClaim = item.type === 'FOUND' && item.status !== 'RESOLVED' && !isOwner;
    const canResolve = (isOwner || isAdmin) && item.status !== 'RESOLVED';

    const phone = item.contactPhone || (item.user && item.user.phone) || '';
    const email = item.contactEmail || (item.user && item.user.email) || '';
    const cleanPhone = phone.replace(/[^0-9]/g, '');
    const hasPhoto = item.imageUrl && item.imageUrl.trim() !== '';

    container.innerHTML = `
      <!-- Left Column: Item Showcase & Metadata -->
      <div class="item-showcase">
        <div class="item-showcase-media">
          ${hasPhoto
            ? `<img src="${item.imageUrl}" alt="${UI.escapeHTML(item.title)}" onerror="this.style.display='none'">`
            : `<div style="display:flex;align-items:center;justify-content:center;height:100%;color:var(--text-muted);font-size:14px;flex-direction:column;gap:8px;">
                <svg xmlns="http://www.w3.org/2000/svg" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><rect x="3" y="3" width="18" height="18" rx="2"></rect><circle cx="8.5" cy="8.5" r="1.5"></circle><polyline points="21 15 16 10 5 21"></polyline></svg>
                <span>No photo uploaded</span>
              </div>`
          }
          <div class="item-card-badges">
            ${UI.renderTypeBadge(item.type)}
            ${UI.renderStatusBadge(item.status)}
          </div>
          <div class="item-card-date-badge">${UI.formatDate(item.date)}</div>
        </div>

        <div class="item-showcase-body">
          <h1 style="font-size: var(--font-size-2xl); margin-bottom: var(--space-3);">${UI.escapeHTML(item.title)}</h1>

          <!-- Meta Spec Grid -->
          <div class="item-meta-spec-grid">
            <div class="spec-item">
              <span class="spec-label">Date</span>
              <span class="spec-val">${UI.formatDate(item.date)}</span>
            </div>
            <div class="spec-item">
              <span class="spec-label">Where</span>
              <span class="spec-val">${UI.escapeHTML(item.location || item.specificSpot || 'Campus')}</span>
            </div>
            <div class="spec-item">
              <span class="spec-label">Reported By</span>
              <span class="spec-val">${UI.escapeHTML(item.user ? item.user.name : 'Student')}</span>
            </div>
            <div class="spec-item">
              <span class="spec-label">Reference ID</span>
              <span class="spec-val">#LF-${item.id}</span>
            </div>
          </div>

          <!-- Description Section -->
          ${item.description ? `
          <div style="margin-bottom: var(--space-6);">
            <h3 style="font-size: var(--font-size-md); margin-bottom: var(--space-2);">Description</h3>
            <p style="font-size: var(--font-size-base); line-height: 1.7; color: var(--text-secondary); white-space: pre-line;">
              ${UI.escapeHTML(item.description)}
            </p>
          </div>
          ` : ''}

          <!-- Direct Contact Section -->
          <div style="background: var(--bg-secondary); border-radius: var(--border-radius-md); padding: var(--space-5); margin-bottom: var(--space-6);">
            <div style="font-weight: 700; font-size: var(--font-size-md); margin-bottom: var(--space-3); display: flex; align-items: center; gap: 8px;">
              <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72c.127.96.361 1.903.7 2.81a2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45c.907.339 1.85.573 2.81.7A2 2 0 0 1 22 16.92z"></path></svg>
              Contact Directly
            </div>
            
            <div style="display: flex; flex-direction: column; gap: 10px;">
              ${phone ? `
              <div style="display: flex; gap: 10px;">
                <a href="tel:${phone}" class="contact-action-btn btn-call" style="flex: 1; min-height: 44px; font-size: 14px;">
                  <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72c.127.96.361 1.903.7 2.81a2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45c.907.339 1.85.573 2.81.7A2 2 0 0 1 22 16.92z"></path></svg>
                  <span>Call: ${UI.escapeHTML(phone)}</span>
                </a>
                <a href="https://wa.me/91${phone.replace(/[^0-9]/g, '')}?text=Hi,%20I%20am%20contacting%20you%20regarding%20the%20${encodeURIComponent(item.title)}%20listed%20on%20REC%20CampusFind" target="_blank" class="contact-action-btn btn-whatsapp" style="flex: 1; min-height: 44px; font-size: 14px;">
                  <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="currentColor"><path d="M.057 24l1.687-6.163c-1.041-1.804-1.588-3.849-1.587-5.946.003-6.556 5.338-11.891 11.893-11.891 3.181.001 6.167 1.24 8.413 3.488 2.245 2.248 3.481 5.236 3.48 8.414-.003 6.557-5.338 11.892-11.893 11.892-1.99-.001-3.951-.5-5.688-1.448l-6.305 1.654zm6.597-3.807c1.676.995 3.276 1.591 5.392 1.592 5.448 0 9.886-4.434 9.889-9.885.002-5.462-4.415-9.89-9.881-9.892-5.452 0-9.887 4.434-9.889 9.884-.001 2.225.651 3.891 1.746 5.634l-.999 3.648 3.742-.981zm11.387-5.464c-.074-.124-.272-.198-.57-.347-.297-.149-1.758-.868-2.031-.967-.272-.099-.47-.149-.669.149-.198.297-.768.967-.941 1.165-.173.198-.347.223-.644.074-.297-.149-1.255-.462-2.39-1.475-.883-.788-1.48-1.761-1.653-2.059-.173-.297-.018-.458.13-.606.134-.133.297-.347.446-.521.151-.172.2-.296.3-.495.099-.198.05-.372-.025-.521-.075-.148-.669-1.611-.916-2.206-.242-.579-.487-.501-.669-.51l-.57-.01c-.198 0-.52.074-.792.372s-1.04 1.016-1.04 2.479 1.065 2.876 1.213 3.074c.149.198 2.095 3.2 5.076 4.487.709.306 1.263.489 1.694.626.712.226 1.36.194 1.872.118.571-.085 1.758-.719 2.006-1.413.248-.695.248-1.29.173-1.414z"/></svg>
                  <span>Chat on WhatsApp</span>
                </a>
              </div>
              ` : ''}
              ${email ? `
              <a href="mailto:${email}" style="display: flex; align-items: center; gap: 10px; padding: 12px 16px; background: #fff; color: var(--text-primary); border: 1px solid var(--border-color); border-radius: 8px; text-decoration: none; font-weight: 600; font-size: 14px; transition: background 0.2s ease; min-height: 44px;">
                <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"></path><polyline points="22,6 12,13 2,6"></polyline></svg>
                Email: ${UI.escapeHTML(email)}
              </a>
              ` : ''}
              ${!phone && !email ? `
              <div style="font-size: 13px; color: var(--text-muted); padding: 12px;">
                No direct contact provided. Submit a claim below to connect with the reporter.
              </div>
              ` : ''}
            </div>
          </div>

          <!-- Action Buttons -->
          <div style="display: flex; gap: var(--space-3); flex-wrap: wrap; align-items: center;">
            ${canClaim ? `
              <button class="btn btn-primary btn-lg" id="btn-open-claim" style="flex: 1;">
                Claim This Item
              </button>
            ` : ''}

            ${canResolve ? `
              <button class="btn btn-success btn-lg" id="btn-resolve-item">
                Mark as Reunited / Resolved
              </button>
            ` : ''}

            ${(isOwner || isAdmin) ? `
              <button class="btn btn-danger btn-sm" id="btn-delete-item" style="margin-left: auto;">
                Delete Listing
              </button>
            ` : ''}
          </div>
        </div>
      </div>

      <!-- Right Column: How Recovery Works -->
      <div>
        <!-- Campus Handover Guide Card -->
        <div class="card" style="padding: var(--space-6);">
          <h3 style="font-size: var(--font-size-md); margin-bottom: var(--space-4);">
            How Recovery Works
          </h3>

          <div style="display: flex; flex-direction: column; gap: var(--space-4);">
            <div style="display: flex; gap: var(--space-3);">
              <div style="width: 24px; height: 24px; border-radius: 50%; background-color: var(--accent-light); color: var(--accent-primary); font-weight: 700; font-size: 12px; display: flex; align-items: center; justify-content: center; flex-shrink: 0;">1</div>
              <div>
                <div style="font-size: var(--font-size-sm); font-weight: 600;">Contact Directly</div>
                <div class="text-muted" style="font-size: var(--font-size-xs);">Call or email the reporter using the contact info on this page.</div>
              </div>
            </div>

            <div style="display: flex; gap: var(--space-3);">
              <div style="width: 24px; height: 24px; border-radius: 50%; background-color: var(--accent-light); color: var(--accent-primary); font-weight: 700; font-size: 12px; display: flex; align-items: center; justify-content: center; flex-shrink: 0;">2</div>
              <div>
                <div style="font-size: var(--font-size-sm); font-weight: 600;">Verify Ownership</div>
                <div class="text-muted" style="font-size: var(--font-size-xs);">Describe unique marks or serial numbers known only to the real owner.</div>
              </div>
            </div>

            <div style="display: flex; gap: var(--space-3);">
              <div style="width: 24px; height: 24px; border-radius: 50%; background-color: var(--accent-light); color: var(--accent-primary); font-weight: 700; font-size: 12px; display: flex; align-items: center; justify-content: center; flex-shrink: 0;">3</div>
              <div>
                <div style="font-size: var(--font-size-sm); font-weight: 600;">Meet & Collect</div>
                <div class="text-muted" style="font-size: var(--font-size-xs);">Arrange a safe pickup on campus. Bring your REC ID card for verification.</div>
              </div>
            </div>
          </div>
        </div>

      </div>
    `;

    // Hook events
    const openClaimBtn = document.getElementById('btn-open-claim');
    if (openClaimBtn) {
      openClaimBtn.addEventListener('click', () => {
        UI.openModal('claim-modal');
      });
    }

    const resolveBtn = document.getElementById('btn-resolve-item');
    if (resolveBtn) {
      resolveBtn.addEventListener('click', async () => {
        if (confirm('Mark this listing as Reunited & Resolved?')) {
          await API.updateItemStatus(item.id, 'RESOLVED');
          UI.showToast('Resolved', 'Item status marked as Reunited/Resolved.', 'success');
          setTimeout(() => window.location.reload(), 500);
        }
      });
    }

    const deleteBtn = document.getElementById('btn-delete-item');
    if (deleteBtn) {
      deleteBtn.addEventListener('click', () => {
        UI.confirmDialog({
          title: 'Delete Listing?',
          message: 'This item will be permanently removed from the campus feed if you click delete.',
          confirmText: 'Yes, Delete',
          cancelText: 'Cancel',
          isDestructive: true,
          onConfirm: async () => {
            try {
              await API.deleteItem(item.id);
              UI.showToast('Listing Removed', 'The item was deleted.', 'info');
              setTimeout(() => window.location.href = 'browse.html', 500);
            } catch (err) {
              UI.showToast('Delete Failed', err.message || 'Unable to delete listing.', 'error');
            }
          }
        });
      });
    }
  }

  // Claim Form Submit
  const claimForm = document.getElementById('claim-form');
  if (claimForm) {
    claimForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      const proof = document.getElementById('proof-description').value.trim();

      if (!proof || proof.length < 10) {
        UI.showToast('Incomplete Proof', 'Please provide descriptive ownership details (at least 10 characters).', 'error');
        return;
      }

      const submitBtn = document.getElementById('submit-claim-btn');
      submitBtn.disabled = true;
      submitBtn.textContent = 'Submitting...';

      try {
        await API.submitClaim(itemId, proof);
        UI.closeModal('claim-modal');
        UI.showToast('Claim Submitted', 'Your claim has been received. The reporter will review it.', 'success');
        setTimeout(() => window.location.reload(), 800);
      } catch (err) {
        UI.showToast('Error', err.message || 'Failed to submit claim.', 'error');
        submitBtn.disabled = false;
        submitBtn.textContent = 'Submit Claim for Review';
      }
    });
  }

  function showNotFound(msg) {
    container.innerHTML = `
      <div class="empty-state" style="grid-column: 1 / -1;">
        <h3 class="empty-state-title">Listing Not Found</h3>
        <p class="empty-state-desc">${msg}</p>
        <a href="browse.html" class="btn btn-primary">Return to Feed</a>
      </div>
    `;
  }
});
