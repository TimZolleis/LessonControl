package de.waldorfaugsburg.lessoncontrol.client.service.voicemeeter;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import lombok.Data;

import java.io.File;

public final class Voicemeeter {

    private final VoicemeeterInstance instance;

    public Voicemeeter(final VoicemeeterService service) {
        final File file = new File(service.getConfiguration().getDllPath());
        if (!file.exists()) throw new IllegalStateException("DLL is missing");

        System.load(file.getAbsolutePath());
        instance = Native.load("VoicemeeterRemote64", VoicemeeterInstance.class);
    }

    public int runVoicemeeter() {
        return instance.VBVMR_RunVoicemeeter(3);
    }

    public int login() throws VoicemeeterException {
        return instance.VBVMR_Login();
    }

    public int logout() {
        return instance.VBVMR_Logout();
    }

    public int getVoicemeeterType() {
        final Pointer type = getPointer(4);
        final int val = instance.VBVMR_GetVoicemeeterType(type);
        if (val == 0) {
            return type.getInt(0);
        }
        return val;
    }

    public int getVoicemeeterVersion() {
        final Pointer version = getPointer(4);
        final int val = instance.VBVMR_GetVoicemeeterVersion(version);
        if (val == 0) {
            return version.getInt(0);
        }
        return val;
    }

    public int areParametersDirty() {
        return instance.VBVMR_IsParametersDirty();
    }

    public boolean getParameterBoolean(final String parameterName) {
        return getParameterFloat(parameterName) == 1;
    }

    public float getParameterFloat(final String parameterName) {
        final Pointer paramName = getStringPointer(parameterName);
        final Pointer paramValue = getPointer(4);
        final int val = instance.VBVMR_GetParameterFloat(paramName, paramValue);

        switch (val) {
            case 0:
                return paramValue.getFloat(0);
            case -1:
                throw new VoicemeeterException("An error has occurred");
            case -2:
                throw new VoicemeeterException("Unable to get the Voicemeeter server");
            case -3:
                throw new VoicemeeterException("Unknown parameter name");
            case -5:
                throw new VoicemeeterException("Structure mismatch");
            default:
                throw new VoicemeeterException("Unexpected function return value. Function returned " + val);
        }
    }

    public String getParameterString(final String parameterName) {
        final Pointer paramName = getStringPointer(parameterName);
        final Pointer paramValue = getPointer(8);
        final int val = instance.VBVMR_GetParameterStringA(paramName, paramValue);

        switch (val) {
            case 0:
                return paramValue.getString(0);
            case -1:
                throw new VoicemeeterException("An error has occurred");
            case -2:
                throw new VoicemeeterException("Unable to get the Voicemeeter server");
            case -3:
                throw new VoicemeeterException("Unknown parameter name");
            case -5:
                throw new VoicemeeterException("Structure mismatch");
            default:
                throw new VoicemeeterException("Unexpected function return value. Function returned " + val);
        }
    }

    public float getLevel(final int type, final int channel) {
        final Pointer levelValue = getPointer(4);
        final int val = instance.VBVMR_GetLevel(type, channel, levelValue);

        switch (val) {
            case 0:
                return levelValue.getFloat(0);
            case -1:
                throw new VoicemeeterException("An error has occurred");
            case -2:
                throw new VoicemeeterException("Unable to get the Voicemeeter server");
            case -3:
                throw new VoicemeeterException("No level value available");
            case -4:
                throw new VoicemeeterException("The type of the channel is outside of the allowed range");
            default:
                throw new VoicemeeterException("Unexpected function return value. Function returned " + val);
        }
    }

    public void setParameterFloat(final String parameterName, final float value) {
        final Pointer paramName = getStringPointer(parameterName);
        final int val = instance.VBVMR_SetParameterFloat(paramName, value);

        switch (val) {
            case 0:
                break;
            case -1:
                throw new VoicemeeterException("An error has occurred");
            case -2:
                throw new VoicemeeterException("Unable to get the Voicemeeter server");
            case -3:
                throw new VoicemeeterException("Unknown parameter name");
            default:
                throw new VoicemeeterException("Unexpected function return value. Function returned " + val);
        }
    }

    public void setParameterString(String parameterName, String value) {
        final Pointer paramName = getStringPointer(parameterName);
        final Pointer paramValue = getStringPointer(value);
        final int val = instance.VBVMR_SetParameterStringA(paramName, paramValue);

        switch (val) {
            case 0:
                break;
            case -1:
                throw new VoicemeeterException("An error has occurred");
            case -2:
                throw new VoicemeeterException("Unable to get the Voicemeeter server");
            case -3:
                throw new VoicemeeterException("Unknown parameter name");
            default:
                throw new VoicemeeterException("Unexpected function return value. Function returned " + val);
        }
    }

    public void setParameters(final String script) {
        final Pointer stringPointer = getStringPointer(script);
        final int val = instance.VBVMR_SetParameters(stringPointer);

        switch (val) {
            case 0:
                break;
            case -1:
            case -3:
            case -4:
                throw new VoicemeeterException("An error has occurred");
            case -2:
                throw new VoicemeeterException("Unable to get the Voicemeeter server");
            default:
                if (val > 0)
                    throw new VoicemeeterException("Script error on line " + val);
                else
                    throw new VoicemeeterException("Unexpected function return value. Function returned " + val);
        }
    }

    public int getNumberOfAudioDevices(final boolean areInputDevices) {
        if (areInputDevices) {
            return instance.VBVMR_Input_GetDeviceNumber();
        } else {
            return instance.VBVMR_Output_GetDeviceNumber();
        }
    }

    public DeviceDescription getAudioDeviceDescriptionA(final int index, final boolean isInputDevice) {
        final Pointer type = getPointer(512);
        final Pointer name = getPointer(512);
        final Pointer hardwareId = getPointer(512);

        final int val;
        if (isInputDevice) {
            val = instance.VBVMR_Input_GetDeviceDescA(index, type, name, hardwareId);
        } else {
            val = instance.VBVMR_Output_GetDeviceDescA(index, type, name, hardwareId);
        }

        if (val != 0)
            throw new VoicemeeterException("Unexpected function return value. Function returned " + val);

        final DeviceDescription desc = new DeviceDescription();
        desc.setType(type.getInt(0));
        desc.setName(name.getString(0));
        desc.setHardwareId(hardwareId.getString(0));
        return desc;
    }

    private Pointer getStringPointer(final String string) {
        final int size = string.getBytes().length + 1;
        final Memory m = new Memory(size);
        m.setString(0, string);
        return m;
    }

    private Pointer getPointer(final int size) {
        return new Memory(size);
    }

    @Data
    public static class DeviceDescription {
        private int type;
        private String name;
        private String hardwareId;
    }
}
