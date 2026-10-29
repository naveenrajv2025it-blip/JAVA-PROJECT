/**
 * REC CampusFind - Client API & Servlet Integration Layer
 * Connects frontend views directly to Java Servlets with real session auth
 */

const API = (function() {
  const CONTEXT_PATH = (function() {
    const path = window.location.pathname;
    const match = path.match(/^(\/[^\/]+)/);
    if (match && !match[1].endsWith('.html') && match[1] !== '/api') {
      return match[1];
    }
    return '';
  })();

  // Purge any stale mock/fake session stored in legacy localStorage
  try {
    localStorage.removeItem('campus_lf_user');
    localStorage.removeItem('campus_lf_role');
  } catch (e) {}

  let currentUser = null;
  let sessionCheckPromise = null;

  // Reusable fetch wrapper with JSON parsing and clean error handling
  async function fetchJSON(url, options = {}) {
    try {
      const resp = await fetch(url, {
        headers: {
          'Content-Type': 'application/json',
          ...(options.headers || {})
        },
        credentials: 'same-origin',
        ...options
      });

      if (!resp.ok) {
        let errorMsg = `Server error (${resp.status})`;
        try {
          const errData = await resp.json();
          if (errData && errData.error) errorMsg = errData.error;
        } catch (e) {}
        const err = new Error(errorMsg);
        err.status = resp.status;
        throw err;
      }

      return await resp.json();
    } catch (err) {
      throw err;
    }
  }

  return {
    getContextPath: () => CONTEXT_PATH,

    // Real Server-backed session verification
    checkSession: async (force = false) => {
      if (currentUser && !force) return currentUser;
      if (sessionCheckPromise && !force) return sessionCheckPromise;

      sessionCheckPromise = (async () => {
        try {
          const user = await fetchJSON(`${CONTEXT_PATH}/api/auth`);
          if (user && (user.email || user.userId)) {
            currentUser = {
              ...user,
              name: user.name || user.fullName || 'Student',
              fullName: user.fullName || user.name || 'Student',
              roll: user.roll || user.rollNumber || '',
              rollNumber: user.rollNumber || user.roll || '',
              avatar: (user.fullName || user.name || 'REC').split(' ').map(n => n[0]).join('').substring(0, 2).toUpperCase()
            };
            return currentUser;
          }
        } catch (e) {
          // 401: Normal unauthenticated state for public visitors
        } finally {
          sessionCheckPromise = null;
        }
        currentUser = null;
        return null;
      })();

      return sessionCheckPromise;
    },

    getCurrentUser: () => currentUser,

    isAuthenticated: () => !!(currentUser && (currentUser.email || currentUser.userId)),

    setCurrentUser: (user) => {
      if (!user) {
        currentUser = null;
        return;
      }
      currentUser = {
        ...user,
        name: user.name || user.fullName || 'Student',
        fullName: user.fullName || user.name || 'Student',
        roll: user.roll || user.rollNumber || '',
        rollNumber: user.rollNumber || user.roll || '',
        avatar: (user.fullName || user.name || 'REC').split(' ').map(n => n[0]).join('').substring(0, 2).toUpperCase()
      };
    },

    // 1. Fetch Item Listings with Query/Type Filters
    getItems: async (filters = {}) => {
      const params = new URLSearchParams();
      if (filters.type && filters.type !== 'ALL') params.append('type', filters.type);
      if (filters.status && filters.status !== 'ALL') params.append('status', filters.status);
      if (filters.query) params.append('query', filters.query);
      if (filters.sortBy) params.append('sortBy', filters.sortBy);

      try {
        const data = await fetchJSON(`${CONTEXT_PATH}/api/items?${params.toString()}`);
        return (data || []).map(item => ({
          id: item.itemId,
          itemId: item.itemId,
          type: (item.itemType || 'LOST').toUpperCase(),
          itemType: (item.itemType || 'LOST').toUpperCase(),
          title: item.title || 'Untitled Item',
          category: item.categoryName || 'General',
          categoryId: item.categoryId || 1,
          location: item.specificSpot || item.buildingName || 'Campus Spot',
          locationId: item.locationId || 1,
          specificSpot: item.specificSpot || '',
          date: item.incidentDate,
          incidentDate: item.incidentDate,
          status: item.status || 'OPEN',
          description: item.description || '',
          imageUrl: item.imageUrl || '',
          contactPhone: item.contactPhone || item.reporterPhone || (item.user && item.user.phone) || '',
          contactEmail: item.contactEmail || item.reporterEmail || (item.user && item.user.email) || '',
          contactPreference: item.contactPreference || 'PHONE',
          user: {
            id: item.userId,
            name: item.reporterName || 'Student',
            roll: item.reporterRoll || '',
            email: item.reporterEmail || '',
            phone: item.reporterPhone || '',
            role: item.reporterRole || 'STUDENT'
          },
          createdAt: item.createdAt
        }));
      } catch (e) {
        console.warn('API.getItems request failed:', e.message);
        return [];
      }
    },

    // 2. Fetch Single Item Details
    getItemById: async (id) => {
      try {
        const item = await fetchJSON(`${CONTEXT_PATH}/api/items?id=${id}`);
        return {
          id: item.itemId,
          itemId: item.itemId,
          type: (item.itemType || 'LOST').toUpperCase(),
          itemType: (item.itemType || 'LOST').toUpperCase(),
          title: item.title,
          category: item.categoryName || 'General',
          categoryId: item.categoryId || 1,
          location: item.specificSpot || item.buildingName || 'Campus Spot',
          locationId: item.locationId || 1,
          specificSpot: item.specificSpot || '',
          date: item.incidentDate,
          incidentDate: item.incidentDate,
          status: item.status || 'OPEN',
          description: item.description || '',
          imageUrl: item.imageUrl || '',
          contactPhone: item.contactPhone || item.reporterPhone || (item.user && item.user.phone) || '',
          contactEmail: item.contactEmail || item.reporterEmail || (item.user && item.user.email) || '',
          contactPreference: item.contactPreference || 'PHONE',
          user: {
            id: item.userId,
            name: item.reporterName || 'Student',
            roll: item.reporterRoll || '',
            email: item.reporterEmail || '',
            phone: item.reporterPhone || '',
            role: item.reporterRole || 'STUDENT'
          },
          createdAt: item.createdAt
        };
      } catch (e) {
        return null;
      }
    },

    // 3. Create Item Report
    createItem: async (itemData) => {
      const payload = {
        itemType: (itemData.type || itemData.itemType || 'LOST').toUpperCase(),
        title: itemData.title,
        categoryId: parseInt(itemData.categoryId, 10) || 1,
        locationId: parseInt(itemData.locationId, 10) || 1,
        specificSpot: itemData.specificSpot || itemData.location || '',
        incidentDate: itemData.date || itemData.incidentDate,
        description: itemData.description || `Missed/found at: ${itemData.specificSpot || 'Campus'}. Contact: ${itemData.contactPhone || ''}`,
        imageUrl: itemData.imageUrl || '',
        contactPhone: itemData.contactPhone || '',
        contactEmail: itemData.contactEmail || '',
        contactPreference: 'PHONE'
      };

      const res = await fetchJSON(`${CONTEXT_PATH}/api/items`, {
        method: 'POST',
        body: JSON.stringify(payload)
      });

      return {
        id: res.itemId,
        ...payload,
        ...res
      };
    },

    // 4. Update Status (e.g. mark as Resolved)
    updateItemStatus: async (itemId, newStatus) => {
      return await fetchJSON(`${CONTEXT_PATH}/api/items`, {
        method: 'PUT',
        body: JSON.stringify({ itemId: parseInt(itemId, 10), status: newStatus })
      });
    },

    // 5. Delete Item
    deleteItem: async (itemId) => {
      return await fetchJSON(`${CONTEXT_PATH}/api/items?id=${itemId}`, { method: 'DELETE' });
    },

    // 6. Metrics
    getMetrics: async () => {
      try {
        return await fetchJSON(`${CONTEXT_PATH}/api/admin/metrics`);
      } catch (e) {
        const items = await API.getItems();
        const total = items.length;
        const resolved = items.filter(it => it.status === 'RESOLVED').length;
        return {
          totalReported: total,
          totalResolved: resolved,
          activeLost: items.filter(it => it.type === 'LOST' && it.status === 'OPEN').length,
          activeFound: items.filter(it => it.type === 'FOUND' && it.status === 'OPEN').length,
          recoveryRate: total > 0 ? Math.round((resolved / total) * 100) : 0
        };
      }
    },

    // 7. Authentication (Supports passwordless student email login)
    login: async (email, password = '') => {
      const payload = { email: (email || '').trim().toLowerCase() };
      if (password) payload.password = password;
      const res = await fetchJSON(`${CONTEXT_PATH}/api/auth/login`, {
        method: 'POST',
        body: JSON.stringify(payload)
      });
      if (res && res.user) {
        API.setCurrentUser(res.user);
        return res.user;
      }
      return null;
    },

    register: async (userData) => {
      const res = await fetchJSON(`${CONTEXT_PATH}/api/auth/register`, {
        method: 'POST',
        body: JSON.stringify(userData)
      });
      if (res && res.user) {
        API.setCurrentUser(res.user);
        return res.user;
      }
      return null;
    },

    logout: async () => {
      try {
        await fetchJSON(`${CONTEXT_PATH}/api/auth/logout`, { method: 'POST' });
      } catch (e) {}
      currentUser = null;
      try {
        localStorage.removeItem('campus_lf_user');
      } catch (e) {}
    }
  };
})();

window.API = API;
