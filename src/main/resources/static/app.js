/**
 * StaffSync - Employee Management System
 * Interactive Client Logic with Role-Based Access Control (RBAC) & JWT
 */

// Configuration & State
const API_BASE = '';
let state = {
  currentUser: null,
  currentRole: null, // 'ROLE_ADMIN' | 'ROLE_USER'
  authToken: localStorage.getItem('auth_token') || null,
  employees: [],
  departments: new Set(),
  currentView: 'grid', // 'grid' | 'table'
  searchTerm: '',
  selectedDepartment: 'ALL'
};

// DOM Elements
const employeeGridContainer = document.getElementById('employeeGridContainer');
const employeeTableContainer = document.getElementById('employeeTableContainer');
const employeeTableBody = document.getElementById('employeeTableBody');
const statEmployeeCount = document.getElementById('statEmployeeCount');
const statDepartmentCount = document.getElementById('statDepartmentCount');
const statProjectCount = document.getElementById('statProjectCount');
const searchInput = document.getElementById('searchInput');
const departmentFilter = document.getElementById('departmentFilter');
const themeToggleBtn = document.getElementById('themeToggleBtn');
const themeIcon = document.getElementById('themeIcon');
const authNavContainer = document.getElementById('authNavContainer');

// Modal Elements
const addEmployeeModal = document.getElementById('addEmployeeModal');
const editEmployeeModal = document.getElementById('editEmployeeModal');
const uploadPhotoModal = document.getElementById('uploadPhotoModal');
const authModal = document.getElementById('authModal');

// ==========================================
// Initialization
// ==========================================
document.addEventListener('DOMContentLoaded', () => {
  initTheme();
  setupEventListeners();
  setupDropzones();
  checkAuthSession();
  loadStats();
  loadEmployees();
});

// Helper: Wrapper for fetch with JWT Authorization header
function apiFetch(url, options = {}) {
  options.headers = options.headers || {};
  if (state.authToken) {
    if (options.headers instanceof Headers) {
      options.headers.set('Authorization', `Bearer ${state.authToken}`);
    } else {
      options.headers['Authorization'] = `Bearer ${state.authToken}`;
    }
  }
  return fetch(url, options);
}

// Role Check Helper
function isAdmin() {
  return state.currentUser && (state.currentRole === 'ROLE_ADMIN' || state.currentRole === 'ADMIN');
}

// ==========================================
// Theme Management
// ==========================================
function initTheme() {
  const savedTheme = localStorage.getItem('theme') || 'dark';
  document.documentElement.setAttribute('data-theme', savedTheme);
  updateThemeIcon(savedTheme);
}

function updateThemeIcon(theme) {
  if (themeIcon) {
    themeIcon.className = theme === 'light' ? 'fa-solid fa-moon' : 'fa-solid fa-sun';
  }
}

if (themeToggleBtn) {
  themeToggleBtn.addEventListener('click', () => {
    const currentTheme = document.documentElement.getAttribute('data-theme') || 'dark';
    const newTheme = currentTheme === 'light' ? 'dark' : 'light';
    document.documentElement.setAttribute('data-theme', newTheme);
    localStorage.setItem('theme', newTheme);
    updateThemeIcon(newTheme);
  });
}

