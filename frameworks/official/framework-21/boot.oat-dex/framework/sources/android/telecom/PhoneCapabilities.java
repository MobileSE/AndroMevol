package android.telecom;

public final class PhoneCapabilities {
    public static final int ADD_CALL = 16;
    public static final int ALL = 12543;
    public static final int DISCONNECT_FROM_CONFERENCE = 8192;
    public static final int HOLD = 1;
    public static final int MANAGE_CONFERENCE = 128;
    public static final int MERGE_CONFERENCE = 4;
    public static final int MUTE = 64;
    public static final int RESPOND_VIA_TEXT = 32;
    public static final int SEPARATE_FROM_CONFERENCE = 4096;
    public static final int SUPPORTS_VT_LOCAL = 256;
    public static final int SUPPORTS_VT_REMOTE = 512;
    public static final int SUPPORT_HOLD = 2;
    public static final int SWAP_CONFERENCE = 8;
    public static final int VoLTE = 1024;
    public static final int VoWIFI = 2048;

    public static String toString(int capabilities) {
        StringBuilder builder = new StringBuilder();
        builder.append("[Capabilities:");
        if ((capabilities & 1) != 0) {
            builder.append(" HOLD");
        }
        if ((capabilities & 2) != 0) {
            builder.append(" SUPPORT_HOLD");
        }
        if ((capabilities & 4) != 0) {
            builder.append(" MERGE_CONFERENCE");
        }
        if ((capabilities & 8) != 0) {
            builder.append(" SWAP_CONFERENCE");
        }
        if ((capabilities & 16) != 0) {
            builder.append(" ADD_CALL");
        }
        if ((capabilities & 32) != 0) {
            builder.append(" RESPOND_VIA_TEXT");
        }
        if ((capabilities & 64) != 0) {
            builder.append(" MUTE");
        }
        if ((capabilities & 128) != 0) {
            builder.append(" MANAGE_CONFERENCE");
        }
        if ((capabilities & 256) != 0) {
            builder.append(" SUPPORTS_VT_LOCAL");
        }
        if ((capabilities & 512) != 0) {
            builder.append(" SUPPORTS_VT_REMOTE");
        }
        if ((capabilities & 1024) != 0) {
            builder.append(" VoLTE");
        }
        if ((capabilities & 2048) != 0) {
            builder.append(" VoWIFI");
        }
        builder.append("]");
        return builder.toString();
    }

    private PhoneCapabilities() {
    }
}
