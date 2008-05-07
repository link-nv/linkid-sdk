package net.link.safeonline.demo.lawyer.webapp;

import net.link.safeonline.demo.wicket.tools.RoleSession;
import wicket.protocol.http.WebApplication;

public class LawyerSession extends RoleSession {

	private static final long serialVersionUID = 1L;

	private Lawyer lawyer;

	public LawyerSession(WebApplication application) {
		super(application);
	}

	public Lawyer getLawyer() {
		return this.lawyer;
	}

	public void setLawyer(Lawyer lawyer) {
		this.lawyer = lawyer;
	}

}
