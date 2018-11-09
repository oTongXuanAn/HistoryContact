package an.xuan.tong.historycontact.call.receiver

import android.app.Notification


interface INotification<T> {
    fun build(): Notification
}