// ==========================================
// Event Listeners
// ==========================================
function setupEventListeners() {
  // View Toggle Buttons
  const viewGridBtn = document.getElementById('viewGridBtn');
  const viewTableBtn = document.getElementById('viewTableBtn');

  if (viewGridBtn && viewTableBtn) {
    viewGridBtn.addEventListener('click', () => {
      state.currentView = 'grid';
      viewGridBtn.classList.add('active');
      viewTableBtn.classList.remove('active');
      renderEmployees();
    });

    viewTableBtn.addEventListener('click', () => {
      state.currentView = 'table';
      viewTableBtn.classList.add('active');
      viewGridBtn.classList.remove('active');
      renderEmployees();
    });
  }

  // Search & Filter
  if (searchInput) {
    searchInput.addEventListener('input', (e) => {
      state.searchTerm = e.target.value.trim().toLowerCase();
      renderEmployees();
    });
  }

  if (departmentFilter) {
    departmentFilter.addEventListener('change', (e) => {
      state.selectedDepartment = e.target.value;
      renderEmployees();
    });
  }

  // Modal Open/Close Controls
  const openAddBtn = document.getElementById('openAddModalBtn');
  if (openAddBtn) {
    openAddBtn.addEventListener('click', () => {
      if (!isAdmin()) {
        showToast('Access Denied: Admin role required to add employees', 'error');
        return;
      }
      resetAddForm();
      openModal(addEmployeeModal);
    });
  }

  document.querySelectorAll('.close-modal-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      closeAllModals();
    });
  });

  // Close modal when clicking outside modal box
  document.querySelectorAll('.modal-overlay').forEach(modal => {
    modal.addEventListener('click', (e) => {
      if (e.target === modal) {
        closeAllModals();
      }
    });
  });

  // Close modal with Escape key
  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') {
      closeAllModals();
    }
  });

  // Image preview in Add Modal
  const addImageInput = document.getElementById('addEmployeeImage');
  const addPhotoPreview = document.getElementById('addPhotoPreview');
  const addDropzonePrompt = document.getElementById('addDropzonePrompt');
  if (addImageInput) {
    addImageInput.addEventListener('change', (e) => {
      const file = e.target.files && e.target.files[0];
      if (file) {
        const reader = new FileReader();
        reader.onload = (re) => {
          if (addPhotoPreview && addDropzonePrompt) {
            addPhotoPreview.src = re.target.result;
            addPhotoPreview.style.display = 'block';
            addDropzonePrompt.style.display = 'none';
          }
        };
        reader.readAsDataURL(file);
      }
    });
  }

  // Image preview in Photo Modal
  const modalPhotoFile = document.getElementById('modalPhotoFile');
  const photoModalPreview = document.getElementById('photoModalPreview');
  const photoDropzonePrompt = document.getElementById('photoDropzonePrompt');
  if (modalPhotoFile) {
    modalPhotoFile.addEventListener('change', (e) => {
      const file = e.target.files && e.target.files[0];
      if (file) {
        const reader = new FileReader();
        reader.onload = (re) => {
          if (photoModalPreview && photoDropzonePrompt) {
            photoModalPreview.src = re.target.result;
            photoModalPreview.style.display = 'block';
            photoDropzonePrompt.style.display = 'none';
          }
        };
        reader.readAsDataURL(file);
      }
    });
  }

  // Form Submissions
  const addForm = document.getElementById('addEmployeeForm');
  if (addForm) addForm.addEventListener('submit', handleAddEmployee);

  const editForm = document.getElementById('editEmployeeForm');
  if (editForm) editForm.addEventListener('submit', handleEditEmployee);

  const photoForm = document.getElementById('uploadPhotoForm');
  if (photoForm) photoForm.addEventListener('submit', handleUploadPhoto);

  // Auth Form Tabs
  const tabSignIn = document.getElementById('tabSignIn');
  const tabSignUp = document.getElementById('tabSignUp');
  const signInForm = document.getElementById('signInForm');
  const signUpForm = document.getElementById('signUpForm');

  if (tabSignIn && tabSignUp && signInForm && signUpForm) {
    tabSignIn.addEventListener('click', () => {
      tabSignIn.classList.add('active');
      tabSignUp.classList.remove('active');
      signInForm.style.display = 'block';
      signUpForm.style.display = 'none';
    });

    tabSignUp.addEventListener('click', () => {
      tabSignUp.classList.add('active');
      tabSignIn.classList.remove('active');
      signInForm.style.display = 'none';
      signUpForm.style.display = 'block';
    });

    signInForm.addEventListener('submit', handleSignIn);
    signUpForm.addEventListener('submit', handleSignUp);
  }

  // Quick Demo credentials buttons
  const quickDemoAdminBtn = document.getElementById('quickDemoAdminBtn');
  if (quickDemoAdminBtn) {
    quickDemoAdminBtn.addEventListener('click', () => {
      const u = document.getElementById('loginUsername');
      const p = document.getElementById('loginPassword');
      if (u) u.value = 'admin';
      if (p) p.value = '123';
    });
  }

  const quickDemoUserBtn = document.getElementById('quickDemoUserBtn');
  if (quickDemoUserBtn) {
    quickDemoUserBtn.addEventListener('click', () => {
      const u = document.getElementById('loginUsername');
      const p = document.getElementById('loginPassword');
      if (u) u.value = 'user';
      if (p) p.value = '123';
    });
  }

  // Nav sign-in trigger
  const navSignInBtn = document.getElementById('navSignInBtn');
  if (navSignInBtn) {
    navSignInBtn.addEventListener('click', () => openModal(authModal));
  }
}

