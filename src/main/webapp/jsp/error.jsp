<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>

<!DOCTYPE html>
<html>
	<head>
		<meta charset="UTF-8">
		<title>エラー</title>
	</head>
	<body>
		<h1>処理中にエラーが発生しました</h1>
		<!-- note -->
		<p>エラーメッセージ: <%= exception.getMessage() %></p>
		<a href="../login.jsp">ログイン画面に戻る</a>
	</body>
</html>