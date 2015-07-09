<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<!DOCTYPE html>
<html>
<head>
<title>中華科技大學 -</title>

<style>
/* overwrite bootstrap form */
.form-signin {
	max-width: 300px;
	padding: 19px 29px 29px;
	background-color: #f8f8f8;
	border: 1px solid #e5e5e5;
	-webkit-border-radius: 5px;
	-moz-border-radius: 5px;
	border-radius: 5px;
	-webkit-box-shadow: 0 1px 2px rgba(0, 0, 0, .05);
	-moz-box-shadow: 0 1px 2px rgba(0, 0, 0, .05);
	box-shadow: 0 1px 2px rgba(0, 0, 0, .05);
}
</style>
<script src="inc/js/plugin/jquery.cookie.js"></script>
<script>
$(document).ready(function () {
    $("#remember").click(function () {
    	remember();
    });
  	/*
    $("#login").click(function () {
    	remember();
    });
  	*/
  	//記住帳號
    $('#remember').change(function(){
        this.checked ? 
        $.cookie("loginusername",$("#username").val(), {expires:999, path: "/"}) : 
        $.removeCookie('loginusername', { path:'/' });
    });
});


</script>
</head>
<body>
	<div class="content-page">
	<form action="Login" method="post" class="form-signin">
		<p>
		<ul class="nav nav-pills">
			<li class="active"><a href="#">資訊系統</a></li>
			<li><a href="www.cust.edu.tw">學校首頁</a></li>			
			<li><a href="www.cust.edu.tw">校園郵件</a></li>
		</ul>
		</p>
		<div class="input-prepend">
			<span class="add-on">帳號 </span>
			<input type="text" name="username" id="username" value="${cookie['loginusername'].value}" size="12" placeholder="學號或教職員身份證號">
		</div>
		<div class="input-prepend">
			<span class="add-on">密碼 </span> 
			<input type="password" size="12" name="password" placeholder="密碼">
		</div>
		<label class="checkbox">
		<input type="checkbox" <c:if test="${cookie['loginusername'].value!=null}">checked</c:if> id="remember" value="remember"> 儲存登入資訊..
		</label> <input type="submit" id="login" name="method:login" value="登入" class="btn btn-danger"/>
	</form>
	</div>
</html>