// Drag and drop helper for file inputs
function setupDropzones() {
  const dropzones = [
    { zone: document.getElementById('addDropzone'), input: document.getElementById('addEmployeeImage') },
    { zone: document.getElementById('photoDropzone'), input: document.getElementById('modalPhotoFile') }
  ];

  dropzones.forEach(({ zone, input }) => {
    if (!zone || !input) return;

    ['dragenter', 'dragover'].forEach(eventName => {
      zone.addEventListener(eventName, (e) => {
        e.preventDefault();
        e.stopPropagation();
        zone.classList.add('drag-active');
      });
    });

    ['dragleave', 'drop'].forEach(eventName => {
      zone.addEventListener(eventName, (e) => {
        e.preventDefault();
        e.stopPropagation();
        zone.classList.remove('drag-active');
      });
    });

    zone.addEventListener('drop', (e) => {
      const files = e.dataTransfer.files;
      if (files && files.length > 0) {
        input.files = files;
        input.dispatchEvent(new Event('change', { bubbles: true }));
      }
    });
  });
}

function resetAddForm() {
  const form = document.getElementById('addEmployeeForm');
  if (form) form.reset();
  const preview = document.getElementById('addPhotoPreview');
  const prompt = document.getElementById('addDropzonePrompt');
  if (preview) {
    preview.src = '';
    preview.style.display = 'none';
  }
  if (prompt) prompt.style.display = 'block';
}

// ==========================================
// API Handlers: Stats & Employees
// ==========================================
async function loadStats() {
  try {
    const res = await apiFetch(`${API_BASE}/api/employees/stats`);
    if (res.ok) {
      const stats = await res.json();
      if (statEmployeeCount) statEmployeeCount.textContent = stats.employeeCount || 0;
      if (statDepartmentCount) statDepartmentCount.textContent = stats.departmentCount || 0;
      if (statProjectCount) statProjectCount.textContent = stats.projectCount || 0;
    }
  } catch (err) {
    console.error('Failed to load stats:', err);
  }
}

async function loadEmployees() {
  try {
    const res = await apiFetch(`${API_BASE}/api/employees`);
    if (res.ok) {
      state.employees = await res.json();
      updateDepartmentFilterOptions();
      renderEmployees();
    } else {
      showToast('Failed to load employees', 'error');
    }
  } catch (err) {
    console.error('Error fetching employees:', err);
    showToast('Network error loading employees', 'error');
  }
}

function updateDepartmentFilterOptions() {
  if (!departmentFilter) return;

  state.departments = new Set();
  state.employees.forEach(e => {
    if (e.department && e.department.trim()) {
      state.departments.add(e.department.trim());
    }
  });

  const currentVal = departmentFilter.value || 'ALL';
  departmentFilter.innerHTML = '<option value="ALL">All Departments</option>';

  const sortedDepts = Array.from(state.departments).sort();
  sortedDepts.forEach(dept => {
    const opt = document.createElement('option');
    opt.value = dept;
    opt.textContent = dept;
    departmentFilter.appendChild(opt);
  });

  departmentFilter.value = state.departments.has(currentVal) ? currentVal : 'ALL';
  state.selectedDepartment = departmentFilter.value;
}

