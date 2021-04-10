package de.waldorfaugsburg.lessoncontrol.client.service.general;

import de.waldorfaugsburg.lessoncontrol.client.service.AbstractService;
import de.waldorfaugsburg.lessoncontrol.client.util.WallpaperUtil;
import de.waldorfaugsburg.lessoncontrol.common.service.GeneralServiceConfiguration;

public final class GeneralService extends AbstractService<GeneralServiceConfiguration> {

    public GeneralService(final GeneralServiceConfiguration configuration) {
        super(configuration);
    }

    @Override
    public void enable() throws Exception {
        WallpaperUtil.setWallpaper(getConfiguration().getWallpaperPath());
    }

    @Override
    public void disable() throws Exception {

    }
}
