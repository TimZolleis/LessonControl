package de.waldorfaugsburg.lessoncontrol.client.util;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIFunctionMapper;
import com.sun.jna.win32.W32APITypeMapper;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public final class WallpaperUtil {

    private static final long SET_WALLPAPER = 20;
    private static final long UPDATE_INI_FILE = 0x01;
    private static final long SEND_WIN_INI_CHANGE = 0x02;

    private static final SystemParameterInfoLibrary LIBRARY;

    static {
        final Map<String, Object> map = new HashMap<>();
        map.put(Library.OPTION_TYPE_MAPPER, W32APITypeMapper.UNICODE);
        map.put(Library.OPTION_FUNCTION_MAPPER, W32APIFunctionMapper.UNICODE);
        LIBRARY = Native.load("user32", SystemParameterInfoLibrary.class, map);
    }

    public static void setWallpaper(final String path) {
        final File file = new File(path);
        if (!file.exists()) throw new IllegalStateException("Wallpaper is missing");

        LIBRARY.SystemParametersInfo(new WinDef.UINT_PTR(SET_WALLPAPER), new WinDef.UINT_PTR(0),
                file.getAbsolutePath(), new WinDef.UINT_PTR(UPDATE_INI_FILE | SEND_WIN_INI_CHANGE));
    }

    public interface SystemParameterInfoLibrary extends StdCallLibrary {
        boolean SystemParametersInfo(WinDef.UINT_PTR uiAction, WinDef.UINT_PTR uiParam, String pvParam, WinDef.UINT_PTR fWinIni);
    }
}
