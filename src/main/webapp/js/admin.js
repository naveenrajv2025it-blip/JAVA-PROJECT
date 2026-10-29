/**
 * Campus Lost & Found System - Admin Console Script
 * Claim verification queue review, approve/reject workflow & inventory moderation
 */

let currentReviewClaimId = null;

document.addEventListener('DOMContentLoaded', async () => {
  const user = await API.checkSession();
  if (!user || (user.role !== 'ADMIN' && user.role !== 'STAFF')) {
    UI.showToast('Restricted Access', 'Security Desk console requires administrative login.', 'error');
    setTimeout(() => {
      window.location.href = 'login.html?redirect=admin.html';
    }, 800);
    return;
  }

  const claimsFilter = document.getElementById('claims-filter-status');

  // Load Metrics
  async function refreshMetrics() {
    const metrics = await API.getMetrics();
    document.getElementById('admin-pending-claims').textContent = metrics.pendingClaims;
    document.getElementById('admin-total-items').textContent = metrics.totalReported;
    document.getElementById('admin-resolved-items').textContent = metrics.totalResolved;
    document.getElementById('admin-recovery-rate').textContent = `${metrics.recoveryRate}%`;
  }

  // Load Claims Queue
  async function loadClaims() {
    const filterStatus = claimsFilter.value;
    const claims = await API.getClaims({ status: filterStatus });
    const tbody = document.getElementById('claims-queue-tbody');

    if (claims.length === 0) {
      tbody.innerHTML = `
        <tr>
          <td colspan="7" style="text-align: center; padding: 30px;" class="text-muted">
            No claims found in this category.
          </td>
        </tr>
      `;
      return;
    }

    tbody.innerHTML = claims.map(c => `
      <tr>
        <td><strong>#CLM-${c.id}</strong></td>
        <td>
          <a href="item-details.html?id=${c.itemId}" style="font-weight: 700; color: var(--accent-primary);">
            ${UI.escapeHTML(c.itemTitle)}
          </a>
        </td>
        <td>
          <div style="font-weight: 600;">${UI.escapeHTML(c.claimant ? c.claimant.name : 'Unknown')}</div>
          <div class="text-muted" style="font-size: 11px;">Roll: ${UI.escapeHTML(c.claimant ? c.claimant.roll : '')} &bull; ${UI.escapeHTML(c.claimant ? c.claimant.email : '')}</div>
        </td>
        <td>
          <div style="max-width: 280px; font-size: 12px; color: var(--text-secondary); text-overflow: ellipsis; overflow: hidden; white-space: nowrap;">
            "${UI.escapeHTML(c.proofDescription)}"
          </div>
        </td>
        <td>${UI.formatDate(c.submittedAt)}</td>
        <td>${UI.renderStatusBadge(c.status)}</td>
        <td style="text-align: right;">
          <button class="btn btn-primary btn-sm" onclick="openClaimReview(${c.id})">
            Inspect & Verify
          </button>
        </td>
      </tr>
    `).join('');
  }

  // Load Inventory Moderation
  async function loadInventory() {
    const items = await API.getItems();
    const tbody = document.getElementById('admin-inventory-tbody');

    if (items.length === 0) {
      tbody.innerHTML = `
        <tr>
          <td colspan="8" style="text-align: center; padding: 30px;" class="text-muted">
            No items registered.
          </td>
        </tr>
      `;
      return;
    }

    tbody.innerHTML = items.map(item => `
      <tr>
        <td>${UI.renderTypeBadge(item.type)}</td>
        <td>
          <a href="item-details.html?id=${item.id}" style="font-weight: 700; color: var(--text-primary);">
            ${UI.escapeHTML(item.title)}
          </a>
        </td>
        <td><span class="badge badge-neutral">${UI.escapeHTML(item.category)}</span></td>
        <td>${UI.escapeHTML(item.location)}</td>
        <td>${UI.escapeHTML(item.user ? item.user.name : 'Unknown')} (${UI.escapeHTML(item.user ? item.user.role : '')})</td>
        <td>${UI.formatDate(item.date)}</td>
        <td>${UI.renderStatusBadge(item.status)}</td>
        <td style="text-align: right;">
          <div style="display: inline-flex; gap: 6px;">
            <a href="item-details.html?id=${item.id}" class="btn btn-secondary btn-sm" title="View">View</a>
            ${item.status !== 'RESOLVED' ? `
              <button class="btn btn-success btn-sm" onclick="adminMarkResolved(${item.id})">Resolve</button>
            ` : `
              <button class="btn btn-secondary btn-sm" onclick="adminMarkOpen(${item.id})">Reopen</button>
            `}
            <button class="btn btn-danger btn-sm" onclick="adminDeleteItem(${item.id})">&times;</button>
          </div>
        </td>
      </tr>
    `).join('');
  }

  // Open Claim Review Modal
  window.openClaimReview = async function(claimId) {
    const claims = await API.getClaims();
    const claim = claims.find(c => c.id === claimId);
    if (!claim) return;

    currentReviewClaimId = claimId;
    document.getElementById('modal-item-title').textContent = `${claim.itemTitle} (#LF-${claim.itemId})`;
    document.getElementById('modal-claimant-info').textContent = `${claim.claimant.name} | Roll No: ${claim.claimant.roll} | ${claim.claimant.email}`;
    document.getElementById('modal-proof-desc').textContent = claim.proofDescription;
    document.getElementById('review-admin-remarks').value = claim.adminRemarks || '';

    UI.openModal('review-modal');
  };

  // Approve Claim Handler
  document.getElementById('btn-approve-claim').addEventListener('click', async () => {
    if (!currentReviewClaimId) return;
    const remarks = document.getElementById('review-admin-remarks').value.trim() || 'Verified ownership proof. Item approved for collection at Security Desk.';
    
    await API.reviewClaim(currentReviewClaimId, 'APPROVED', remarks);
    UI.closeModal('review-modal');
    UI.showToast('Claim Approved', 'Item status marked as Reunited/Resolved.', 'success');
    
    await refreshMetrics();
    await loadClaims();
    await loadInventory();
  });

  // Reject Claim Handler
  document.getElementById('btn-reject-claim').addEventListener('click', async () => {
    if (!currentReviewClaimId) return;
    const remarks = document.getElementById('review-admin-remarks').value.trim() || 'Ownership verification failed: Details provided do not match the item specifications.';
    
    await API.reviewClaim(currentReviewClaimId, 'REJECTED', remarks);
    UI.closeModal('review-modal');
    UI.showToast('Claim Rejected', 'Claim rejected and recorded in system.', 'info');
    
    await refreshMetrics();
    await loadClaims();
    await loadInventory();
  });

  // Action helpers
  window.adminMarkResolved = async function(id) {
    await API.updateItemStatus(id, 'RESOLVED');
    UI.showToast('Status Updated', 'Item marked as Reunited.', 'success');
    refreshMetrics();
    loadInventory();
  };

  window.adminMarkOpen = async function(id) {
    await API.updateItemStatus(id, 'OPEN');
    UI.showToast('Status Updated', 'Item reopened.', 'info');
    refreshMetrics();
    loadInventory();
  };

  window.adminDeleteItem = async function(id) {
    if (confirm('Permanently delete this campus listing?')) {
      await API.deleteItem(id);
      UI.showToast('Deleted', 'Listing removed by administrator.', 'info');
      refreshMetrics();
      loadInventory();
    }
  };

  claimsFilter.addEventListener('change', loadClaims);

  // Initial runs
  await refreshMetrics();
  await loadClaims();
  await loadInventory();
});