// ==========================================
// Rendering: Grid & Table (Role-Aware)
// ==========================================
function renderEmployees() {
  const filtered = state.employees.filter(emp => {
    const matchDept = state.selectedDepartment === 'ALL' ||
      (emp.department && emp.department.trim().toLowerCase() === state.selectedDepartment.trim().toLowerCase());

    const query = state.searchTerm;
    const matchSearch = !query ||
      (emp.name && emp.name.toLowerCase().includes(query)) ||
      (emp.department && emp.department.toLowerCase().includes(query)) ||
      (emp.project && emp.project.toLowerCase().includes(query)) ||
      (emp.location && emp.location.toLowerCase().includes(query)) ||
      (emp.contact && emp.contact.toLowerCase().includes(query));

    return matchDept && matchSearch;
  });

  if (state.currentView === 'grid') {
    if (employeeGridContainer) employeeGridContainer.style.display = 'grid';
    if (employeeTableContainer) employeeTableContainer.style.display = 'none';
    renderGridView(filtered);
  } else {
    if (employeeGridContainer) employeeGridContainer.style.display = 'none';
    if (employeeTableContainer) employeeTableContainer.style.display = 'block';
    renderTableView(filtered);
  }
}

// Safe avatar error handler
window.handleAvatarError = function(img, initials) {
  img.onerror = null;
  const placeholder = document.createElement('div');
  placeholder.className = img.classList.contains('table-avatar')
    ? 'avatar-placeholder table-avatar-placeholder'
    : 'avatar-placeholder';
  placeholder.textContent = initials;
  img.replaceWith(placeholder);
};

function renderGridView(list) {
  if (!employeeGridContainer) return;

  if (list.length === 0) {
    employeeGridContainer.innerHTML = `
      <div class="empty-state">
        <i class="fa-solid fa-users-slash"></i>
        <h3>No Employees Found</h3>
        <p>No employee records matched your search query or department filter.</p>
        <button class="btn btn-primary" onclick="resetFilters()">Reset Filters</button>
      </div>
    `;
    return;
  }

  const adminActive = isAdmin();

  employeeGridContainer.innerHTML = list.map(emp => {
    const initials = escapeHtml(getInitials(emp.name));
    const photoSrc = emp.imageName ? `/images/${encodeURIComponent(emp.imageName)}` : '';
    const formattedDate = emp.date ? emp.date.replace('T', ' ') : 'N/A';

    return `
      <div class="employee-card" data-id="${emp.id}">
        <div class="card-banner"></div>
        <div class="card-header-inner">
          <div class="avatar-wrapper">
            ${emp.imageName ? `
              <img src="${photoSrc}" alt="${escapeHtml(emp.name || '')}" class="employee-avatar" onerror="handleAvatarError(this, '${initials}')">
            ` : `
              <div class="avatar-placeholder">${initials}</div>
            `}
          </div>
          <span class="emp-id-badge">ID #${emp.id}</span>
        </div>

        <div class="card-body">
          <h3 class="emp-name">${escapeHtml(emp.name || 'Unnamed Employee')}</h3>
          <span class="emp-department">${escapeHtml(emp.department || 'General')}</span>

          <div class="emp-details">
            <div class="detail-row">
              <i class="fa-solid fa-folder-tree"></i>
              <span>Project:</span>
              <span class="val">${escapeHtml(emp.project || 'Unassigned')}</span>
            </div>
            <div class="detail-row">
              <i class="fa-solid fa-calendar-day"></i>
              <span>Joined:</span>
              <span class="val">${escapeHtml(formattedDate)}</span>
            </div>
            <div class="detail-row">
              <i class="fa-solid fa-location-dot"></i>
              <span>Location:</span>
              <span class="val">${escapeHtml(emp.location || 'Remote')}</span>
            </div>
            <div class="detail-row">
              <i class="fa-solid fa-phone"></i>
              <span>Contact:</span>
              <span class="val">${escapeHtml(emp.contact || 'None')}</span>
            </div>
          </div>
        </div>

        <div class="card-footer">
          ${adminActive ? `
            <button class="icon-btn" title="Update Photo (Admin)" onclick="openPhotoModal(${emp.id})">
              <i class="fa-solid fa-camera"></i>
            </button>
            <button class="btn btn-secondary btn-sm" title="Edit Employee (Admin)" onclick="openEditModal(${emp.id})">
              <i class="fa-solid fa-pen-to-square"></i> Edit
            </button>
            <button class="btn btn-danger btn-sm" title="Delete Employee (Admin)" onclick="confirmDeleteEmployee(${emp.id})">
              <i class="fa-solid fa-trash"></i>
            </button>
          ` : `
            <span style="font-size: 0.78rem; color: var(--text-dim); display: inline-flex; align-items: center; gap: 6px;">
              <i class="fa-solid fa-eye" style="font-size: 11px;"></i> Directory View
            </span>
          `}
        </div>
      </div>
    `;
  }).join('');
}

