package de.waldorfaugsburg.lessoncontrol.client.service.voicemeeter;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

import java.io.File;

public final class Voicemeeter {

    public static final String WORKING_DIRECTORY = "C:\\Program Files (x86)\\VB\\Voicemeeter\\";
    public static final String EXECUTABLE_PATH = WORKING_DIRECTORY + "voicemeeter.exe";
    public static final String DLL_PATH = "C:\\Program Files (x86)\\VB\\Voicemeeter\\VoicemeeterRemote64.dll";

    private static VoicemeeterInstance instance;
    private static boolean initialized;

    public static void init() {
        if (initialized) return;

        System.load(DLL_PATH);
        instance = Native.load("VoicemeeterRemote64", VoicemeeterInstance.class);

        final int loginResponse = instance.VBVMR_Login();
        if (loginResponse < 0) throw new VoicemeeterException("Invalid login response: " + loginResponse);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> instance.VBVMR_Logout()));
        initialized = true;
    }

    public static void setParameterFloat(final String name, final float value) {
        final Pointer pointer = getStringPointer(name);
        final int response = instance.VBVMR_SetParameterFloat(pointer, value);
        if (response != 0) throw new VoicemeeterException("Invalid response: " + response);
    }

    public static void setParameterString(final String name, final String value) {
        final Pointer namePointer = getStringPointer(name);
        final Pointer valuePointer = getStringPointer(value);
        final int response = instance.VBVMR_SetParameterStringA(namePointer, valuePointer);
        if (response != 0) throw new VoicemeeterException("Invalid response: " + response);
    }

    public static float getLevel(final int type, final int channel) {
        final Pointer levelPointer = getPointer(4);
        final int response = instance.VBVMR_GetLevel(type, channel, levelPointer);
        if (response != 0) throw new VoicemeeterException("Invalid response: " + response);

        final float value = levelPointer.getFloat(0);
        return (float) (20 * Math.log10(value) + 60);
    }

    public static int getNumberOfInputDevices() {
        return instance.VBVMR_Input_GetDeviceNumber();
    }

    public static int getNumberOfOutputDevices() {
        return instance.VBVMR_Output_GetDeviceNumber();
    }

    private static Pointer getStringPointer(final String string) {
        final int size = string.getBytes().length + 1;
        final Memory memory = new Memory(size);
        memory.setString(0, string);
        return memory;
    }

    private static Pointer getPointer(final int size) {
        return new Memory(size);
    }
}
