package com.example.userkabirstore.fragment

import android.graphics.Color
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import cn.pedant.SweetAlert.SweetAlertDialog
import com.example.userkabirstore.R
import com.example.userkabirstore.databinding.FragmentProfileBinding
import com.example.userkabirstore.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class ProfileFragment : BaseFragment() {

    private val auth = FirebaseAuth.getInstance()
    private val databaseReference = FirebaseDatabase.getInstance().reference
    private lateinit var binding: FragmentProfileBinding
    private lateinit var dialog: SweetAlertDialog
    private lateinit var confirmBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        showLoader(true)

        checkInternetAndSaveUserData()

        binding.apply {
            nameProfile.isEnabled = false
            emailProfile.isEnabled = false
            addressProfile.isEnabled = false
            phoneProfile.isEnabled = false
            saveInfoBtn.isEnabled = false
            saveInfoBtn.setBackgroundResource(R.drawable.proceed_disabled)

            editProfileTv.setOnClickListener {
                nameProfile.isEnabled = !nameProfile.isEnabled
                emailProfile.isEnabled = !emailProfile.isEnabled
                addressProfile.isEnabled = !addressProfile.isEnabled
                phoneProfile.isEnabled = !phoneProfile.isEnabled
                saveInfoBtn.isEnabled = !saveInfoBtn.isEnabled

                if (!saveInfoBtn.isEnabled) {
                    saveInfoBtn.setBackgroundResource(R.drawable.proceed_disabled)
                } else {
                    saveInfoBtn.setBackgroundResource(R.drawable.login_btn_shape)
                }


            }
        }


        binding.saveInfoBtn.setOnClickListener {

            val name = binding.nameProfile.text.toString()
            val email = binding.emailProfile.text.toString()
            val address = binding.addressProfile.text.toString()
            val phone = binding.phoneProfile.text.toString()




            if (isInternetOn(requireContext())) {

                val namePattern = "^[A-Za-z\\s]{2,50}$"
                val mobilePattern = "^(?:\\+91|91)?[789]\\d{9}$"
                val addressPattern = "^[a-zA-Z0-9\\s,.#-]+$"

                if (email.isBlank() || name.isBlank() || address.isBlank() || phone.isBlank()) {
                    snackBarBottom(binding.root, "Please fill all the details")

                }else if(!name.matches(Regex(namePattern))){
                    snackBarBottom(binding.root, "Please enter a valid name (2-50 alphabetic characters)")
                }else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    snackBarBottom(binding.root, "Please enter a valid email address")
                }else if(!address.matches(Regex(addressPattern))){
                    snackBarBottom(binding.root, "Please enter a valid address.")
                }else if(!phone.matches(Regex(mobilePattern))){
                    snackBarBottom(binding.root, "Please enter a valid mobile number.")
                }  else {
                    updateUserProfile(name, email, address, phone)
                }
            } else {
                dialog = SweetAlertDialog(
                    requireContext(), SweetAlertDialog.ERROR_TYPE
                ).setTitleText(getString(R.string.title_internet))
                    .setContentText(getString(R.string.content_internet)).setConfirmText("Retry")
                dialog.setCancelable(false)
                dialog.setConfirmClickListener { sweetAlertDialog: SweetAlertDialog ->  // Explicit type
                    sweetAlertDialog.dismissWithAnimation()
                }
                dialog.show()


                confirmBtn =
                    dialog.findViewById<View>(cn.pedant.SweetAlert.R.id.confirm_button) as Button
                confirmBtn.setTextColor(Color.BLACK)
                confirmBtn.textSize = 14f
            }
        }

        return binding.root
    }

    private fun checkInternetAndSaveUserData() {
        if (isInternetOn(requireContext())) {
            saveUserData()
        } else {
            dialog = SweetAlertDialog(
                requireContext(), SweetAlertDialog.ERROR_TYPE
            ).setTitleText(getString(R.string.title_internet))
                .setContentText(getString(R.string.content_internet)).setConfirmText("Retry")
            dialog.setCancelable(false)
            dialog.setConfirmClickListener { sweetAlertDialog: SweetAlertDialog ->  // Explicit type
                // Optionally check again when the user presses the confirm button
                saveUserData()
                sweetAlertDialog.dismissWithAnimation()
            }
            dialog.show()


            confirmBtn =
                dialog.findViewById<View>(cn.pedant.SweetAlert.R.id.confirm_button) as Button
            confirmBtn.setTextColor(Color.BLACK)
            confirmBtn.textSize = 14f
        }
    }


    private fun showLoader(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun updateUserProfile(name: String, email: String, address: String, phone: String) {

        val userId = auth.currentUser?.uid

        if (userId != null) {

            val userReference = databaseReference.child("user").child(userId)

            val userData = hashMapOf(
                "name" to name, "address" to address, "email" to email, "phone" to phone
            )
            userReference.setValue(userData).addOnSuccessListener {

                snackBarBottom(binding.root, "Profile Updated Successfully")

            }.addOnFailureListener {

                snackBarBottom(binding.root, "Profile update failed")
            }
        }
    }

    private fun saveUserData() {
        val userId = auth.currentUser?.uid

        if (userId != null) {
            val userReference = databaseReference.child("user").child(userId)

            userReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    if (snapshot.exists()) {

                        val userProfile = snapshot.getValue(UserModel::class.java)

                        if (userProfile != null) {


                            binding.nameProfile.setText(userProfile.name)
                            binding.emailProfile.setText(userProfile.email)
                            binding.addressProfile.setText(userProfile.address)
                            binding.phoneProfile.setText(userProfile.phone)
                        }

                    }

                    showLoader(false)
                }

                override fun onCancelled(error: DatabaseError) {
                    showLoader(false)
                }

            })
        }

    }

    companion object {


    }
}