function renderTableView(list) {
  if (!employeeTableBody) return;

  if (list.length === 0) {
    employeeTableBody.innerHTML = `
      <tr>
        <td colspan="7" style="text-align: center; padding: 40px; color: var(--text-muted);">
          No matching employees found.
        </td>
      </tr>
    `;
    return;
  }

  const adminActive = isAdmin();

  employeeTableBody.innerHTML = list.map(emp => {
    const initials = escapeHtml(getInitials(emp.name));
    const photoSrc = emp.imageName ? `/images/${encodeURIComponent(emp.imageName)}` : '';
    const formattedDate = emp.date ? emp.date.replace('T', ' ') : 'N/A';

    return `
      <tr>
        <td>
          <div class="table-user-cell">
            ${emp.imageName ? `
              <img src="${photoSrc}" class="table-avatar" alt="${escapeHtml(emp.name || '')}" onerror="handleAvatarError(this, '${initials}')">
            ` : `
              <div class="avatar-placeholder table-avatar-placeholder">${initials}</div>
            `}
            <div>
              <strong style="color: var(--text-main); display: block;">${escapeHtml(emp.name || 'Unnamed')}</strong>
              <small style="color: var(--text-dim);">ID #${emp.id}</small>
            </div>
          </div>
        </td>
        <td><span class="emp-department">${escapeHtml(emp.department || 'N/A')}</span></td>
        <td><strong>${escapeHtml(emp.project || 'Unassigned')}</strong></td>
        <td>${escapeHtml(formattedDate)}</td>
        <td>${escapeHtml(emp.location || 'Remote')}</td>
        <td>${escapeHtml(emp.contact || 'None')}</td>
        <td style="text-align: right;">
          ${adminActive ? `
            <div style="display: inline-flex; gap: 6px;">
              <button class="icon-btn" style="width:32px;height:32px;font-size:12px;" title="Update Photo" onclick="openPhotoModal(${emp.id})">
                <i class="fa-solid fa-camera"></i>
              </button>
              <button class="btn btn-secondary btn-sm" title="Edit Employee" onclick="openEditModal(${emp.id})">
                <i class="fa-solid fa-pen"></i>
              </button>
              <button class="btn btn-danger btn-sm" title="Delete Employee" onclick="confirmDeleteEmployee(${emp.id})">
                <i class="fa-solid fa-trash"></i>
              </button>
            </div>
          ` : `
            <span style="font-size: 0.75rem; color: var(--text-dim);">View Only</span>
          `}
        </td>
      </tr>
    `;
  }).join('');
}

window.resetFilters = function() {
  if (searchInput) searchInput.value = '';
  if (departmentFilter) departmentFilter.value = 'ALL';
  state.searchTerm = '';
  state.selectedDepartment = 'ALL';
  renderEmployees();
};

