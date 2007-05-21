package net.link.safeonline.service;

import javax.annotation.Resource;
import javax.ejb.EJBContext;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.common.SafeOnlineRoles;
import net.link.safeonline.entity.ApplicationEntity;

public class ApplicationOwnerAccessControlInterceptor {

	@Resource
	EJBContext ctx;

	@AroundInvoke
	public Object applicationAccessControl(InvocationContext invocation)
			throws Exception {
		ApplicationEntity application = null;

		for (Object parameter : invocation.getParameters()) {
			if (parameter != null
					&& parameter.getClass().equals(ApplicationEntity.class)) {
				application = (ApplicationEntity) parameter;
				break;
			}
		}

		boolean returnValue = false;
		boolean isOperator = ctx.isCallerInRole(SafeOnlineRoles.OPERATOR_ROLE);
		if (isOperator) {
			returnValue = true;
		}
		if (application == null) {
			returnValue = false;
		}
		String subjectName = ctx.getCallerPrincipal().getName();
		if (application != null
				&& application.getApplicationOwner().getAdmin().getLogin()
						.equals(subjectName)) {
			returnValue = true;
		}
		if (returnValue == false) {
			throw new PermissionDeniedException();
		}
		return invocation.proceed();
	}

}
