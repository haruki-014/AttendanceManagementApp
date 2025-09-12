<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html>
	<head>
		<meta charset="UTF-8">
		<title>ユーザー管理画面</title>
		<link rel="stylesheet" href="${ pageContext.request.contextPath }/style/style.css">
		<%-- <link rel="stylesheet" href="${ pageContext.request.contextPath }/style/user_management.css"> --%>
	</head>
	<body>
		<div class="container">
			<h1>ユーザー管理画面</h1>
			<p>ようこそ、${ user.name }さん</p>
			
			<div class="main-nav">
				<a href="${pageContext.request.contextPath}/attendance?action=filter">勤怠履歴管理</a>
				<a href="${pageContext.request.contextPath}/users?action=list">ユーザー管理</a>
				<a href="${pageContext.request.contextPath}/logout">ログアウト</a>
			</div>
			
			<c:if test="${ not empty successMessage }">
				<p class="success-message"><c:out value="${ successMessage }" /></p>
			</c:if>
			
			<h2>ユーザー追加・編集</h2>
			<form action="${pageContext.request.contextPath}/users" method="post" class="user-form">
				<input type="hidden" name="action" value="${ userToEdit != null ? 'update' : 'add' }">
				
				<c:if test="${ userToEdit != null }">
    				<input type="hidden" name="id" value="${ userToEdit.id }">
				</c:if>
				
				<c:if test="${ userToEdit != null }">
					<input type="hidden" name="name" value="${ userToEdit.name }">
				</c:if>
				
				<label for="name">ユーザーID:</label>
				<input type="text" id="name" name="name"
					value="<c:out value='${ userToEdit.name }'/>"
					<c:if test="${ userToEdit != null }">readonly</c:if>
					required>
				
				<label for="password">パスワード:</label>
				<input type="password" id="password" name="password" <c:if test="${ userToEdit == null }">required</c:if>>
				
				<label for="role">役割:</label>
				<select id="role" name="role" required>
					<option value="employee" <c:if test="${ userToEdit.role == 'employee' }">selected</c:if>>従業員</option>
					<option value="admin" <c:if test="${ userToEdit.role == 'admin' }">selected</c:if>>管理者</option>
				</select>
				
				<p>
					<label for="enabled">アカウント有効:</label>
					<input type="checkbox" id="enabled" name="enabled" value="true"
						<c:if test="${ userToEdit == null || userToEdit.enabled }">checked</c:if>
					>
				</p>
				
				<c:choose>
    				<c:when test="${ userToEdit != null }">
        				<c:set var="submitLabel" value="更新" />
    				</c:when>
    				<c:otherwise>
        				<c:set var="submitLabel" value="追加" />
    				</c:otherwise>
				</c:choose>
				
				<div class="button-group">
					<input type="submit" value="${ submitLabel }">
				</div>
			</form>
			
			
			<c:if test="${ userToEdit != null }">
				<form action="${pageContext.request.contextPath}/users" method="post" style="displsy:inline;">
					<input type="hidden" name="action" value="reset_password">
					<input type="hidden" name="name" value="${ userToEdit.name }">
					<input type="hidden" name="newPassword" value="password">
					<input type="submit" value="パスワードをリセット" class="button secondary"
						onclick="return confirm('本当にパスワードをリセットしますか？(デフォルトパスワード: passwprd)');">
				</form>
			</c:if>
			
			
			<p class="error-message"><c:out value="${ errorMessage }" /></p>
			
			<h2>既存ユーザー</h2>
			<table>
				<thead>
					<tr>
						<th>ユーザーID</th>
						<th>役割</th>
						<th>有効</th>
						<th>操作</th>
					</tr>
				</thead>
				<tbody>
					<c:forEach var="u" items="${ users }">
						<tr>
							<td>${ u.name }</td>
							<td>${ u.role }</td>
							<td>
								<c:choose>
									<c:when test="${ u.enabled }">
										<c:set var="submitValue" value="無効化" />
										<c:set var="submitClass" value="danger" />
										<c:set var="submitOnClick" value="無効" />
									</c:when>
									<c:otherwise>
										<c:set var="submitValue" value="有効化" />
										<c:set var="submitClass" value="secondary" />
										<c:set var="submitOnClick" value="有効" />
									</c:otherwise>
								</c:choose>
								<form action="${pageContext.request.contextPath}/users" method="post" class="existing-users">
									<input type="hidden" name="action" value="toggle_enabled">
									<input type="hidden" name="name" value="${ u.name }">
									<input  type="hidden" name="enabled" value="${ u.enabled }">
									<input type="submit" value="${ submitValue }"
										class="button ${ submitClass }"
										onclick="return confirm('本当にこのユーザーを${ submitOnClick }にしますか？');">
								</form>
							</td>
							<td class="table-actions">
								<a href="users?action=edit&name=${ u.name }" class="button">編集</a>
								<form action="users" method="post" style="display:inline;">
									<input type="hidden" name="action" value="delete">
									<input type="hidden" name="name" value="${ u.name }">
									<input type="submit" value="削除" class="button danger"
											onclick="return confirm('本当にこのユーザーを削除しますか？');">
								</form>
							</td>
						</tr>
					</c:forEach>
					<c:if test="${ empty users }">
						<tr><td colspan="4">ユーザーがいません</td></tr>
					</c:if>
				</tbody>
			</table>
		</div>
	</body>
</html>