// ==========================================
// CRUD Operations (RBAC Protected)
// ==========================================
async function handleAddEmployee(e) {
  e.preventDefault();
  if (!isAdmin()) {
    showToast('Access Denied: Admin privileges required to add employees', 'error');
    return;
  }

  const form = e.target;
  const submitBtn = document.getElementById('saveAddEmployeeBtn');
  if (submitBtn) {
    submitBtn.disabled = true;
    submitBtn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Saving...';
  }

  try {
    const formData = new FormData(form);
    const res = await apiFetch(`${API_BASE}/api/employees`, {
      method: 'POST',
      body: formData
    });

    if (res.ok) {
      showToast('Employee created successfully!', 'success');
      resetAddForm();
      closeAllModals();
      await loadEmployees();
      await loadStats();
    } else {
      const err = await res.json().catch(() => ({}));
      showToast(err.message || 'Failed to add employee', 'error');
    }
  } catch (err) {
    console.error('Error adding employee:', err);
    showToast('Network error while adding employee', 'error');
  } finally {
    if (submitBtn) {
      submitBtn.disabled = false;
      submitBtn.innerHTML = '<i class="fa-solid fa-check"></i> Save Employee';
    }
  }
}

window.openEditModal = function(id) {
  if (!isAdmin()) {
    showToast('Access Denied: Admin privileges required to edit employees', 'error');
    return;
  }

  const emp = state.employees.find(e => e.id === id);
  if (!emp) return;

  const idInput = document.getElementById('editEmpId');
  const nameInput = document.getElementById('editName');
  const deptInput = document.getElementById('editDepartment');
  const projInput = document.getElementById('editProject');
  const dateInput = document.getElementById('editDate');
  const locInput = document.getElementById('editLocation');
  const contactInput = document.getElementById('editContact');

  if (idInput) idInput.value = emp.id;
  if (nameInput) nameInput.value = emp.name || '';
  if (deptInput) deptInput.value = emp.department || '';
  if (projInput) projInput.value = emp.project || '';
  if (dateInput) dateInput.value = emp.date ? emp.date.split('T')[0] : '';
  if (locInput) locInput.value = emp.location || '';
  if (contactInput) contactInput.value = emp.contact || '';

  openModal(editEmployeeModal);
};

