package an.xuan.tong.historycontact.call2

import android.app.Notification

/**
 * Created by Viktor Degtyarev on 16.10.17
 * E-mail: viktor@degtyarev.biz
 */
interface INotification<T> {
    fun build(): Notification
}