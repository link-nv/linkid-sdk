package net.link.safeonline.demo.cinema.webapp;

import net.link.safeonline.demo.wicket.tools.RoleSession;

import org.apache.wicket.Request;

public class CinemaSession extends RoleSession {

	private static final long serialVersionUID = 1L;

	private Cinema cinema;

	public CinemaSession(Request request) {

        super(request);
	}

	public Cinema getCinema() {
		return this.cinema;
	}

	public void setCinema(Cinema cinema) {
		this.cinema = cinema;
	}

}