async function handleEditEmployee(e) {
  e.preventDefault();
  if (!isAdmin()) {
    showToast('Access Denied: Admin privileges required', 'error');
    return;
  }

  const idInput = document.getElementById('editEmpId');
  const id = idInput ? idInput.value : null;
  if (!id) return;

  const submitBtn = document.getElementById('saveEditEmployeeBtn');
  if (submitBtn) {
    submitBtn.disabled = true;
    submitBtn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Updating...';
  }

  const payload = {
    name: (document.getElementById('editName')?.value || '').trim(),
    department: (document.getElementById('editDepartment')?.value || '').trim(),
    project: (document.getElementById('editProject')?.value || '').trim(),
    date: document.getElementById('editDate')?.value || '',
    location: (document.getElementById('editLocation')?.value || '').trim(),
    contact: (document.getElementById('editContact')?.value || '').trim()
  };

  try {
    const res = await apiFetch(`${API_BASE}/api/employees/${id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });

    if (res.ok) {
      showToast('Employee updated successfully!', 'success');
      closeAllModals();
      await loadEmployees();
      await loadStats();
    } else {
      const err = await res.json().catch(() => ({}));
      showToast(err.message || 'Failed to update employee', 'error');
    }
  } catch (err) {
    console.error('Error updating employee:', err);
    showToast('Network error updating employee', 'error');
  } finally {
    if (submitBtn) {
      submitBtn.disabled = false;
      submitBtn.innerHTML = '<i class="fa-solid fa-floppy-disk"></i> Update Changes';
    }
  }
}

window.openPhotoModal = function(id) {
  if (!isAdmin()) {
    showToast('Access Denied: Admin privileges required to update photos', 'error');
    return;
  }

  const photoEmpId = document.getElementById('photoEmpId');
  if (photoEmpId) photoEmpId.value = id;
  const form = document.getElementById('uploadPhotoForm');
  if (form) form.reset();
  const preview = document.getElementById('photoModalPreview');
  const prompt = document.getElementById('photoDropzonePrompt');
  if (preview) {
    preview.src = '';
    preview.style.display = 'none';
  }
  if (prompt) prompt.style.display = 'block';

  openModal(uploadPhotoModal);
};

async function handleUploadPhoto(e) {
  e.preventDefault();
  if (!isAdmin()) {
    showToast('Access Denied: Admin privileges required', 'error');
    return;
  }

  const idInput = document.getElementById('photoEmpId');
  const id = idInput ? idInput.value : null;
  const fileInput = document.getElementById('modalPhotoFile');

  if (!fileInput || !fileInput.files || fileInput.files.length === 0) {
    showToast('Please select a photo first', 'error');
    return;
  }

  const formData = new FormData();
  formData.append('image', fileInput.files[0]);

  try {
    const res = await apiFetch(`${API_BASE}/api/employees/${id}/photo`, {
      method: 'POST',
      body: formData
    });

    if (res.ok) {
      showToast('Photo uploaded successfully!', 'success');
      closeAllModals();
      await loadEmployees();
    } else {
      const err = await res.json().catch(() => ({}));
      showToast(err.message || 'Failed to upload photo', 'error');
    }
  } catch (err) {
    console.error('Error uploading photo:', err);
    showToast('Network error uploading photo', 'error');
  }
}

window.confirmDeleteEmployee = async function(id) {
  if (!isAdmin()) {
    showToast('Access Denied: Admin privileges required to delete employees', 'error');
    return;
  }

  const emp = state.employees.find(e => e.id === id);
  const name = emp && emp.name ? emp.name : `ID #${id}`;

  if (confirm(`Are you sure you want to delete employee "${name}" (ID #${id})?`)) {
    try {
      const res = await apiFetch(`${API_BASE}/api/employees/${id}`, {
        method: 'DELETE'
      });

      if (res.ok) {
        showToast(`Employee "${name}" deleted`, 'info');
        await loadEmployees();
        await loadStats();
      } else {
        const err = await res.json().catch(() => ({}));
        showToast(err.message || 'Failed to delete employee', 'error');
      }
    } catch (err) {
      console.error('Error deleting employee:', err);
      showToast('Network error deleting employee', 'error');
    }
  }
};

// ==========================================
// Authentication: Sign In, Sign Up, Session
// ==========================================
async function checkAuthSession() {
  try {
    const res = await apiFetch(`${API_BASE}/api/auth/me`);
    if (res.ok) {
      const data = await res.json();
      state.currentUser = data.username;
      state.currentRole = data.role;
    } else {
      state.currentUser = null;
      state.currentRole = null;
      state.authToken = null;
      localStorage.removeItem('auth_token');
    }
  } catch (err) {
    state.currentUser = null;
    state.currentRole = null;
  }
  renderAuthNav();
  renderEmployees();
}

function renderAuthNav() {
  if (!authNavContainer) return;

  if (state.currentUser) {
    const initials = escapeHtml(state.currentUser.substring(0, 2).toUpperCase());
    const roleLabel = isAdmin() ? 'Admin' : 'User';
    const roleClass = isAdmin() ? 'admin' : 'user';

    authNavContainer.innerHTML = `
      <div style="display: flex; align-items: center; gap: 10px;">
        <div class="user-badge">
          <div class="avatar-sm">${initials}</div>
          <span>${escapeHtml(state.currentUser)}</span>
          <span class="role-badge ${roleClass}">${roleLabel}</span>
        </div>
        <button class="btn btn-secondary btn-sm" onclick="handleLogout()" title="Sign Out">
          <i class="fa-solid fa-arrow-right-from-bracket"></i>
        </button>
      </div>
    `;
  } else {
    authNavContainer.innerHTML = `
      <button class="btn btn-primary btn-sm" onclick="openModal(document.getElementById('authModal'))">
        <i class="fa-solid fa-right-to-bracket"></i> Sign In
      </button>
    `;
  }

  // Admin visibility control for Add Employee button
  const openAddBtn = document.getElementById('openAddModalBtn');
  if (openAddBtn) {
    openAddBtn.style.display = isAdmin() ? 'inline-flex' : 'none';
  }
}

async function handleSignIn(e) {
  e.preventDefault();
  const username = (document.getElementById('loginUsername')?.value || '').trim();
  const password = (document.getElementById('loginPassword')?.value || '').trim();

  if (!username || !password) {
    showToast('Username and password are required', 'error');
    return;
  }

  try {
    const res = await fetch(`${API_BASE}/api/auth/signin`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, password })
    });

    if (res.ok) {
      const data = await res.json();
      state.currentUser = data.username;
      state.currentRole = data.role;
      state.authToken = data.token;
      if (data.token) {
        localStorage.setItem('auth_token', data.token);
      }
      renderAuthNav();
      renderEmployees();
      closeAllModals();
      showToast(`Welcome back, ${data.username}! Signed in as ${isAdmin() ? 'Admin' : 'User'}.`, 'success');
    } else {
      const err = await res.json().catch(() => ({}));
      showToast(err.message || 'Invalid username or password', 'error');
    }
  } catch (err) {
    console.error('Sign in error:', err);
    showToast('Network error during sign in', 'error');
  }
}

