/**
 * StaffSync - Employee Management System
 * Interactive Client Logic
 */

// Configuration & State
const API_BASE = ''; // Relative path so it works seamlessly on Spring Boot port 8081
let state = {
  currentUser: null,
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
  checkAuthSession();
  loadStats();
  loadEmployees();
});

// ==========================================
// Theme Management
// ==========================================
function initTheme() {
  const savedTheme = localStorage.getItem('theme') || 'dark';
  document.documentElement.setAttribute('data-theme', savedTheme);
  updateThemeIcon(savedTheme);
}

function updateThemeIcon(theme) {
  if (theme === 'light') {
    themeIcon.className = 'fa-solid fa-moon';
  } else {
    themeIcon.className = 'fa-solid fa-sun';
  }
}

themeToggleBtn.addEventListener('click', () => {
  const currentTheme = document.documentElement.getAttribute('data-theme');
  const newTheme = currentTheme === 'light' ? 'dark' : 'light';
  document.documentElement.setAttribute('data-theme', newTheme);
  localStorage.setItem('theme', newTheme);
  updateThemeIcon(newTheme);
});

// ==========================================
// Event Listeners
// ==========================================
function setupEventListeners() {
  // View Toggle Buttons
  const viewGridBtn = document.getElementById('viewGridBtn');
  const viewTableBtn = document.getElementById('viewTableBtn');

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

  // Search & Filter
  searchInput.addEventListener('input', (e) => {
    state.searchTerm = e.target.value.trim().toLowerCase();
    renderEmployees();
  });

  departmentFilter.addEventListener('change', (e) => {
    state.selectedDepartment = e.target.value;
    renderEmployees();
  });

  // Modal Open/Close Controls
  document.getElementById('openAddModalBtn').addEventListener('click', () => {
    openModal(addEmployeeModal);
  });

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

  // Image preview in Add Modal
  const addImageInput = document.getElementById('addEmployeeImage');
  const addPhotoPreview = document.getElementById('addPhotoPreview');
  const addDropzonePrompt = document.getElementById('addDropzonePrompt');
  addImageInput.addEventListener('change', (e) => {
    const file = e.target.files[0];
    if (file) {
      const reader = new FileReader();
      reader.onload = (re) => {
        addPhotoPreview.src = re.target.result;
        addPhotoPreview.style.display = 'block';
        addDropzonePrompt.style.display = 'none';
      };
      reader.readAsDataURL(file);
    }
  });

  // Image preview in Photo Modal
  const modalPhotoFile = document.getElementById('modalPhotoFile');
  const photoModalPreview = document.getElementById('photoModalPreview');
  const photoDropzonePrompt = document.getElementById('photoDropzonePrompt');
  modalPhotoFile.addEventListener('change', (e) => {
    const file = e.target.files[0];
    if (file) {
      const reader = new FileReader();
      reader.onload = (re) => {
        photoModalPreview.src = re.target.result;
        photoModalPreview.style.display = 'block';
        photoDropzonePrompt.style.display = 'none';
      };
      reader.readAsDataURL(file);
    }
  });

  // Add Employee Form Submission
  document.getElementById('addEmployeeForm').addEventListener('submit', handleAddEmployee);

  // Edit Employee Form Submission
  document.getElementById('editEmployeeForm').addEventListener('submit', handleEditEmployee);

  // Upload Photo Form Submission
  document.getElementById('uploadPhotoForm').addEventListener('submit', handleUploadPhoto);

  // Auth Form Tabs
  const tabSignIn = document.getElementById('tabSignIn');
  const tabSignUp = document.getElementById('tabSignUp');
  const signInForm = document.getElementById('signInForm');
  const signUpForm = document.getElementById('signUpForm');

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

  // Quick Demo fill
  document.getElementById('quickDemoBtn')?.addEventListener('click', () => {
    document.getElementById('loginUsername').value = 'admin';
    document.getElementById('loginPassword').value = '123';
  });

  // Sign In / Sign Up Handlers
  signInForm.addEventListener('submit', handleSignIn);
  signUpForm.addEventListener('submit', handleSignUp);
}

