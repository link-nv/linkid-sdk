package net.link.safeonline.sdk.api.ws.configuration;

import java.io.Serializable;
import java.util.List;
import org.jetbrains.annotations.Nullable;


/**
 * Created by wvdhaute
 * Date: 10/03/15
 * Time: 14:19
 */
public class ThemesDO implements Serializable {

    private final List<ThemeDO> themes;

    public ThemesDO(final List<ThemeDO> themes) {

        this.themes = themes;
    }

    // Helper methods

    @Nullable
    public ThemeDO findDefaultTheme() {

        for (ThemeDO theme : themes) {
            if (theme.isDefaultTheme())
                return theme;
        }
        return null;
    }

    @Override
    public String toString() {

        return "ThemesDO{" +
               "themes=" + themes +
               '}';
    }

    // Accessors

    public List<ThemeDO> getThemes() {

        return themes;
    }
}
