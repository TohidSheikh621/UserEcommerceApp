package com.example.userkabirstore.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import cn.pedant.SweetAlert.SweetAlertDialog
import com.example.userkabirstore.R
import com.example.userkabirstore.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database

class LoginActivity : BaseActivity() {

    private lateinit var email: String
    private lateinit var password: String
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var dialog: SweetAlertDialog
    private lateinit var confirmBtn: Button

    private lateinit var binding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth
        databaseReference = Firebase.database.reference

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            window.statusBarColor =
                resources.getColor(R.color.lavender) // Optionally change the status bar background color to white
        }

        val googleSignInOption =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(
                getString(
                    R.string.default_web_client_id
                )
            ).requestEmail().build()

        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOption)

        binding.loginBtn.setOnClickListener {

            email = binding.emailEt.text.toString().trim()
            password = binding.passwordEt.text.toString().trim()
            val passwordPattern = "^(?=.*[a-z])(?=.*\\d)(?=.*[@\$!%*?&])[A-Za-z\\d@\$!%*?&]{8,}$"

            if (email.isBlank() || password.isBlank()) {

                snackBarBottom(binding.root, "Please fill all the details")

            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                snackBarBottom(binding.root, "Please enter a valid email address")
            } else if (!password.matches(Regex(passwordPattern))) {
                snackBarBottom(
                    binding.root,
                    "Password must include 8+ characters, a digit, a capital letter, and a symbol."
                )
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

                    }
                    showLoader(false)
                    dialog.show()

                    confirmBtn =
                        dialog.findViewById<View>(cn.pedant.SweetAlert.R.id.confirm_button) as Button
                    confirmBtn.setTextColor(Color.BLACK)
                    confirmBtn.textSize = 14f
                }

            }
        }

        binding.googleBtn.setOnClickListener {

            showLoader(true)
            if (isInternetOn(this)) {

                val signInIntent = googleSignInClient.signInIntent
                launcher.launch(signInIntent)
            } else {
                showLoader(true)
                dialog = SweetAlertDialog(
                    this, SweetAlertDialog.ERROR_TYPE
                ).setTitleText(getString(R.string.title_internet))
                    .setContentText(getString(R.string.content_internet)).setConfirmText("Retry")
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

        binding.notHaveAcTv.setOnClickListener {
            var intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }

    private fun createAccount(email: String, password: String) {
        showLoader(true)
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {

                Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                finish()
                showLoader(false)

            } else {
                val errorMessage = when (task.exception) {
                    is FirebaseAuthInvalidUserException -> "No account found with this email. Please create an account."
                    is FirebaseAuthInvalidCredentialsException -> "Incorrect password. Please try again."
                    else -> "Login failed. Please try again."
                }

                snackBarBottom(binding.root, errorMessage)

                Log.d("Account", "loginAccount : Failure", task.exception)
                showLoader(false)
            }
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

    private val launcher =

        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            showLoader(true)
            if (result.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                if (task.isSuccessful) {
                    val account: GoogleSignInAccount = task.result
                    val credential = GoogleAuthProvider.getCredential(account.idToken, null)

                    auth.signInWithCredential(credential).addOnCompleteListener { authTask ->
                        if (authTask.isSuccessful) {
                            Toast.makeText(this, "Google Sign In Successful", Toast.LENGTH_SHORT)
                                .show()

                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                            showLoader(false)
                        } else {

                            snackBarBottom(binding.root, "Google Sign In Failed")

                            Log.e("GoogleSignIn", "Error signing in: ${authTask.exception?.message}", authTask.exception)

                            showLoader(false)
                        }
                    }

                } else {
                    snackBarBottom(binding.root, "Google Sign In Failed")

                    Log.d("Account", "GoogleSignIn : Failure", task.exception)
                    showLoader(false)
                }
            }
        }

    override fun onStart() {
        super.onStart()
        var currentUser = auth.currentUser

        if (currentUser != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}