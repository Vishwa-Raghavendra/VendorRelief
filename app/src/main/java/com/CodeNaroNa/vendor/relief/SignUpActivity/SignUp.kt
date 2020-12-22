package com.CodeNaroNa.vendor.relief.SignUpActivity

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider

import com.CodeNaroNa.vendor.relief.GlobalHelpers.Constants
import com.CodeNaroNa.vendor.relief.GlobalHelpers.Resource
import com.CodeNaroNa.vendor.relief.R
import com.CodeNaroNa.vendor.relief.Repositories.CentralRepository
import com.CodeNaroNa.vendor.relief.SignUpActivity.ViewModels.SignUpActivityViewModel
import com.CodeNaroNa.vendor.relief.SignUpActivity.ViewModels.SignUpActivityViewModelFactory
import com.CodeNaroNa.vendor.relief.GlobalHelpers.BaseActivity
import com.CodeNaroNa.vendor.relief.VendorActivity.VendorActivity
import com.CodeNaroNa.vendor.relief.databinding.ActivitySignUpBinding
import com.CodeNaroNa.vendor.relief.SignUpActivity.Dialogs.DialogLayout
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class SignUp : BaseActivity(), DialogLayout.OtpDialogInterface {
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var viewModel: SignUpActivityViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Creating ViewModel
        val factory = SignUpActivityViewModelFactory(this.applicationContext, CentralRepository())
        viewModel = ViewModelProvider(this, factory).get(SignUpActivityViewModel::class.java)

        //To Delete Current User [Testing Purposes only]
//        FirebaseAuth.getInstance().currentUser?.apply {
//            this.delete()
//        }


        setUpClickListeners()
        setUpLiveDataObservers()
    }

    private fun setUpLiveDataObservers() {
        viewModel.optVerificationState.observe(this, {
            when (it) {
                is Resource.Success -> {
                    hideProgressDialog()
                    val intent = Intent(applicationContext, VendorActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                }
                is Resource.Error -> {
                    hideProgressDialog()
                    showErrorSnackMessage(it.message!!)
                }
                is Resource.Loading -> {
                    showProgressDialog("Signing In...")
                }
                else -> {
                }
            }
        })
    }

    private fun showOTPDialog() {
        val dialogLayout = DialogLayout(this)
        dialogLayout.show(supportFragmentManager, "otpDialog")
    }

    private fun sendOtpCode(phoneNumber: String) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(phoneNumber, 60, TimeUnit.SECONDS, this, object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(p0: PhoneAuthCredential) {
                showToast("OTP Arrived")
            }

            override fun onVerificationFailed(p0: FirebaseException) {
                showErrorSnackMessage("" + p0.localizedMessage)
            }

            override fun onCodeSent(p0: String, p1: PhoneAuthProvider.ForceResendingToken) {
                super.onCodeSent(p0, p1)
                viewModel.saveEnteredOTP(p0)
            }
        })
    }

    private fun setUpClickListeners() {
        binding.genOtp.setOnClickListener {
            closeKeyBoard(binding.root)
            generateOTP(binding.phone.text.toString())
        }
    }

    private fun generateOTP(phoneNumber: String) {
        if (viewModel.verifyNumber(phoneNumber)) {
            showOTPDialog()
            sendOtpCode(viewModel.modifiedPhoneNumber)
        } else
            showErrorSnackMessage("Invalid Phone Number")
    }

    private fun getUserType(): String = if (binding.userSelected.checkedRadioButtonId == R.id.newUser) Constants.NEW_USER else Constants.EXISTING_USER
    override fun verify(otpEdit: String?) {
        viewModel.signInUser(otpEdit!!, getUserType())
    }
}