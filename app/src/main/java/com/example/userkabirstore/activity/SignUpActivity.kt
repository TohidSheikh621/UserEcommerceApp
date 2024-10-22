package com.example.userkabirstore.activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Button
import cn.pedant.SweetAlert.SweetAlertDialog
import com.example.userkabirstore.R
import com.example.userkabirstore.databinding.ActivitySignUpBinding
import com.example.userkabirstore.model.UserModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database

class SignUpActivity : BaseActivity() {
    private lateinit var email: String
    private lateinit var name: String
    private lateinit var password: String
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var dialog: SweetAlertDialog
    private lateinit var confirmBtn: Button

    private lateinit var binding: ActivitySignUpBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            window.statusBarColor = resources.getColor(R.color.lavender) // Optionally change the status bar background color to white
        }


        auth = Firebase.auth

        databaseReference = Firebase.database.reference

        binding.createAcBtn.setOnClickListener {
            email = binding.emailEt.text.toString().trim()
            name = binding.nameEt.text.toString().trim()
            password = binding.passwordEt.text.toString().trim()
            val passwordPattern = "^(?=.*[a-z])(?=.*\\d)(?=.*[@\$!%*?&])[A-Za-z\\d@\$!%*?&]{8,}$"
            val namePattern = "^[A-Za-z\\s]{2,50}$"

            if (email.isBlank() || name.isBlank() || password.isBlank()) {
                snackBarBottom(binding.root, "Please fill all the details")

            }else if(!name.matches(Regex(namePattern))){
                snackBarBottom(binding.root, "Please enter a valid name (2-50 alphabetic characters)")
            }else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                snackBarBottom(binding.root, "Please enter a valid email address")
            }else if(!password.matches(Regex(passwordPattern))){
                snackBarBottom(binding.root, "Password must be at least 8 characters long, include a digit, a lowercase, an uppercase letter, and a special character")
            } else {

                showLoader(true)
                if (isInternetOn(this)) {
                    createAccount(email, password)
                } else {
                    showLoader(true)
                    dialog = SweetAlertDialog(
                        this, SweetAlertDialog.ERROR_TYPE
                    ).setTitleText(getString(R.string.title_internet))
                        .setContentText(getString(R.string.content_internet))
                        .setConfirmText("Retry")
                    dialog.setCancelable(false)
                    dialog.setConfirmClickListener { sweetAlertDialog: SweetAlertDialog ->  // Explicit type
                        // Optionally check again when the user presses the confirm button
                        sweetAlertDialog.dismissWithAnimation()
                        showLoader(false)
                    }
                    dialog.show()

                    confirmBtn =
                        dialog.findViewById<View>(cn.pedant.SweetAlert.R.id.confirm_button) as Button
                    confirmBtn.setTextColor(Color.BLACK)
                    confirmBtn.textSize = 14f
                }
            }
        }

        binding.alreadyAccountTv.setOnClickListener {

            var intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()

        }
    }


    private fun showLoader(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
            binding.overlayView.visibility = View.VISIBLE // Show the transparent overlay
        } else {
            binding.progressBar.visibility = View.GONE
            binding.overlayView.visibility = View.GONE // Hide the transparent overlay
        }
    }

    private fun createAccount(email: String, password: String) {
        showLoader(true)
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {

                snackBarBottom(binding.root, "Account Created")
                saveUserData()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                finish()
                showLoader(false)
            } else {

                val errorMessage = when (task.exception) {
                    is FirebaseAuthWeakPasswordException -> "The password is too weak."
                    is FirebaseAuthInvalidCredentialsException -> "The email address is malformed."
                    is FirebaseAuthUserCollisionException -> "An account with this email already exists."
                    else -> "Account creation failed. Please try again."
                }

                snackBarBottom(binding.root, errorMessage)
                Log.d("Account", "createAccount : Failure", task.exception)
                showLoader(false)

            }
        }
    }

    private fun saveUserData() {
        email = binding.emailEt.text.toString().trim()
        name = binding.nameEt.text.toString().trim()
        password = binding.passwordEt.text.toString().trim()

        val user = UserModel(email, name, password)
        val userId = FirebaseAuth.getInstance().currentUser!!.uid

        databaseReference.child("user").child(userId).setValue(user)
    }
}