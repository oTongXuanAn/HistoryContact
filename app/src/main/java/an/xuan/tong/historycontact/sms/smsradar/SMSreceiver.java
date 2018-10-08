package an.xuan.tong.historycontact.sms.smsradar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

public class SMSreceiver extends BroadcastReceiver {
    private final String TAG = this.getClass().getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED"))
            Log.e(TAG + "antx", "SMSreceiver onReceive SMS_RECEIVED");
        Bundle extras = intent.getExtras();
        String strMessage = "";
        if (extras != null) {
            Object[] smsextras = (Object[]) extras.get("pdus");
            for (int i = 0; i < smsextras.length; i++) {
                SmsMessage smsmsg = SmsMessage.createFromPdu((byte[]) smsextras[i]);

                String strMsgBody = smsmsg.getMessageBody().toString();
                String strMsgSrc = smsmsg.getOriginatingAddress();
                strMessage += "SMS from " + strMsgSrc + " : " + strMsgBody;
            }
            Log.e(TAG + "antx", strMessage);
        }

    }
}
