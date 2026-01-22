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

    // Layouts
    private lateinit var loginLayout: LinearLayout
    private lateinit var signupLayout: LinearLayout
    private lateinit var resetLayout: LinearLayout
    private lateinit var homeLayout: LinearLayout
    private lateinit var adminDashboardLayout: LinearLayout
    private lateinit var imgLogo: ImageView

    // TextViews
    private lateinit var tvForgot: TextView
    private lateinit var tvToSignup: TextView
    private lateinit var tvSignupTitle: TextView
    private lateinit var tvSignupSubtitle: TextView
    private lateinit var txtAdminEmail: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAuth = FirebaseAuth.getInstance()

        // Init layouts
        loginLayout = findViewById(R.id.loginLayout)
        signupLayout = findViewById(R.id.signupLayout)
        resetLayout = findViewById(R.id.resetLayout)
        homeLayout = findViewById(R.id.homeLayout)
        adminDashboardLayout = findViewById(R.id.adminDashboardLayout)
        imgLogo = findViewById(R.id.imgLogo)

        // Init views
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        tvForgot = findViewById(R.id.tvForgot)
        tvToSignup = findViewById(R.id.tvToSignup)
        tvSignupTitle = findViewById(R.id.tvSignupTitle)
        tvSignupSubtitle = findViewById(R.id.tvSignupSubtitle)
        txtAdminEmail = findViewById(R.id.txtAdminEmail)

        val btnReset = findViewById<Button>(R.id.btnReset)
        val etResetEmail = findViewById<EditText>(R.id.etResetEmail)

        val btnSignup = findViewById<Button>(R.id.btnSignup)
        val etSignupEmail = findViewById<EditText>(R.id.etSignupEmail)
        val etSignupPassword = findViewById<EditText>(R.id.etSignupPassword)
        val etSignupConfirmPassword =
            findViewById<EditText>(R.id.etSignupConfirmPassword)
        val tvBackToLogin = findViewById<TextView>(R.id.tvBackToLogin)

        togglePassword(etPassword)
        togglePassword(etSignupPassword)
        togglePassword(etSignupConfirmPassword)

        // 👇 VERY IMPORTANT: show login first
        showLoginOnly()

        // ================= LOGIN =================
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                toast("Fill all fields")
                return@setOnClickListener
            }

            mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {

                        val adminEmail = "34harshadad@gmail.com"
                        hideAll()

                        if (email == adminEmail) {
                            txtAdminEmail.text = email
                            adminDashboardLayout.visibility = View.VISIBLE
                        } else {
                            homeLayout.visibility = View.VISIBLE
                        }

                    } else {
                        toast(task.exception?.message ?: "Login failed")
                    }
                }
        }

        // ================= FORGOT =================
        tvForgot.setOnClickListener {
            hideAll()
            resetLayout.visibility = View.VISIBLE
            imgLogo.visibility = View.VISIBLE
        }

        btnReset.setOnClickListener {
            val email = etResetEmail.text.toString().trim()
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                toast("Enter valid email")
                return@setOnClickListener
            }

            mAuth.sendPasswordResetEmail(email).addOnSuccessListener {
                toast("Reset link sent")
                showLoginOnly()
            }
        }

        // ================= SIGNUP =================
        tvToSignup.setOnClickListener {
            hideAll()
            signupLayout.visibility = View.VISIBLE
            tvSignupTitle.visibility = View.VISIBLE
            tvSignupSubtitle.visibility = View.VISIBLE
        }

        tvBackToLogin.setOnClickListener {
            showLoginOnly()
        }

        btnSignup.setOnClickListener {
            val email = etSignupEmail.text.toString().trim()
            val pass = etSignupPassword.text.toString()
            val confirm = etSignupConfirmPassword.text.toString()

            if (email.isEmpty() || pass.isEmpty() || confirm.isEmpty()) {
                toast("Fill all fields")
                return@setOnClickListener
            }
            if (pass != confirm) {
                toast("Passwords do not match")
                return@setOnClickListener
            }

            mAuth.createUserWithEmailAndPassword(email, pass)
                .addOnSuccessListener {
                    toast("Account created")
                    showLoginOnly()
                }
                .addOnFailureListener {
                    toast(it.message ?: "Error")
                }
        }
    }

    // ================= HELPERS =================
    private fun hideAll() {
        loginLayout.visibility = View.GONE
        signupLayout.visibility = View.GONE
        resetLayout.visibility = View.GONE
        homeLayout.visibility = View.GONE
        adminDashboardLayout.visibility = View.GONE
        imgLogo.visibility = View.GONE
        tvForgot.visibility = View.GONE
        tvToSignup.visibility = View.GONE
        tvSignupTitle.visibility = View.GONE
        tvSignupSubtitle.visibility = View.GONE
    }

    private fun showLoginOnly() {
        hideAll()
        loginLayout.visibility = View.VISIBLE
        imgLogo.visibility = View.VISIBLE
        tvForgot.visibility = View.VISIBLE
        tvToSignup.visibility = View.VISIBLE
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    // Password eye
    @SuppressLint("ClickableViewAccessibility")
    private fun togglePassword(editText: EditText) {
        editText.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = editText.compoundDrawables[2]
                if (drawableEnd != null &&
                    event.rawX >= (editText.right - drawableEnd.bounds.width())
                ) {
                    editText.transformationMethod =
                        if (editText.transformationMethod is PasswordTransformationMethod)
                            null else PasswordTransformationMethod.getInstance()
                    editText.setSelection(editText.text.length)
                    return@setOnTouchListener true
                }
            }
            false
        }
    }
}
