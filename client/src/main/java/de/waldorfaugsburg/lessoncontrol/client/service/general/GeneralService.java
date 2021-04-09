package de.waldorfaugsburg.lessoncontrol.client.service.general;

import de.waldorfaugsburg.lessoncontrol.client.service.AbstractService;
import de.waldorfaugsburg.lessoncontrol.client.util.WallpaperUtil;
import de.waldorfaugsburg.lessoncontrol.common.service.GeneralServiceConfiguration;

public final class GeneralService extends AbstractService<GeneralServiceConfiguration> {

    private final static String WALLPAPER_UPDATE_CMD = "C:\\FWA\\bginfo.exe info.bgi /all /nolicprompt";

    public GeneralService(final GeneralServiceConfiguration configuration) {
        super(configuration);
    }

    @Override
    public void enable() throws Exception {
        WallpaperUtil.setWallpaper(getConfiguration().getWallpaperPath());
        Runtime.getRuntime().exec(WALLPAPER_UPDATE_CMD);
    }

    @Override
    public void disable() throws Exception {

    }
}
