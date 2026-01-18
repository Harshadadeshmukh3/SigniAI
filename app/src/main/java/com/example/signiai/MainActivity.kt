package com.example.signiai

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.util.Patterns
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth

    // ✅ GLOBAL layouts
    private lateinit var loginLayout: LinearLayout
    private lateinit var signupLayout: LinearLayout
    private lateinit var resetLayout: LinearLayout
    private lateinit var imgLogo: ImageView
    private lateinit var tvForgot: TextView
    private lateinit var tvToSignup: TextView
    private lateinit var tvSignupTitle: TextView
    private lateinit var tvSignupSubtitle: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAuth = FirebaseAuth.getInstance()

        // ✅ INITIALIZE layouts (NO val here)
        loginLayout = findViewById(R.id.loginLayout)
        signupLayout = findViewById(R.id.signupLayout)
        resetLayout = findViewById(R.id.resetLayout)
        imgLogo = findViewById(R.id.imgLogo)

        // ===== Login Views =====
        val etUser = findViewById<EditText>(R.id.etUsername)
        val etPass = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        tvForgot = findViewById(R.id.tvForgot)
        tvToSignup = findViewById(R.id.tvToSignup)
        tvSignupTitle = findViewById(R.id.tvSignupTitle)
        tvSignupSubtitle = findViewById(R.id.tvSignupSubtitle)

        // ===== Reset Views =====
        val etResetEmail = findViewById<EditText>(R.id.etResetEmail)
        val btnChange = findViewById<Button>(R.id.btnChange)
        val etNewPass = findViewById<EditText>(R.id.etNewPassword)
        val etConfirmPass = findViewById<EditText>(R.id.etConfirmPassword)

        // ===== Signup Views =====
        val btnSignup = findViewById<Button>(R.id.btnSignup)
        val etSignupPassword = findViewById<EditText>(R.id.etSignupPassword)
        val etSignupEmail = findViewById<EditText>(R.id.etSignupEmail)
        val etSignupConfirmPassword = findViewById<EditText>(R.id.etSignupConfirmPassword)
        val tvBackToLogin = findViewById<TextView>(R.id.tvBackToLogin)
        val cbDeaf = findViewById<CheckBox>(R.id.cbDeaf)
        val cbMute = findViewById<CheckBox>(R.id.cbMute)
        val cbNormal = findViewById<CheckBox>(R.id.cbNormal)

        // ===== APPLY EYE ICON =====
        togglePassword(etPass)
        togglePassword(etNewPass)
        togglePassword(etConfirmPass)
        togglePassword(etSignupPassword)
        togglePassword(etSignupConfirmPassword)

        // ================= LOGIN =================
        btnLogin.setOnClickListener {

            val email = etUser.text.toString().trim()
            val password = etPass.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Login Successful 🎉", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(
                            this,
                            "Login Failed: ${task.exception?.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }

        // ================= FORGOT PASSWORD =================
        tvForgot.setOnClickListener {
            showOnly(resetLayout)
            imgLogo.visibility = View.VISIBLE
        }

        btnChange.setOnClickListener {

            val email = etResetEmail.text.toString().trim()

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                toast("Enter valid email")
                return@setOnClickListener
            }

            mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        toast("Reset link sent 📩")
                        showOnly(loginLayout)
                    } else {
                        toast(it.exception?.message ?: "Error")
                    }
                }
        }

        // ================= SIGNUP =================
        btnSignup.setOnClickListener {

            val email = etSignupEmail.text.toString().trim()
            val password = etSignupPassword.text.toString()
            val confirm = etSignupConfirmPassword.text.toString()

            if (email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
                toast("Fill all fields")
                return@setOnClickListener
            }

            if (password != confirm) {
                toast("Passwords do not match")
                return@setOnClickListener
            }

            if (!cbDeaf.isChecked && !cbMute.isChecked && !cbNormal.isChecked) {
                toast("Select user type")
                return@setOnClickListener
            }

            mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        toast("Account Created 🎉")
                        showOnly(loginLayout)
                    } else {
                        toast(it.exception?.message ?: "Error")
                    }
                }
        }

        tvToSignup.setOnClickListener {
            showOnly(signupLayout)
        }

        tvBackToLogin.setOnClickListener {
            showOnly(loginLayout)
        }
    }

    private fun showOnly(layout: View) {

        loginLayout.visibility = View.GONE
        signupLayout.visibility = View.GONE
        resetLayout.visibility = View.GONE
        imgLogo.visibility = View.GONE

        tvForgot.visibility = View.GONE
        tvToSignup.visibility = View.GONE

        layout.visibility = View.VISIBLE

        // Show heading ONLY on Signup page
        if (layout == signupLayout) {
            tvSignupTitle.visibility = View.VISIBLE
            tvSignupSubtitle.visibility = View.VISIBLE
        } else {
            tvSignupTitle.visibility = View.GONE
            tvSignupSubtitle.visibility = View.GONE
        }

        // Show bottom texts ONLY on Login page
        if (layout == loginLayout) {
            tvForgot.visibility = View.VISIBLE
            tvToSignup.visibility = View.VISIBLE
            imgLogo.visibility = View.VISIBLE
        }
    }



    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    // ================= PASSWORD EYE TOGGLE =================
    @SuppressLint("ClickableViewAccessibility")
    private fun togglePassword(editText: EditText) {
        editText.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = editText.compoundDrawables[2]
                if (drawableEnd != null &&
                    event.rawX >= (editText.right - drawableEnd.bounds.width())
                ) {
                    if (editText.transformationMethod is PasswordTransformationMethod) {
                        editText.transformationMethod = null
                    } else {
                        editText.transformationMethod =
                            PasswordTransformationMethod.getInstance()
                    }
                    editText.setSelection(editText.text.length)
                    return@setOnTouchListener true
                }
            }
            false
        }
    }
}