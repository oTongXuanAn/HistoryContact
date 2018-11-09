package an.xuan.tong.historycontact.call.receiver;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.text.TextUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Utils {
    public static String getDefaultPath(Context context) {
        return String.format("%s%s",
                Utils.normalDir(Environment.getExternalStorageDirectory().getAbsolutePath()),
                "HistoryContact/");
    }

    private static String normalDir(String dir) {
        if (TextUtils.isEmpty(dir)) {
            return dir;
        }

        dir = dir.replace('\\', '/');
        if (!dir.substring(dir.length() - 1, dir.length()).equals("/")) {
            dir += "/";
        }
        return dir;
    }

    public static String makeFileName(String phone, Integer typeCall) {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("ddMMyyyyHHmmss");
        String sTypeCall = "";
        if (typeCall == ProcessingBase.TypeCall.INSTANCE.getINC()) {
            sTypeCall = "incoming";
        } else {
            sTypeCall = "outgoing";
        }
        String nameSubscr = sTypeCall;
        String phoneSubscr = phone;
        Date date = calendar.getTime();

        return String.format("%s%s%s", df.format(date), nameSubscr, phoneSubscr);
    }
}
