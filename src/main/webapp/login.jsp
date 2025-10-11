<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="ja">
	<head>
		<meta charset="UTF-8">
		<title>勤怠管理システム - ログイン</title>
		<link rel="stylesheet" href="${ pageContext.request.contextPath }/style/style.css">
	</head>
	<body>
		<div class="container">
			<h1>勤怠管理システム画面</h1>
			<form action="login" method="post">
				<p>
					<label for="userId">ユーザーID: </label>
					<input type="text" id="userId" name="userId" onblur="fetchUserName()" required />
					<script type="text/javascript">document.getElementById("userId").focus()</script>
					<script>
						function fetchUserName() {
						    const userId = document.getElementById("userId").value;
						    if (!userId) {
						        document.getElementById("userNameDisplay").innerText = "";
						        return;
						    }
						
						    fetch("getUserName?userId=" + encodeURIComponent(userId))
						        .then(response => response.text())
						        .then(name => {
						            if (name) {
						                document.getElementById("userNameDisplay").innerText =
						                    "このIDのユーザー名: " + name;
						            } else {
						                document.getElementById("userNameDisplay").innerText =
						                    "ユーザーが見つかりません";
						            }
						        })
						        .catch(error => console.error("Error:", error));
						}
					</script>
					<p id="userNameDisplay" style="color: blue;"></p>
				</p>
				<p>
					<label for="password">パスワード: </label>
					<input type="password" id="password" name="password" placeholder="password" required>
				</p>
				<div class="button-group">
					<input type="submit" value="ログイン">
				</div>
			</form>
			<p class="error-message"><c:out value="${errorMessage}" /></p>
			<c:if test="${not empty sessionScope.successMessage}">
				<p class="success-message"><c:out value="${sessionScope.successMessage}" /></p>
				<c:remove var="successMessage" scope="session" />
			</c:if>
		</div>
	</body>
</html>