async function handleSignUp(e) {
  e.preventDefault();
  const username = (document.getElementById('regUsername')?.value || '').trim();
  const email = (document.getElementById('regEmail')?.value || '').trim();
  const password = (document.getElementById('regPassword')?.value || '').trim();

  if (!username || !password) {
    showToast('Username and password are required', 'error');
    return;
  }

  try {
    const res = await fetch(`${API_BASE}/api/auth/signup`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, email, password })
    });

    if (res.ok) {
      const data = await res.json();
      state.currentUser = data.username;
      state.currentRole = data.role;
      state.authToken = data.token;
      if (data.token) {
        localStorage.setItem('auth_token', data.token);
      }
      renderAuthNav();
      renderEmployees();
      closeAllModals();
      showToast('Account created successfully! Signed in.', 'success');
    } else {
      const err = await res.json().catch(() => ({}));
      showToast(err.message || 'Username already taken', 'error');
    }
  } catch (err) {
    console.error('Sign up error:', err);
    showToast('Network error during registration', 'error');
  }
}

window.handleLogout = async function() {
  try {
    await apiFetch(`${API_BASE}/api/auth/logout`, { method: 'POST' });
  } catch (err) {
    console.error('Logout error:', err);
  } finally {
    state.currentUser = null;
    state.currentRole = null;
    state.authToken = null;
    localStorage.removeItem('auth_token');
    renderAuthNav();
    renderEmployees();
    showToast('You have been logged out', 'info');
  }
};

// ==========================================
// Helpers: Modals, Toast, Utilities
// ==========================================
function openModal(modal) {
  if (modal) modal.classList.add('active');
}

function closeAllModals() {
  document.querySelectorAll('.modal-overlay').forEach(modal => {
    modal.classList.remove('active');
  });
}

function showToast(message, type = 'info') {
  const container = document.getElementById('toastContainer');
  if (!container) return;

  const toast = document.createElement('div');
  toast.className = `toast ${type}`;

  const iconMap = {
    success: 'fa-circle-check',
    error: 'fa-circle-exclamation',
    info: 'fa-circle-info'
  };

  toast.innerHTML = `
    <i class="fa-solid ${iconMap[type] || 'fa-bell'}" style="font-size: 18px;"></i>
    <span style="font-size: 0.9rem; font-weight: 500;">${escapeHtml(message)}</span>
  `;

  container.appendChild(toast);

  setTimeout(() => {
    toast.style.opacity = '0';
    toast.style.transform = 'translateY(10px)';
    setTimeout(() => toast.remove(), 250);
  }, 3500);
}

function getInitials(name) {
  if (!name || !name.trim()) return 'EMP';
  const parts = name.trim().split(/\s+/);
  if (parts.length === 1) return parts[0].substring(0, 2).toUpperCase();
  return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
}

function escapeHtml(str) {
  if (!str) return '';
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;');
}
