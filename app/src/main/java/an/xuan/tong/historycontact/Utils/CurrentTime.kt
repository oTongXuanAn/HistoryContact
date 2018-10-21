package an.xuan.tong.historycontact.Utils

import java.text.DateFormat
import java.util.*
import java.text.SimpleDateFormat
import android.R.attr.timeZone
import android.annotation.SuppressLint
import java.time.LocalDate
import java.util.concurrent.TimeUnit


public class CurrentTime {
    companion object {
        fun getLocalTime(): Long {
            val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"), Locale.getDefault())
            val currentLocalTime = calendar.time
            return currentLocalTime.time / 1000 + 7 * 60 * 60
        }

        fun timeOffset(): Long {
            return 7 * 60 * 60
        }
    }
}