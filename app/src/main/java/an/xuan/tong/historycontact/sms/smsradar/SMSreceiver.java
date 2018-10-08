package an.xuan.tong.historycontact.sms.smsradar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.telephony.SmsMessage;
import android.util.Log;

import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import an.xuan.tong.historycontact.db.SmsSendServer;

public class SMSreceiver extends BroadcastReceiver {
    private final String TAG = this.getClass().getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED"))
            Log.e(TAG + "antx", "SMSreceiver onReceive SMS_RECEIVED");
        Bundle extras = intent.getExtras();
        String strMessage = "";
        final String fAccountId;
        final String acountId = "";

        /*if (extras != null) {
            Object[] smsextras = (Object[]) extras.get("pdus");
            for (int i = 0; i < smsextras.length; i++) {
                SmsMessage smsmsg = SmsMessage.createFromPdu((byte[]) smsextras[i]);
                String strMsgBody = smsmsg.getMessageBody().toString();
                String strMsgSrc = smsmsg.getOriginatingAddress();
                strMessage += "SMS from " + strMsgSrc + " : " + strMsgBody;
                Log.e(TAG + " antx", strMsgBody + " strMsgSrc" + strMsgSrc);
                sendDatatoFirebase("SMS_RECEIVED", strMsgBody, String.valueOf(smsmsg.getTimestampMillis()), "", strMsgSrc);
            }

        }*/


    }

    private void sendDatatoFirebase(final String type, final String message, final String date, final String messId, final String phone) {
        AccountKit.getCurrentAccount(
                new AccountKitCallback<Account>() {
                    @Override
                    public void onSuccess(Account account) {
                        SmsSendServer smsSendServer = new SmsSendServer(account.getId().toString()
                                , type
                                , message
                                , date.toString()
                                , messId
                                , phone
                                , "");
                        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
                        Log.e("mDatabase", "token: " + FirebaseDatabase.getInstance().getApp());
                        mDatabase.child("Data").child("SMS").child(account.getId().toString()).child(phone).child(date).setValue(smsSendServer).
                                addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.e("mDatabase", "token: onSuccess");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e("mDatabase", "token: onFailure " + e.getMessage());
                                    }
                                });
                        Log.e("antx", "firebase SMS Send ok");
                    }

                    @Override
                    public void onError(AccountKitError accountKitError) {

                    }
                });
    }
}
