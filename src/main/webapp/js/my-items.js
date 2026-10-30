/**
 * Campus Lost & Found System - User Dashboard Script
 * Personal reports management & claim status progression tracker
 */

let activeTab = 'reports';

function switchDashboardTab(tab) {
  activeTab = tab;
  const reportsBtn = document.getElementById('tab-reports-btn');
  const claimsBtn = document.getElementById('tab-claims-btn');
  const reportsPane = document.getElementById('tab-reports-pane');
  const claimsPane = document.getElementById('tab-claims-pane');

  if (tab === 'reports') {
    reportsBtn.classList.add('active');
    claimsBtn.classList.remove('active');
    reportsPane.classList.add('active');
    claimsPane.classList.remove('active');
  } else {
    claimsBtn.classList.add('active');
    reportsBtn.classList.remove('active');
    claimsPane.classList.add('active');
    reportsPane.classList.remove('active');
  }
}

document.addEventListener('DOMContentLoaded', async () => {
  const currentUser = await API.checkSession();
  if (!currentUser) {
    window.location.href = 'login.html?redirect=my-items.html';
    return;
  }

  // Greeting
  const greetingEl = document.getElementById('user-greeting');
  if (greetingEl) {
    greetingEl.textContent = `${currentUser.fullName || currentUser.name || 'Student'}'s Dashboard`;
  }

  // Load User Reports
  const allItems = await API.getItems();
  const userItems = allItems.filter(it => {
    return (
      (it.userId && currentUser.id && Number(it.userId) === Number(currentUser.id)) ||
      (it.userId && currentUser.userId && Number(it.userId) === Number(currentUser.userId)) ||
      (it.user && (it.user.id === currentUser.id || it.user.id === currentUser.userId)) ||
      (currentUser.email && it.contactEmail && currentUser.email.toLowerCase() === it.contactEmail.toLowerCase())
    );
  });

  // Load User Claims
  const userClaims = await API.getClaims({ claimantId: currentUser.id });

  // Update Summary Counts
  const resolvedCount = userItems.filter(it => it.status === 'RESOLVED').length;
  const pendingClaimsCount = userClaims.filter(c => c.status === 'PENDING').length;

  document.getElementById('user-stat-reports').textContent = userItems.length;
  document.getElementById('user-stat-resolved').textContent = resolvedCount;
  document.getElementById('user-stat-claims').textContent = userClaims.length;
  document.getElementById('user-stat-pending').textContent = pendingClaimsCount;

  document.getElementById('tab-reports-count').textContent = userItems.length;
  document.getElementById('tab-claims-count').textContent = userClaims.length;

  // Render Reports Table
  renderReportsTable(userItems);

  // Render Claims List
  renderClaimsList(userClaims);

  function renderReportsTable(items) {
    const tbody = document.getElementById('my-reports-tbody');

    if (items.length === 0) {
      tbody.innerHTML = `
        <tr>
          <td colspan="6" style="text-align: center; padding: 40px;">
            <div class="empty-state" style="border: none; padding: 20px 0;">
              <h3 class="empty-state-title">No reports submitted yet</h3>
              <p class="empty-state-desc">You haven't reported any lost or found belongings yet.</p>
              <a href="report.html" class="btn btn-primary btn-sm">+ Create Your First Report</a>
            </div>
          </td>
        </tr>
      `;
      return;
    }

    tbody.innerHTML = items.map(item => `
      <tr>
        <td>${UI.renderTypeBadge(item.type)}</td>
        <td>
          <div style="display: flex; align-items: center; gap: var(--space-3);">
            <img src="${item.imageUrl || ''}" alt="${UI.escapeHTML(item.title)}" style="width: 44px; height: 44px; border-radius: var(--border-radius-sm); object-fit: cover; background: #f1f5f9;" onerror="this.style.display='none'">
            <div>
              <a href="item-details.html?id=${item.id}" style="font-weight: 700; color: var(--text-primary);">
                ${UI.escapeHTML(item.title)}
              </a>
              <div class="text-muted" style="font-size: 11px;">#LF-${item.id}</div>
            </div>
          </div>
        </td>
        <td>
          <div style="font-weight: 500;">${UI.escapeHTML(item.location || item.specificSpot || 'Campus')}</div>
        </td>
        <td>${UI.formatDate(item.date)}</td>
        <td>${UI.renderStatusBadge(item.status)}</td>
        <td style="text-align: right;">
          <div style="display: inline-flex; gap: var(--space-2); justify-content: flex-end;">
            <a href="item-details.html?id=${item.id}" class="btn btn-secondary btn-sm" title="View Details">
              View
            </a>
            ${item.status !== 'RESOLVED' ? `
              <button class="btn btn-success btn-sm" onclick="markItemResolved(${item.id})">
                Reunited
              </button>
            ` : ''}
            <button class="btn btn-danger btn-sm" onclick="deleteUserItem(${item.id})">
              &times;
            </button>
          </div>
        </td>
      </tr>
    `).join('');
  }

  function renderClaimsList(claims) {
    const list = document.getElementById('my-claims-list');

    if (claims.length === 0) {
      list.innerHTML = `
        <div class="empty-state">
          <div class="empty-state-title">No claims filed</div>
          <p class="empty-state-desc">When you spot your missing item in the catalog, submit an ownership claim to begin the return verification.</p>
          <a href="browse.html?type=FOUND" class="btn btn-primary btn-sm">Browse Found Items</a>
        </div>
      `;
      return;
    }

    list.innerHTML = claims.map(claim => {
      const isApproved = claim.status === 'APPROVED';
      const isPending = claim.status === 'PENDING';
      const isRejected = claim.status === 'REJECTED';

      return `
        <div class="card" style="padding: var(--space-6);">
          <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: var(--space-4); flex-wrap: wrap; gap: var(--space-3);">
            <div>
              <div style="font-size: var(--font-size-xs); color: var(--text-muted); text-transform: uppercase; font-weight: 700; margin-bottom: 2px;">
                Ownership Claim #CLM-${claim.id}
              </div>
              <h3 style="font-size: var(--font-size-md); font-weight: 700; color: var(--text-primary);">
                Item: <a href="item-details.html?id=${claim.itemId}">${UI.escapeHTML(claim.itemTitle)}</a>
              </h3>
            </div>
            <div>
              ${UI.renderStatusBadge(claim.status)}
            </div>
          </div>

          <!-- Stepper Visual -->
          <div style="background-color: var(--bg-secondary); border-radius: var(--border-radius-md); padding: var(--space-4) var(--space-6); margin-bottom: var(--space-4);">
            <div class="claim-stepper">
              <div class="stepper-step completed">
                <span style="font-weight: 700;">&check;</span> Claim Lodged
              </div>
              <div class="stepper-line ${isPending || isApproved ? 'completed' : ''}"></div>
              <div class="stepper-step ${isApproved ? 'completed' : (isPending ? 'active' : '')}">
                <span style="font-weight: 700;">${isApproved ? '&check;' : '2.'}</span> Admin Review
              </div>
              <div class="stepper-line ${isApproved ? 'completed' : ''}"></div>
              <div class="stepper-step ${isApproved ? 'completed' : ''}">
                <span style="font-weight: 700;">${isApproved ? '&check;' : '3.'}</span> ${isApproved ? 'Ready for Pickup' : 'Release'}
              </div>
            </div>
          </div>

          <!-- Submitted Proof -->
          <div style="margin-bottom: var(--space-3);">
            <div style="font-size: var(--font-size-xs); font-weight: 700; color: var(--text-muted); text-transform: uppercase; margin-bottom: 2px;">
              Your Submitted Ownership Proof
            </div>
            <p style="font-size: var(--font-size-sm); color: var(--text-secondary); background: #F8FAFC; border: 1px solid var(--border-color); padding: 8px 12px; border-radius: var(--border-radius-sm);">
              "${UI.escapeHTML(claim.proofDescription)}"
            </p>
          </div>

          <!-- Admin Remarks Notice -->
          ${claim.adminRemarks ? `
            <div style="background-color: ${isApproved ? '#ECFDF5' : (isRejected ? '#FEF2F2' : '#FFFBEB')}; border: 1px solid ${isApproved ? '#A7F3D0' : (isRejected ? '#FECDD3' : '#FDE68A')}; border-radius: var(--border-radius-sm); padding: var(--space-3) var(--space-4); font-size: var(--font-size-xs);">
              <strong>Campus Security Desk Remarks:</strong> ${UI.escapeHTML(claim.adminRemarks)}
            </div>
          ` : ''}

          <div style="margin-top: var(--space-4); display: flex; justify-content: space-between; align-items: center; font-size: var(--font-size-xs); color: var(--text-muted);">
            <div>Submitted on: ${UI.formatDate(claim.submittedAt)}</div>
            <a href="item-details.html?id=${claim.itemId}" style="font-weight: 600; color: var(--accent-primary);">
              Inspect Item Listing &rarr;
            </a>
          </div>
        </div>
      `;
    }).join('');
  }
});

// Window Action Helpers
window.markItemResolved = async function(itemId) {
  if (confirm('Mark this listing as Reunited & Resolved?')) {
    await API.updateItemStatus(itemId, 'RESOLVED');
    UI.showToast('Updated', 'Item marked as Reunited.', 'success');
    setTimeout(() => window.location.reload(), 500);
  }
};

window.deleteUserItem = async function(itemId) {
  if (confirm('Are you sure you want to delete this listing?')) {
    await API.deleteItem(itemId);
    UI.showToast('Deleted', 'Item was removed.', 'info');
    setTimeout(() => window.location.reload(), 500);
  }
};
