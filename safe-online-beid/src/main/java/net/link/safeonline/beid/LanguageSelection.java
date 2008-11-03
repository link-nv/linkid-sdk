package net.link.safeonline.beid;

import javax.ejb.Local;

import net.link.safeonline.SafeOnlineService;
import net.link.safeonline.ctrl.LanguageSelectionBase;


@Local
public interface LanguageSelection extends SafeOnlineService, LanguageSelectionBase {

}
