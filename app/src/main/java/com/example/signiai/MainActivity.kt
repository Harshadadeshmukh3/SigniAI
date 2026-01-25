package com.example.signiai

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import java.util.Calendar
import com.google.firebase.firestore.Query
import android.annotation.SuppressLint
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Patterns
import android.view.MotionEvent
import android.view.View
import android.widget.*
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    // Layouts
    private lateinit var loginLayout: LinearLayout
    private lateinit var signupLayout: LinearLayout
    private lateinit var resetLayout: LinearLayout
    private lateinit var homeLayoutMain: ConstraintLayout
    private lateinit var adminDashboardLayoutMain: ConstraintLayout

    private lateinit var imgLogo: ImageView
    private lateinit var tvForgot: TextView
    private lateinit var tvToSignup: TextView
    private lateinit var tvSignupTitle: TextView
    private lateinit var tvSignupSubtitle: TextView

    private lateinit var tvTotalUsers: TextView
    private lateinit var tvActiveUsers: TextView
    private lateinit var tvNewUsers: TextView
    private lateinit var tvAiRequests: TextView
    private lateinit var tvLastLogin: TextView


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
        adminDashboardLayoutMain = findViewById(R.id.adminDashboardLayoutMain)
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

        //admin
        tvTotalUsers = findViewById(R.id.tvTotalUsers)
        tvActiveUsers = findViewById(R.id.tvActiveUsers)
        tvNewUsers = findViewById(R.id.tvNewUsers)
        tvAiRequests = findViewById(R.id.tvAiRequests)
        tvLastLogin = findViewById(R.id.tvLastLogin)


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
                    hideAll()

                    // ADMIN CHECK
                    if (email == "34harshadad@gmail.com") {
                        adminDashboardLayoutMain.visibility = View.VISIBLE
                        // 🔥 ADD THIS LINE HERE
                        loadAdminDashboardData()

                    } else {
                        homeLayoutMain.visibility = View.VISIBLE
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
                        "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
                        "isActive" to true
                    )

                    com.google.firebase.firestore.FirebaseFirestore.getInstance()
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

    private fun loadAdminDashboardData() {

        val db = FirebaseFirestore.getInstance()

        // TOTAL USERS
        db.collection("users")
            .get()
            .addOnSuccessListener {
                tvTotalUsers.text = it.size().toString()
            }

        // ACTIVE USERS
        db.collection("users")
            .whereEqualTo("isActive", true)
            .get()
            .addOnSuccessListener {
                tvActiveUsers.text = it.size().toString()
            }

        // NEW USERS (TODAY)
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val startOfDay = calendar.time

        db.collection("users")
            .whereGreaterThan("createdAt", startOfDay)
            .get()
            .addOnSuccessListener {
                tvNewUsers.text = it.size().toString()
            }

        // AI REQUESTS
        db.collection("gesture_history")
            .get()
            .addOnSuccessListener {
                tvAiRequests.text = it.size().toString()
            }

        // LAST LOGIN
        db.collection("users")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener {
                if (!it.isEmpty) {
                    val ts = it.documents[0].getTimestamp("createdAt")
                    tvLastLogin.text = ts?.toDate().toString()
                }
            }
    }
    
    // ================= HELPERS =================

    private fun hideAll() {
        loginLayout.visibility = View.GONE
        signupLayout.visibility = View.GONE
        resetLayout.visibility = View.GONE
        homeLayoutMain.visibility = View.GONE
        adminDashboardLayoutMain.visibility = View.GONE
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
