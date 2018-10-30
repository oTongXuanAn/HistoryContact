package an.xuan.tong.historycontact.libCall;

public class CallRecorder2 {
    public static native int load();
    public static native int startFix(int i2);
    public static native int stopFix();
    static {
        LibLoader.loadLib();
    }
}