// ==========================================
// API Handlers: Stats & Employees
// ==========================================
async function loadStats() {
  try {
    const res = await fetch(`${API_BASE}/api/employees/stats`);
    if (res.ok) {
      const stats = await res.json();
      statEmployeeCount.textContent = stats.employeeCount || 0;
      statDepartmentCount.textContent = stats.departmentCount || 0;
      statProjectCount.textContent = stats.projectCount || 0;
    }
  } catch (err) {
    console.error('Failed to load stats:', err);
  }
}

async function loadEmployees() {
  try {
    const res = await fetch(`${API_BASE}/api/employees`);
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
  state.departments = new Set();
  state.employees.forEach(e => {
    if (e.department && e.department.trim()) {
      state.departments.add(e.department.trim());
    }
  });

  const currentVal = departmentFilter.value;
  departmentFilter.innerHTML = '<option value="ALL">All Departments</option>';
  state.departments.forEach(dept => {
    const opt = document.createElement('option');
    opt.value = dept;
    opt.textContent = dept;
    departmentFilter.appendChild(opt);
  });
  departmentFilter.value = currentVal || 'ALL';
}

// ==========================================
// Rendering: Grid & Table
// ==========================================
function renderEmployees() {
  const filtered = state.employees.filter(emp => {
    const matchDept = state.selectedDepartment === 'ALL' || 
                      (emp.department && emp.department.toLowerCase() === state.selectedDepartment.toLowerCase());
    
    const query = state.searchTerm;
    const matchSearch = !query || 
      (emp.name && emp.name.toLowerCase().includes(query)) ||
      (emp.department && emp.department.toLowerCase().includes(query)) ||
      (emp.project && emp.project.toLowerCase().includes(query)) ||
      (emp.location && emp.location.toLowerCase().includes(query));

    return matchDept && matchSearch;
  });

  if (state.currentView === 'grid') {
    employeeGridContainer.style.display = 'grid';
    employeeTableContainer.style.display = 'none';
    renderGridView(filtered);
  } else {
    employeeGridContainer.style.display = 'none';
    employeeTableContainer.style.display = 'block';
    renderTableView(filtered);
  }
}

function renderGridView(list) {
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

  employeeGridContainer.innerHTML = list.map(emp => {
    const initials = getInitials(emp.name);
    const photoSrc = emp.imageName ? `/images/${emp.imageName}` : '';
    const formattedDate = emp.date ? emp.date.replace('T', ' ') : 'N/A';

    return `
      <div class="employee-card" data-id="${emp.id}">
        <div class="card-banner"></div>
        <div class="card-header-inner">
          <div class="avatar-wrapper">
            ${emp.imageName ? `
              <img src="${photoSrc}" alt="${emp.name}" class="employee-avatar" onerror="this.outerHTML='<div class=\\'avatar-placeholder\\'>${initials}</div>'">
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
          <button class="icon-btn" title="Update Photo" onclick="openPhotoModal(${emp.id})">
            <i class="fa-solid fa-camera"></i>
          </button>
          <button class="btn btn-secondary btn-sm" onclick="openEditModal(${emp.id})">
            <i class="fa-solid fa-pen-to-square"></i> Edit
          </button>
          <button class="btn btn-danger btn-sm" onclick="confirmDeleteEmployee(${emp.id}, '${escapeHtml(emp.name)}')">
            <i class="fa-solid fa-trash"></i>
          </button>
        </div>
      </div>
    `;
  }).join('');
}

function renderTableView(list) {
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

  employeeTableBody.innerHTML = list.map(emp => {
    const initials = getInitials(emp.name);
    const photoSrc = emp.imageName ? `/images/${emp.imageName}` : '';
    const formattedDate = emp.date ? emp.date.replace('T', ' ') : 'N/A';

    return `
      <tr>
        <td>
          <div class="table-user-cell">
            ${emp.imageName ? `
              <img src="${photoSrc}" class="table-avatar" alt="${emp.name}" onerror="this.outerHTML='<div class=\\'avatar-placeholder\\' style=\\'width:40px;height:40px;font-size:14px;\\'>${initials}</div>'">
            ` : `
              <div class="avatar-placeholder" style="width:40px;height:40px;font-size:14px;">${initials}</div>
            `}
            <div>
              <strong style="color: var(--text-main); display: block;">${escapeHtml(emp.name)}</strong>
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
          <div style="display: inline-flex; gap: 6px;">
            <button class="icon-btn" style="width:32px;height:32px;font-size:12px;" title="Update Photo" onclick="openPhotoModal(${emp.id})">
              <i class="fa-solid fa-camera"></i>
            </button>
            <button class="btn btn-secondary btn-sm" onclick="openEditModal(${emp.id})">
              <i class="fa-solid fa-pen"></i>
            </button>
            <button class="btn btn-danger btn-sm" onclick="confirmDeleteEmployee(${emp.id}, '${escapeHtml(emp.name)}')">
              <i class="fa-solid fa-trash"></i>
            </button>
          </div>
        </td>
      </tr>
    `;
  }).join('');
}

function resetFilters() {
  searchInput.value = '';
  departmentFilter.value = 'ALL';
  state.searchTerm = '';
  state.selectedDepartment = 'ALL';
  renderEmployees();
}

// ==========================================
// CRUD Operations
// ==========================================
async function handleAddEmployee(e) {
  e.preventDefault();
  const form = e.target;
  const submitBtn = document.getElementById('saveAddEmployeeBtn');
  submitBtn.disabled = true;
  submitBtn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Saving...';

  try {
    const formData = new FormData(form);
    const res = await fetch(`${API_BASE}/api/employees`, {
      method: 'POST',
      body: formData
    });

    if (res.ok) {
      showToast('Employee created successfully!', 'success');
      form.reset();
      document.getElementById('addPhotoPreview').style.display = 'none';
      document.getElementById('addDropzonePrompt').style.display = 'block';
      closeAllModals();
      await loadEmployees();
      await loadStats();
    } else {
      showToast('Failed to add employee', 'error');
    }
  } catch (err) {
    console.error('Error adding employee:', err);
    showToast('Network error while adding employee', 'error');
  } finally {
    submitBtn.disabled = false;
    submitBtn.innerHTML = '<i class="fa-solid fa-check"></i> Save Employee';
  }
}

window.openEditModal = function(id) {
  const emp = state.employees.find(e => e.id === id);
  if (!emp) return;

  document.getElementById('editEmpId').value = emp.id;
  document.getElementById('editName').value = emp.name || '';
  document.getElementById('editDepartment').value = emp.department || '';
  document.getElementById('editProject').value = emp.project || '';
  document.getElementById('editDate').value = emp.date ? emp.date.split('T')[0] : '';
  document.getElementById('editLocation').value = emp.location || '';
  document.getElementById('editContact').value = emp.contact || '';

  openModal(editEmployeeModal);
};

async function handleEditEmployee(e) {
  e.preventDefault();
  const id = document.getElementById('editEmpId').value;
  const submitBtn = document.getElementById('saveEditEmployeeBtn');
  submitBtn.disabled = true;
  submitBtn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Updating...';

  const payload = {
    name: document.getElementById('editName').value.trim(),
    department: document.getElementById('editDepartment').value.trim(),
    project: document.getElementById('editProject').value.trim(),
    date: document.getElementById('editDate').value,
    location: document.getElementById('editLocation').value.trim(),
    contact: document.getElementById('editContact').value.trim()
  };

  try {
    const res = await fetch(`${API_BASE}/api/employees/${id}`, {
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
      showToast('Failed to update employee', 'error');
    }
  } catch (err) {
    console.error('Error updating employee:', err);
    showToast('Network error updating employee', 'error');
  } finally {
    submitBtn.disabled = false;
    submitBtn.innerHTML = '<i class="fa-solid fa-floppy-disk"></i> Update Changes';
  }
}

window.openPhotoModal = function(id) {
  document.getElementById('photoEmpId').value = id;
  document.getElementById('uploadPhotoForm').reset();
  document.getElementById('photoModalPreview').style.display = 'none';
  document.getElementById('photoDropzonePrompt').style.display = 'block';
  openModal(uploadPhotoModal);
};

async function handleUploadPhoto(e) {
  e.preventDefault();
  const id = document.getElementById('photoEmpId').value;
  const fileInput = document.getElementById('modalPhotoFile');
  if (!fileInput.files || fileInput.files.length === 0) {
    showToast('Please select a photo first', 'error');
    return;
  }

  const formData = new FormData();
  formData.append('image', fileInput.files[0]);

  try {
    const res = await fetch(`${API_BASE}/api/employees/${id}/photo`, {
      method: 'POST',
      body: formData
    });

    if (res.ok) {
      showToast('Photo uploaded successfully!', 'success');
      closeAllModals();
      await loadEmployees();
    } else {
      showToast('Failed to upload photo', 'error');
    }
  } catch (err) {
    console.error('Error uploading photo:', err);
    showToast('Network error uploading photo', 'error');
  }
}

window.confirmDeleteEmployee = async function(id, name) {
  if (confirm(`Are you sure you want to delete employee "${name}" (ID #${id})?`)) {
    try {
      const res = await fetch(`${API_BASE}/api/employees/${id}`, {
        method: 'DELETE'
      });

      if (res.ok) {
        showToast(`Employee "${name}" deleted`, 'info');
        await loadEmployees();
        await loadStats();
      } else {
        showToast('Failed to delete employee', 'error');
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
    const res = await fetch(`${API_BASE}/api/auth/me`);
    if (res.ok) {
      const data = await res.json();
      state.currentUser = data.username;
      renderAuthNav();
    } else {
      state.currentUser = null;
      renderAuthNav();
    }
  } catch (err) {
    state.currentUser = null;
    renderAuthNav();
  }
}

function renderAuthNav() {
  if (state.currentUser) {
    const initials = state.currentUser.substring(0, 2).toUpperCase();
    authNavContainer.innerHTML = `
      <div style="display: flex; align-items: center; gap: 10px;">
        <div class="user-badge">
          <div class="avatar-sm">${initials}</div>
          <span>${escapeHtml(state.currentUser)}</span>
        </div>
        <button class="btn btn-secondary btn-sm" onclick="handleLogout()" title="Sign Out">
          <i class="fa-solid fa-arrow-right-from-bracket"></i>
        </button>
      </div>
    `;
  } else {
    authNavContainer.innerHTML = `
      <button class="btn btn-primary btn-sm" onclick="openModal(authModal)">
        <i class="fa-solid fa-right-to-bracket"></i> Sign In
      </button>
    `;
  }
}

async function handleSignIn(e) {
  e.preventDefault();
  const username = document.getElementById('loginUsername').value.trim();
  const password = document.getElementById('loginPassword').value.trim();

  try {
    const res = await fetch(`${API_BASE}/api/auth/signin`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, password })
    });

    if (res.ok) {
      const data = await res.json();
      state.currentUser = data.username;
      renderAuthNav();
      closeAllModals();
      showToast(`Welcome back, ${data.username}!`, 'success');
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
  const username = document.getElementById('regUsername').value.trim();
  const email = document.getElementById('regEmail').value.trim();
  const password = document.getElementById('regPassword').value.trim();

  try {
    const res = await fetch(`${API_BASE}/api/auth/signup`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, email, password })
    });

    if (res.ok) {
      showToast('Account created successfully! Signing in...', 'success');
      // Auto sign in
      const signinRes = await fetch(`${API_BASE}/api/auth/signin`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password })
      });
      if (signinRes.ok) {
        state.currentUser = username;
        renderAuthNav();
      }
      closeAllModals();
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
    await fetch(`${API_BASE}/api/auth/logout`, { method: 'POST' });
    state.currentUser = null;
    renderAuthNav();
    showToast('You have been logged out', 'info');
  } catch (err) {
    console.error('Logout error:', err);
  }
};

// ==========================================
// Helpers: Modals, Toast, Utilities
// ==========================================
function openModal(modal) {
  modal.classList.add('active');
}

function closeAllModals() {
  document.querySelectorAll('.modal-overlay').forEach(modal => {
    modal.classList.remove('active');
  });
}

function showToast(message, type = 'info') {
  const container = document.getElementById('toastContainer');
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
  if (!name) return 'EMP';
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
