<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ taglib prefix="c"		uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt"		uri="http://java.sun.com/jsp/jstl/fmt"%>
 
<body style="background:#eee">
	<div class="login-box visible widget-box no-border" style="position:absolute;top:40%;left:50%;transform:translate(-50%,-50%); max-width:375px; height:auto; padding:1px; background-color: #AAA!important; box-shadow: 1px 1px 3px #AAA;">
		<div class="widget-body no-border">
			<div class="widget-main" style="padding-bottom:10px">
				<div class="row">
					<div class="col-sm-12">
						<form id="loginForm" action="<c:url value='/j_security_check'/>" method="post" data-session="valid">
							<input type="hidden" name="backurl" value="${backurl }"/>
							<fieldset>
								<label style="width:100%;">
									<span class="block input-icon input-icon-right">
										<input type="text" class="form-control" style="text-transform: uppercase !important;" placeholder="<fmt:message key='labels.usuario'/>" name="j_username" value="<c:out value="${param.j_username}" escapeXml="true"/>"/>
										<i class="ace-icon fa fa-user" style="margin-top:1%"></i>
									</span>
								</label>
	
								<label style="width:100%;">
									<span class="block input-icon input-icon-right">
										<input type="password" class="form-control" placeholder="<fmt:message key='labels.senha'/>" name="j_password" />
										<i class="ace-icon fa fa-lock" style="margin-top:1%"></i>
									</span>
								</label>
							
								<c:if test="${not empty param.j_username and not empty param.backurl and not empty param.j_password}">
									<div class="alert alert-danger" id="div-msginvalida">
										Erro ao se conectar, login ou senha inválida
									</div>
								</c:if>

								<button type="submit" class="col-xs-12 btn btn-sm btn-primary" style="border-radius:4px;">
									<i class="ace-icon fa fa-sign-in"></i> <fmt:message key="labels.acessar"/>
								</button>
							</fieldset>
						</form>
					</div>
				</div>
			</div>
		</div>
	</div>
</body>

