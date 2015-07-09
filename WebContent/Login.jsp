<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ include file="/taglibs.jsp"%>
<!DOCTYPE HTML>
<html>
	<head>
		<meta http-equiv="x-ua-compatible" content="ie=8" />
		<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />		
		<title><bean:message key='websitename'/></title>
		
		<link rel="stylesheet" href="style/global.css">	
		<link rel="stylesheet" href="style/style.css">		
		<script src="include/jquery/js/jquery-1.5.1.js"></script>
		<script src="include/jquery/js/jquery-ui-1.8.13.custom.min.js"></script>
		<script src="include/jquery/js/plugin/jquery.blockUI.js"></script>
		<script src="include/jquery/uniform/jquery.uniform.js"></script>	    
	    <link rel="stylesheet" href="include/jquery/uniform/css/uniform.default.css">
		<link rel="stylesheet" href="include/jquery/uniform/css/uniform.aristo.css">
		<link rel="Stylesheet" href="include/jquery/css/redmond/jquery-ui-1.8.13.custom.css"/>
		
		<script>
	      $(function(){
	        $("input, textarea, select, button").uniform();
	      });
	    </script>
	    
	</head>

	<body>
	<div class="headerBody">
		
	</div>
	
	<table width="100%" height="50" border="0" cellpadding="0" cellspacing="0" class="lightBorderBottom">
		<tr>
			<td bgcolor="#edeff4"></td>
		</tr>
	</table>
	
	<table width="100%" height="300">
		<tr>
			<td width="50%"></td>
			<td valign="middle">
			<%@ include file="include/msg.jsp"%>			
			<html:form action="/Login.jspx" method="post" onsubmit="$.blockUI({theme:true, title:'登入', message:'執行中，請稍後'});">
			<table class="logininBody">		
				<tr>
					<td align="right">登入帳號</td>
					<td><input type="text" name="f1" id="username" class="text" size="36" value="${username}"/></td>
				</tr>
				
				<tr>
					<td align="right">密碼</td>
					<td><input type="password" name="f2" class="text" size="36"/></td>
				</tr>
				
				<tr>
					<td></td>
					<td>
					<input name="f0" id="remember" onClick="remberme()" <c:if test="${username!=null}">checked</c:if> type="checkbox" />記住帳號
					</td>
				</tr>
				<tr>
					<td></td>
					<td>
					
					
					<input type="submit" class="blueButton" id="login" value="登入" onClick="remberme()">
					
					
					
					
					
					<!--  input type="button" class="blueButton" value="登入問題" onClick="$.blockUI({theme:true, title:'登入問題', 
					message:'本系統之帳號密碼均與資訊系統同步，請使用相同帳號密碼即可。如果從未使用過資訊系統，學生身份的帳號為學號, 密碼預設為身分證字號；教師身份的帳號為身分證字號, 密碼預設為生日6碼。',timeout: 5000});
					$('.blockOverlay').attr('title','點選空白處關閉說明').click($.unblockUI); 
					"-->
					</td>
				</tr>
								
			</table>
			</html:form>
			
			</td>
			<td width="50%"></td>
		</tr>
	</table>
	</body>
</html>

<script>
function remberme(){
	if(document.getElementById("remember").checked){
		setCookie("acisuser", document.getElementById("username").value);
	}else{
		delCookie("acisuser");
	}	
}

function setCookie(name,value){
  var Days = 365;
  var exp  = new Date();    //new Date("December 31, 9998");
  exp.setTime(exp.getTime() + Days*24*60*60*1000);
  document.cookie=name+"="+escape(value)+";expires="+exp.toGMTString();
}

function getCookie(name){
  var arr = document.cookie.match(new RegExp("(^| )"+name+"=([^;]*)(;|$)"));
  if(arr != null) return unescape(arr[2]); return null;
}

function delCookie(name){
  var exp = new Date();
  exp.setTime(exp.getTime() - 1);
  var cval=getCookie(name);
  if(cval!=null) document.cookie=name +"="+cval+";expires="+exp.toGMTString();
}
</script>