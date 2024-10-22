package com.example.userkabirstore.activity

import android.content.Context
import android.graphics.Color
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.userkabirstore.R
import com.google.android.material.snackbar.Snackbar
import java.util.regex.Pattern


open class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

     fun snackBarTop(view : View , msg: String) {

        val snack: Snackbar = Snackbar.make(view, msg, 1000)
        val view = snack.view
        val params = view.layoutParams as FrameLayout.LayoutParams
        params.gravity = Gravity.TOP
        view.layoutParams = params
        snack.setBackgroundTint(resources.getColor(R.color.lavender))
            .setTextColor(Color.BLACK)
        snack.show()

    }

    fun snackBarBottom(view : View , msg: String) {

        val snack: Snackbar = Snackbar.make(view, msg, 1000)
        val view = snack.view
        val params = view.layoutParams as FrameLayout.LayoutParams
        params.gravity = Gravity.BOTTOM
        view.layoutParams = params
        snack.setBackgroundTint(resources.getColor(R.color.lavender))
            .setTextColor(Color.BLACK)
        snack.show()

    }

    fun isInternetOn(context: Context): Boolean {
        try {
            val cm = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork = cm.activeNetworkInfo
            if (activeNetwork != null) { // connected to the internet
                if (activeNetwork.type == ConnectivityManager.TYPE_WIFI) {
                    // connected to wifi
                    return true
                } else if (activeNetwork.type == ConnectivityManager.TYPE_MOBILE) {
                    // connected to the mobile provider's data plan
                    return true
                }
            } else {
                // not connected to the internet
                return false
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    fun isValidEmailId(email: String?): Boolean {
        val pattern: Pattern
        val EMAIL_PATTERN =
            "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$"
        pattern = Pattern.compile(EMAIL_PATTERN)
        val matcher = pattern.matcher(email)
        return matcher.matches()
    }

    fun isValidMobileNumber(value: String?): Boolean {
        var flag = false
        try {
            val pattern = "^[6789]\\d{9}$"
            flag = if (Pattern.compile(pattern).matcher(value).matches()) {
                true
            } else {
                false
            }
            return flag
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            return flag
        }
    }


    fun isValidPassword(password: String?): Boolean {
        // Regex to check valid password.

        val regex = ("^(?=.*[0-9])"
                + "(?=.*[a-z])(?=.*[A-Z])"
                + "(?=.*[@#$%^&+=])"
                + "(?=\\S+$).{8,20}$")


        // Compile the ReGex
        val p = Pattern.compile(regex)


        // If the password is empty
        // return false
        if (password == null) {
            return false
        }


        // Pattern class contains matcher() method
        // to find matching between given password
        // and regular expression.
        val m = p.matcher(password)


        // Return if the password
        // matched the ReGex
        return m.matches()
    }

}