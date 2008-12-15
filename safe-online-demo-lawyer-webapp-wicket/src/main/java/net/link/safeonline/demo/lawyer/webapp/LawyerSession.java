package net.link.safeonline.demo.lawyer.webapp;

import net.link.safeonline.wicket.tools.RoleSession;

import org.apache.wicket.Request;

public class LawyerSession extends RoleSession {

	private static final long serialVersionUID = 1L;

	private Lawyer lawyer;

	public LawyerSession(Request request) {

        super(request);
	}

	public Lawyer getLawyer() {
		return this.lawyer;
	}

	public void setLawyer(Lawyer lawyer) {
		this.lawyer = lawyer;
	}

}
