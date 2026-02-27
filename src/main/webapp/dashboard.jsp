<%@page import="java.util.List"%>
<%@page import="com.mainapp.entity.Employee"%>
<%@page import="com.mainapp.entity.User"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Employee Management Dashboard</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">

  <style>
    :root {
      --bg-light: #f8f9fa;
      --bg-dark: #1e1e2f;
      --text-light: #333;
      --text-dark: #f1f1f1;
      --card-bg-light: #fff;
      --card-bg-dark: #2c2c3a;
    }

    body {
      background-color: var(--bg-light);
      color: var(--text-light);
      font-family: 'Poppins', sans-serif;
      transition: 0.3s;
    }

    body.dark-mode {
      background-color: var(--bg-dark);
      color: var(--text-dark);
    }

    .navbar {
      background: linear-gradient(90deg, #2575fc, #6a11cb);
    }

    .navbar-brand, .nav-link, .logout-btn {
      color: #fff !important;
    }

    .sidebar {
      background-color: var(--card-bg-light);
      min-height: 100vh;
      box-shadow: 0 4px 10px rgba(0,0,0,0.05);
      transition: 0.3s;
    }

    body.dark-mode .sidebar {
      background-color: var(--card-bg-dark);
    }

    .sidebar a {
      display: block;
      padding: 12px 20px;
      color: inherit;
      text-decoration: none;
      font-weight: 500;
      transition: 0.2s;
    }

    .sidebar a:hover, .sidebar a.active {
      background-color: #2575fc;
      color: #fff;
      border-radius: 8px;
    }

    .card {
      border: none;
      border-radius: 12px;
      box-shadow: 0 4px 10px rgba(0,0,0,0.1);
      transition: 0.3s;
      background-color: var(--card-bg-light);
    }

    .card:hover {
      transform: translateY(-3px);
    }

    body.dark-mode .card {
      background-color: var(--card-bg-dark);
      color: var(--text-dark);
    }

    .employee-img {
      width: 100%;
      height: 150px;
      object-fit: cover;
      border-radius: 12px 12px 0 0;
    }

    .hidden-section {
      display: none;
    }

    .search-bar {
      display: flex;
      justify-content: space-between;
      align-items: center;
      flex-wrap: wrap;
    }

    @media (max-width: 480px) {
      .search-bar {
        flex-direction: column;
        gap: 0.5rem;
      }
    }
  </style>
</head>
<body>

<%
User u = (User)session.getAttribute("token");
if(u != null){
%>

<%
List<Employee> le = (List<Employee>)request.getAttribute("le");
long ec = (long)request.getAttribute("ec");
long dc = (long)request.getAttribute("dc");
long pc = (long)request.getAttribute("pc");
%>

  <!-- Navbar -->
  <nav class="navbar navbar-expand-lg">
    <div class="container-fluid">
      <a class="navbar-brand fw-bold" href="#">Employee Dashboard</a>
      <div class="d-flex align-items-center ms-auto gap-2">
        <button class="btn btn-outline-light" id="darkModeToggle">🌙</button>
        <a href="logout" class="btn btn-outline-light logout-btn">Logout</a>
      </div>
    </div>
  </nav>

  <div class="container-fluid">
    <div class="row">
      <!-- Sidebar -->
      <div class="col-md-3 col-lg-2 sidebar p-3">
        <a href="#" class="active" onclick="showSection('dashboard')">🏠 Dashboard</a>
        <a href="#" onclick="showSection('employees')">👨‍💼 Employees</a>
        <a href="#" onclick="showSection('add')">➕ Add Employee</a>
        <a href="#" onclick="showSection('edit')">✏ Edit Employee</a>
        <a href="#" onclick="showSection('delete')">🗑 Delete Employee</a>
        <a href="#" onclick="showSection('fileInfo')">📁 File Info</a>
      </div>

      <!-- Main Content -->
      <div class="col-md-9 col-lg-10 p-4">

        <!-- Dashboard Section -->
        <section id="dashboardSection">
          <h3 class="fw-bold mb-4">Welcome to Employee Management System</h3>
          <div class="row g-3">
            <div class="col-md-4">
              <div class="card text-center p-3">
                <h5>Total Employees</h5>
                <h2 class="text-primary fw-bold"><%=ec %></h2>
              </div>
            </div>
            <div class="col-md-4">
              <div class="card text-center p-3">
                <h5>Departments</h5>
                <h2 class="text-success fw-bold"><%=dc %></h2>
              </div>
            </div>
            <div class="col-md-4">
              <div class="card text-center p-3">
                <h5>Projects</h5>
                <h2 class="text-warning fw-bold"><%=pc %></h2>
              </div>
            </div>
          </div>
        </section>

        <!-- Employees Section -->
        <section id="employeesSection" class="hidden-section">
          <div class="search-bar mb-3">
            <input type="text" id="searchInput" class="form-control w-50" placeholder="Search employee by name...">
            <select id="deptFilter" class="form-select w-25">
              <option value="">All Departments</option>
              <option value="IT">IT</option>
              <option value="HR">HR</option>
              <option value="Finance">Finance</option>
              <option value="Sales">Sales</option>
            </select>
          </div>

          <div class="row g-4" id="employeeContainer">
            
		 	<%
		 	for(Employee e : le){
		 	%>
		 		<div class="col-md-4 employee-card" data-name="John Doe" data-dept="IT">
	              <div class="card">
	                <img src="/EmpImages/<%=e.getImageName()%>" class="employee-img" alt="Employee">
	                <div class="card-body">
	                  <h5 class="card-title"><%=e.getName()%></h5>
	                  <p><strong>ID:</strong> <%=e.getId()%></p>
	                  <p><strong>Department:</strong> <%=e.getDepartment()%></p>
	                  <p><strong>Project:</strong> <%=e.getProject()%></p>
	                  <p><strong>Joining:</strong> <%=e.getDate()%></p>
	                  <p><strong>Location:</strong> <%=e.getLocation()%></p>
	                  <p><strong>Contact:</strong> <%=e.getContact()%></p>
	                </div>
	              </div>
	            </div>
		 	<%
		 	}
		 	%>
            
          </div>
        </section>

        <!-- Add Section -->
        <section id="addSection" class="hidden-section">
          <h4 class="fw-bold mb-4">Add Employee</h4>
          <form action="addemp" method="post" enctype="multipart/form-data" class="row g-3">
            <div class="col-md-6">
              <label class="form-label">Name</label>
              <input name="name" type="text" class="form-control" placeholder="Enter name">
            </div>
            <div class="col-md-6">
              <label class="form-label">Image</label>
              <input name="image" type="file" class="form-control" placeholder="Enter ID">
            </div>
            <div class="col-md-6">
              <label class="form-label">Department</label>
              <input name="department" type="text" class="form-control" placeholder="Enter department">
            </div>
            <div class="col-md-6">
              <label class="form-label">Project</label>
              <input name="project" type="text" class="form-control" placeholder="Enter project">
            </div>
            <div class="col-md-6">
              <label class="form-label">Joining Date</label>
              <input name="date" type="datetime-local" class="form-control">
            </div>
            <div class="col-md-6">
              <label class="form-label">Location</label>
              <input name="location" type="text" class="form-control" placeholder="Enter location">
            </div>
            <div class="col-12">
              <label class="form-label">Contact</label>
              <input name="contact" type="text" class="form-control" placeholder="Enter contact number">
            </div>
            <div class="col-12">
              <button class="btn btn-primary">Add Employee</button>
            </div>
          </form>
        </section>

        <!-- Edit Section -->
        <section id="editSection" class="hidden-section">
          <h4 class="fw-bold mb-4">Edit Employee</h4>
          <form class="row g-3" action="editemp" method="post">
            <div class="col-md-6">
              <label class="form-label">Employee ID</label>
              <input name="id" type="text" class="form-control" placeholder="Enter ID">
            </div>
            <div class="col-md-6">
              <label class="form-label">Update Department</label>
              <input name="department" type="text" class="form-control" placeholder="Enter department">
            </div>
            <div class="col-md-6">
              <label class="form-label">Update Project</label>
              <input name="project" type="text" class="form-control" placeholder="Enter project">
            </div>
            <div class="col-12">
              <button class="btn btn-warning text-white">Update Employee</button>
            </div>
          </form>
        </section>

        <!-- Delete Section -->
        <section id="deleteSection" class="hidden-section">
          <h4 class="fw-bold mb-4">Delete Employee</h4>
          <form class="row g-3" action="deleteemp" method="post">
            <div class="col-md-6">
              <label class="form-label">Employee ID</label>
              <input name="id" type="text" class="form-control" placeholder="Enter ID">
            </div>
            <div class="col-12">
              <button class="btn btn-danger">Delete Employee</button>
            </div>
          </form>
        </section>

        <!-- File Info -->
        <section id="fileInfoSection" class="hidden-section">
          <h4 class="fw-bold mb-4">File Information</h4>
          <div class="card p-3">
            <p><strong>Username:</strong> <%=u.getUsername()%></p>
            <p><strong>Email:</strong> <%=u.getEmail()%></p>
            <p><strong>Password:</strong> <%=u.getPassword()%></p>
          </div>
        </section>
      </div>
    </div>
  </div>
<%	
} else {
	session.setAttribute("msg", "Try Again");
	response.sendRedirect("SignupAndSignin.jsp");
}
%>


  <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
  <script>
    // Section switching
    function showSection(section) {
      const sections = ['dashboard', 'employees', 'add', 'edit', 'delete', 'fileInfo'];
      sections.forEach(id => document.getElementById(id + 'Section').classList.add('hidden-section'));
      document.getElementById(section + 'Section').classList.remove('hidden-section');
      document.querySelectorAll('.sidebar a').forEach(a => a.classList.remove('active'));
      event.target.classList.add('active');
    }

    // Dark Mode Toggle
    const toggleBtn = document.getElementById('darkModeToggle');
    toggleBtn.addEventListener('click', () => {
      document.body.classList.toggle('dark-mode');
      toggleBtn.textContent = document.body.classList.contains('dark-mode') ? '☀' : '🌙';
    });

    // Search & Filter Employees
    const searchInput = document.getElementById('searchInput');
    const deptFilter = document.getElementById('deptFilter');
    const employeeCards = document.querySelectorAll('.employee-card');

    function filterEmployees() {
      const searchVal = searchInput.value.toLowerCase();
      const deptVal = deptFilter.value;

      employeeCards.forEach(card => {
        const name = card.getAttribute('data-name').toLowerCase();
        const dept = card.getAttribute('data-dept');

        const matchName = name.includes(searchVal);
        const matchDept = deptVal === '' || dept === deptVal;

        card.style.display = (matchName && matchDept) ? 'block' : 'none';
      });
    }

    searchInput.addEventListener('input', filterEmployees);
    deptFilter.addEventListener('change', filterEmployees);
  </script>
</body>
</html>