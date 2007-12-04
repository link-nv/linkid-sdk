package net.link.safeonline.ctrl;

import javax.faces.event.ActionEvent;

public interface LanguageSelectionBase {

	String entry();
	
	String goBack();
	
	void selectLanguage(ActionEvent event);
	
}
