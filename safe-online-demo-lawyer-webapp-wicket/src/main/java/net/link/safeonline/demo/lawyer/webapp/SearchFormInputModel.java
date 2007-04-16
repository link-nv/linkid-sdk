package net.link.safeonline.demo.lawyer.webapp;

import java.io.Serializable;

public final class SearchFormInputModel implements Serializable {

	private static final long serialVersionUID = 1L;

	String safeonlineId = null;

	public SearchFormInputModel() {
		// empty
	}

	public String getSafeonlineId() {
		return safeonlineId;
	}

	public void setSafeonlineId(String safeonlineId) {
		this.safeonlineId = safeonlineId;
	}

}
