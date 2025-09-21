<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html>
	<head>
		<meta charset="UTF-8">
		<title>従業員メニュー</title>
		<link rel="stylesheet" href="${ pageContext.request.contextPath }/style/style.css">
	</head>
	<body>
		<div class="container">
			<h1>従業員メニュー</h1>
			<p>ようこそ、${ user.name }さん</p>
			<p>社員番号: ${ user.id }</p>
			
			<c:if test="${ not empty successMessage }">
				<p class="success-message"><c:out value="${ successMessage }" /></p>
			</c:if>
			
			<div class="button-group">
				<form action="attendance" method="post" style="display:inline;">
					<input type="hidden" name="action" value="checkIn">
					<input type="submit" value="出勤" <c:if test="${ sessionScope.hasActiveAttendance }">disabled</c:if> />
				</form>
				<form action="attendance" method="post" style="display:inline;">
					<input type="hidden" name="action" value="checkOut">
					<input type="submit" value="退勤" <c:if test="${ !sessionScope.hasActiveAttendance }">disabled</c:if> />
				</form>
			</div>
			
			<div>
				<p>${ totalHoursByUser }</p>
			</div>
			
			<h2>あなたの勤怠履歴</h2>
			<table>
			    <thead>
			        <tr>
			            <th>出勤時刻</th>
			            <th>退勤時刻</th>
			        </tr>
			    </thead>
			    <tbody>
			        <c:forEach var="att" items="${ attendanceRecords }">
			            <tr>
			                <td>${ att.checkInTime }</td>
			                <td>${ att.checkOutTime }</td>
			            </tr>
			        </c:forEach>
			        <c:if test="${ empty attendanceRecords }">
			            <tr><td colspan="2">勤怠記録がありません</td></tr>
			        </c:if>
			    </tbody>
			</table>
			
			<div class="pagination">
			    <c:forEach begin="1" end="${ totalPages }" var="i">
			        <c:choose>
			            <c:when test="${ i == currentPage }">
			                <span>[${ i }]</span>
			            </c:when>
			            <c:otherwise>
			                <a href="${pageContext.request.contextPath}/employeeMenu?page=${ i }">[${ i }]</a>
			            </c:otherwise>
			        </c:choose>
			    </c:forEach>
			</div>
			
			
			<div class="button-group">
				<a href="logout" class="button secondary">ログアウト</a>
			</div>
		</div>
	</body>
</html>