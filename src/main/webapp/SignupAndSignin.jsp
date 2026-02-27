<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>Sign In / Sign Up</title>
  <!-- Bootstrap 5 CSS -->
  <link
    href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
    rel="stylesheet"
  />
  <style>
    body {
      background: linear-gradient(135deg, #74b9ff, #a29bfe);
      height: 100vh;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .auth-card {
      width: 100%;
      max-width: 400px;
      background: #fff;
      border-radius: 1rem;
      box-shadow: 0 0 20px rgba(0, 0, 0, 0.1);
      padding: 2rem;
      transition: all 0.4s ease-in-out;
    }

    .auth-card h3 {
      font-weight: 700;
      text-align: center;
      margin-bottom: 1.5rem;
    }

    .toggle-link {
      color: #0984e3;
      cursor: pointer;
      text-decoration: none;
    }

    .toggle-link:hover {
      text-decoration: underline;
    }

    /* Hide inactive form */
    #signupForm {
      display: none;
    }

    @media (max-width: 576px) {
      .auth-card {
        padding: 1.5rem;
      }
    }
  </style>
</head>
<body>

<%
String msg = (String)session.getAttribute("msg");
if(msg == null) msg = "";
session.removeAttribute("msg");
%>


  <div class="auth-card">
    <!-- Sign In Form -->
    <div id="signinForm">
      <h3>Sign In</h3>
      <p class=text-center><%=msg %></p>
      <form action="signin" method="post">
        <div class="mb-3">
          <label for="signinEmail" class="form-label">Username</label>
          <input name="username" type="text" class="form-control" id="signinUsernamel" placeholder="Enter username" required>
        </div>
        <div class="mb-3">
          <label for="signinPassword" class="form-label">Password</label>
          <input name="password" type="password" class="form-control" id="signinPassword" placeholder="Password" required>
        </div>
        <button type="submit" class="btn btn-primary w-100">Sign In</button>
        <p class="text-center mt-3 mb-0">
          Don't have an account?
          <a class="toggle-link" id="showSignup">Sign Up</a>
        </p>
      </form>
    </div>

    <!-- Sign Up Form -->
    <div id="signupForm">
      <h3>Sign Up</h3>
      <p class=text-center><%=msg %></p>
      <form action="signup" method="post">
        <div class="mb-3">
          <label for="signupName" class="form-label">Username</label>
          <input name="username" type="text" class="form-control" id="signupName" placeholder="Enter name" required>
        </div>
        <div class="mb-3">
          <label for="signupEmail" class="form-label">Email address</label>
          <input name="email" type="email" class="form-control" id="signupEmail" placeholder="Enter email" required>
        </div>
        <div class="mb-3">
          <label for="signupPassword" class="form-label">Password</label>
          <input name="password" type="password" class="form-control" id="signupPassword" placeholder="Password" required>
        </div>
        <button type="submit" class="btn btn-success w-100">Create Account</button>
        <p class="text-center mt-3 mb-0">
          Already have an account?
          <a class="toggle-link" id="showSignin">Sign In</a>
        </p>
      </form>
    </div>
  </div>

  <!-- Bootstrap JS -->
  <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>

  <!-- Toggle Script -->
  <script>
    const signinForm = document.getElementById('signinForm');
    const signupForm = document.getElementById('signupForm');
    const showSignup = document.getElementById('showSignup');
    const showSignin = document.getElementById('showSignin');

    showSignup.addEventListener('click', () => {
      signinForm.style.display = 'none';
      signupForm.style.display = 'block';
    });

    showSignin.addEventListener('click', () => {
      signupForm.style.display = 'none';
      signinForm.style.display = 'block';
    });
  </script>

</body>
</html>