package com.mobile.spanduk.ui.sign

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.mobile.spanduk.databinding.ActivityVerifyOtpBinding
import com.mobile.spanduk.utils.Connection
import com.mobile.spanduk.utils.Preferences
import java.util.concurrent.TimeUnit

class VerifyOTPActivity : AppCompatActivity() {

    lateinit var systemCode: String
    lateinit var mAuth: FirebaseAuth
    private lateinit var bind: ActivityVerifyOtpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityVerifyOtpBinding.inflate(layoutInflater)
        setContentView(bind.root)

        val net = Connection(this)
        val pref = Preferences(this)
        val phone = pref.getValues("phone").toString()

        mAuth = FirebaseAuth.getInstance()

        bind.btnBack.setOnClickListener {
            onBackPressed()
        }

        val options = PhoneAuthOptions.newBuilder(mAuth)
            .setPhoneNumber(phone)
            .setTimeout(30L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(mCallbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)

        bind.btnResend.setOnClickListener {
            PhoneAuthProvider.verifyPhoneNumber(options)
            Toast.makeText(this, "Silakan tunggu dan periksa sms Anda", Toast.LENGTH_SHORT).show()
        }

        bind.btnVerify.setOnClickListener {
            if (net.isOnline()) {
                val code = bind.pinView.text.toString()

                if (!code.isEmpty()) {
                    verifyCode(code)
                } else {
                    Toast.makeText(this,
                        "Silakan masukkan kode verifikasi terlebih dahulu!",
                        Toast.LENGTH_SHORT).show()
                }
            } else {
                net.isOffline()
            }
        }
    }

    private val mCallbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onCodeSent(s: String, forceResendingToken: PhoneAuthProvider.ForceResendingToken) {
            super.onCodeSent(s, forceResendingToken)
            systemCode = s
        }

        override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
            val code = phoneAuthCredential.smsCode

            if (code != null) {
                bind.pinView.setText(code)
            }
        }

        override fun onVerificationFailed(error: FirebaseException) {
            Toast.makeText(this@VerifyOTPActivity, error.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun verifyCode(code: String) {
        val credential = PhoneAuthProvider.getCredential(systemCode, code)
        signInWithPhoneAuthCredential(credential)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) {
                if (it.isSuccessful) {
                    Toast.makeText(this, "Verifikasi Berhasil!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, UpdatePasswordActivity::class.java))
                } else {
                    if (it.exception is FirebaseAuthInvalidCredentialsException) {
                        Toast.makeText(this, "Kode verifikasi salah! Silakan coba lagi", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }
}