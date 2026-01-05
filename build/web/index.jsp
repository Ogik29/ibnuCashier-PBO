<%-- 
    Document   : index
    Created on : 14 Dec 2025, 15.20.02
    Author     : dimas
--%>

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <title>IBNU Cashier - Login</title>
    <!-- Simple CSS for cleanliness -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light d-flex justify-content-center align-items-center" style="height:100vh;">
    <div class="card p-4 shadow" style="width: 350px;">
        <h3 class="text-center text-primary mb-3">IBNU Cashier</h3>
        <% if(request.getParameter("error") != null) { %>
            <div class="alert alert-danger">Username/Password Salah</div>
        <% } %>
        <form action="auth" method="post">
            <div class="mb-3">
                <label>Username</label>
                <input type="text" name="username" class="form-control" required>
            </div>
            <div class="mb-3">
                <label>Password</label>
                <input type="password" name="password" class="form-control" required>
            </div>
            <button type="submit" class="btn btn-primary w-100">LOGIN</button>
        </form>
    </div>
</body>
</html>
