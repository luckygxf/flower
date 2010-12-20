<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.sql.*"%>
<%@ page import="java.util.List"%>
<%@ page import="flower.util.DatabaseWorker"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
</head>
<body>
    <table align=center>
    	<tr>
    		<td><a href="./chart?conType=2">边界接口流量</a></td>
    		<td><a href="./chart?conType=1">内部主干流量</a></td>
    		<td><a href="./chart?conType=0">内部子网流量</a></td>
    		<td><a href="./chart?conType=-1">内部其它流量</a></td>
    	</tr>
    </table>
	<% 
	DatabaseWorker.connect();
	List<Object[]> routerList = DatabaseWorker.query("SELECT Router_ID,Router_IP,Router_Descr FROM Routers");
	for (Object[] router : routerList) {
	%> 
		<table align=center>
			<tr><td align=center><b>第<%=router[0]%>号路由器：<%=router[1]%></b></td></tr>
			<tr><td align=center><%=router[2]%></td></tr>
		</table>
		<table WIDTH=100% BORDER=1 CELLSPACING=1 CELLPADDING=1 align=center>
			<tr>
				<td><b>接口ID</b></td>
				<td><b>接口MAC地址</b></td>
				<td><b>接口网卡速率</b></td>
				<td><b>接口IP地址</b></td>
				<td><b>接口子网掩码</b></td>
				<td><b>接口类型</b></td>
				<td><b>邻接接口</b></td>
			</tr>
			<%
			List<Object[]> infList = DatabaseWorker.query("SELECT Interface_ID,Interface_MAC,Interface_Speed,Interface_IP,Interface_Mask,Interface_ConType,Interface_Link FROM Interfaces WHERE Interface_Router='"+router[0]+"' ORDER BY Interface_Index ASC");
			for (Object[] inf : infList) {
			%>
			<tr>
				<td><a href=<%="./chart?ifID="+inf[0]%>><%=inf[0]%></a></td>
				<td><%=inf[1]%></td>
				<td><%=inf[2]%></td>
				<td><%=inf[3]%></td>
				<td><%=inf[4]%></td>
				<td><%=inf[5]%></td>
				<td><%=inf[6]%></td>
			</tr>
			<%} // 结束接口循环
			%>
		</table>
		<br>
	<%} // 结束路由器循环
	DatabaseWorker.release();	
	%>
</body>
</html>