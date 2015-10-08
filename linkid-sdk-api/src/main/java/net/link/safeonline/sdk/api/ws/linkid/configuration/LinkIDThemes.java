package net.link.safeonline.sdk.api.ws.linkid.configuration;

import java.io.Serializable;
import java.util.List;
import org.jetbrains.annotations.Nullable;


/**
 * Created by wvdhaute
 * Date: 10/03/15
 * Time: 14:19
 */
public class LinkIDThemes implements Serializable {

    private final List<LinkIDTheme> themes;

    public LinkIDThemes(final List<LinkIDTheme> themes) {

        this.themes = themes;
    }

    // Helper methods

    @Nullable
    public LinkIDTheme findDefaultTheme() {

        for (LinkIDTheme theme : themes) {
            if (theme.isDefaultTheme())
                return theme;
        }
        return null;
    }

    @Override
    public String toString() {

        return "LinkIDThemes{" +
               "themes=" + themes +
               '}';
    }

    // Accessors

    public List<LinkIDTheme> getThemes() {

        return themes;
    }
}
