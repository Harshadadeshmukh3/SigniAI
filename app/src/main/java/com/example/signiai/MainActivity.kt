package com.example.signiai

import android.annotation.SuppressLint
import android.os.Bundle
import android.content.Intent
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Patterns
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
class MainActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    // Layouts
    private lateinit var loginLayout: LinearLayout
    private lateinit var signupLayout: LinearLayout
    private lateinit var resetLayout: LinearLayout
    private lateinit var homeLayoutMain: ConstraintLayout

    private lateinit var imgLogo: ImageView
    private lateinit var tvForgot: TextView
    private lateinit var tvToSignup: TextView
    private lateinit var tvSignupTitle: TextView
    private lateinit var tvSignupSubtitle: TextView




    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Layout references
        loginLayout = findViewById(R.id.loginLayout)
        signupLayout = findViewById(R.id.signupLayout)
        resetLayout = findViewById(R.id.resetLayout)
        homeLayoutMain = findViewById(R.id.homeLayoutMain)

        imgLogo = findViewById(R.id.imgLogo)

        // Login views
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        // Signup views
        val etSignupName = findViewById<EditText>(R.id.etSignupName)
        val etSignupEmail = findViewById<EditText>(R.id.etSignupEmail)
        val etSignupPassword = findViewById<EditText>(R.id.etSignupPassword)
        val etSignupConfirmPassword = findViewById<EditText>(R.id.etSignupConfirmPassword)
        val btnSignup = findViewById<Button>(R.id.btnSignup)

        // Reset views
        val etResetEmail = findViewById<EditText>(R.id.etResetEmail)
        val btnReset = findViewById<Button>(R.id.btnReset)

        // Text links
        tvForgot = findViewById(R.id.tvForgot)
        tvToSignup = findViewById(R.id.tvToSignup)
        tvSignupTitle = findViewById(R.id.tvSignupTitle)
        tvSignupSubtitle = findViewById(R.id.tvSignupSubtitle)
        val tvBackToLogin = findViewById<TextView>(R.id.tvBackToLogin)




        togglePassword(etPassword)
        togglePassword(etSignupPassword)
        togglePassword(etSignupConfirmPassword)

        showLoginOnly()

        // 👁️ PASSWORD SHOW / HIDE (drawableEnd ic_eye)
        // =====================================================
        var isVisible = false

        etPassword.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {

                val drawableEnd = 2 // RIGHT drawable

                if (etPassword.compoundDrawables[drawableEnd] != null &&
                    event.rawX >=
                    (etPassword.right -
                            etPassword.compoundDrawables[drawableEnd].bounds.width())
                ) {

                    isVisible = !isVisible

                    etPassword.transformationMethod =
                        if (isVisible)
                            HideReturnsTransformationMethod.getInstance()
                        else
                            PasswordTransformationMethod.getInstance()

                    etPassword.setSelection(etPassword.text.length)
                    return@setOnTouchListener true
                }
            }
            false
        }

        // LOGIN
        btnLogin.setOnClickListener {

            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                toast("Fill all fields")
                return@setOnClickListener
            }

            mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {

                    val uid = mAuth.currentUser!!.uid

                    // ✅ STEP 3 — EXACT PLACE (YOU DID IT RIGHT)
                    FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(uid)
                        .get()
                        .addOnSuccessListener { doc ->

                            if (!doc.exists()) {
                                toast("USER DOCUMENT NOT FOUND")
                                return@addOnSuccessListener
                            }

                            val role = doc.getString("role")

                            if (role == "ADMIN") {
                                startActivity(Intent(this, AdminDashboardActivity::class.java))
                                finish()
                            }
                            else {
                                hideAll()
                                homeLayoutMain.visibility = View.VISIBLE
                            }
                        }
                        .addOnFailureListener { e ->
                            toast("Firestore error: ${e.message}")
                        }
                }
                .addOnFailureListener {
                    toast(it.message ?: "Login failed")
                }
        }


        // GO TO SIGNUP
        tvToSignup.setOnClickListener {
            hideAll()
            signupLayout.visibility = View.VISIBLE
            tvSignupTitle.visibility = View.VISIBLE
            tvSignupSubtitle.visibility = View.VISIBLE
        }

        // SIGNUP
        btnSignup.setOnClickListener {

            val name = etSignupName.text.toString().trim()
            val email = etSignupEmail.text.toString().trim()
            val pass = etSignupPassword.text.toString()
            val confirm = etSignupConfirmPassword.text.toString()

            // 🔴 ADD IT HERE (EXACT PLACE)
            if (name.isEmpty()) {
                toast("Enter your name")
                return@setOnClickListener
            }

            if (email.isEmpty() || pass.isEmpty() || confirm.isEmpty()) {
                toast("Fill all fields")
                return@setOnClickListener
            }

            if (pass != confirm) {
                toast("Passwords do not match")
                return@setOnClickListener
            }

            // 🔥 Firebase signup starts AFTER validation
            mAuth.createUserWithEmailAndPassword(email, pass)
                .addOnSuccessListener {
                            val uid = mAuth.currentUser!!.uid

                            val userMap = hashMapOf(
                                "name" to name,
                                "email" to email,
                                "userType" to "user",
                                "role" to "USER",
                                "createdAt" to com.google.firebase.Timestamp.now(),
                                "isActive" to true
                            )

                            FirebaseFirestore.getInstance()
                                .collection("users")
                                .document(uid)
                                .set(userMap)
                                .addOnSuccessListener {
                                    toast("Account created")
                                    showLoginOnly()
                                }
                        }
                        .addOnFailureListener {
                            toast(it.message ?: "Signup failed")
                        }
                }

        // BACK TO LOGIN
        tvBackToLogin.setOnClickListener {
            showLoginOnly()
        }

        // FORGOT PASSWORD
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

            mAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener {
                    toast("Reset link sent")
                    showLoginOnly()
                }
                .addOnFailureListener {
                    toast("Failed to send reset link")
                }
        }
    }

    // ================= HELPERS =================

    private fun hideAll() {
        loginLayout.visibility = View.GONE
        signupLayout.visibility = View.GONE
        resetLayout.visibility = View.GONE
        homeLayoutMain.visibility = View.GONE
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

    @SuppressLint("ClickableViewAccessibility")
    private fun togglePassword(editText: EditText) {
        editText.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawable = editText.compoundDrawables[2]
                if (drawable != null &&
                    event.rawX >= (editText.right - drawable.bounds.width())
                ) {
                    editText.transformationMethod =
                        if (editText.transformationMethod is PasswordTransformationMethod)
                            HideReturnsTransformationMethod.getInstance()
                        else
                            PasswordTransformationMethod.getInstance()

                    editText.setSelection(editText.text.length)
                    return@setOnTouchListener true
                }
            }
            false
        }
    }
}
