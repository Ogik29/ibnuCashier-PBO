<%-- 
    Document   : index
    Created on : 14 Dec 2025, 15.20.02
    Author     : dimas
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<html>
<head>
    <title>IBNU Cashier Login</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-dark d-flex justify-content-center align-items-center vh-100">
    <div class="card p-4" style="width: 300px">
        <h4 class="text-center">Login IBNU</h4>
        <form action="auth" method="post">
            <input type="hidden" name="action" value="login">
            <div class="mb-2">Username: <input type="text" name="username" class="form-control" required></div>
            <div class="mb-3">Password: <input type="password" name="password" class="form-control" required></div>
            <button class="btn btn-primary w-100">Login</button>
        </form>
        <% if(request.getParameter("err")!=null) out.print("<small class='text-danger'>User/Pass Salah!</small>"); %>
    </div>
</body>
</html>
