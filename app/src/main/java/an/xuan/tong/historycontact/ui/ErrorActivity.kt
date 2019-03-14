package an.xuan.tong.historycontact.ui

import an.xuan.tong.historycontact.R
import com.facebook.accountkit.AccountKitError
import com.facebook.accountkit.AccountKit
import android.os.Bundle
import android.app.Activity
import kotlinx.android.synthetic.main.activity_error.*


class ErrorActivity : Activity() {

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_error)
        log_out_button.setOnClickListener {
            AccountKit.logOut()
            finish()
        }
        val acountError = intent.getParcelableExtra<AccountKitError>(TOKEN_ACTIVITY_ERROR_EXTRA)
        if (error.text != null) {
            if (acountError != null) {
                error.text = acountError.toString()
            } else {
                error.setText(R.string.na)
            }
        }
    }

    companion object {
        internal val TOKEN_ACTIVITY_ERROR_EXTRA = "TOKEN_ACTIVITY_ERROR_EXTRA"
    }
}