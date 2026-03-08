package com.example.signiai

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider

import android.annotation.SuppressLint
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
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import android.widget.Spinner
import android.widget.ArrayAdapter

class MainActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    // Layouts
    private lateinit var loginLayout: LinearLayout
    private lateinit var signupLayout: LinearLayout
    private lateinit var resetLayout: LinearLayout

    private lateinit var imgLogo: ImageView
    private lateinit var tvForgot: TextView
    private lateinit var tvToSignup: TextView
    private lateinit var tvSignupTitle: TextView
    private lateinit var tvSignupSubtitle: TextView
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 100

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // ✅ Auto login check
        if (FirebaseAuth.getInstance().currentUser != null) {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
            return
        }
        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()


    // Layout references
        loginLayout = findViewById(R.id.loginLayout)
        signupLayout = findViewById(R.id.signupLayout)
        resetLayout = findViewById(R.id.resetLayout)
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
        val spUserType = findViewById<Spinner>(R.id.spUserType)

        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.user_type_array,
            android.R.layout.simple_spinner_item
        )

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spUserType.adapter = adapter

        // Reset views
        val etResetEmail = findViewById<EditText>(R.id.etResetEmail)
        val btnReset = findViewById<Button>(R.id.btnReset)

        // Text links
        tvForgot = findViewById(R.id.tvForgot)
        tvToSignup = findViewById(R.id.tvToSignup)
        tvSignupTitle = findViewById(R.id.tvSignupTitle)
        tvSignupSubtitle = findViewById(R.id.tvSignupSubtitle)
        val tvBackToLogin = findViewById<TextView>(R.id.tvBackToLogin)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val btnGoogleLogin = findViewById<Button>(R.id.btnGoogleLogin)

        btnGoogleLogin.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
        togglePassword(etPassword)
        togglePassword(etSignupPassword)
        togglePassword(etSignupConfirmPassword)

        showLoginOnly()

        // 👁️ PASSWORD SHOW / HIDE (drawableEnd ic_eye)


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
                                startActivity(Intent(this, HomeActivity::class.java))
                                finish()
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
            val userType = spUserType.selectedItem.toString()

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
                        "username" to name,
                        "about" to "",
                        "userType" to userType,
                        "role" to "USER",
                        "totalCalls" to 0,
                        "contactsCount" to 0,
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {

            val task = GoogleSignIn.getSignedInAccountFromIntent(data)

            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                toast("Google Sign In Failed")
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {

        val credential = GoogleAuthProvider.getCredential(idToken, null)

        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->

                if (task.isSuccessful) {

                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()

                } else {
                    toast("Authentication Failed")
                }
            }
    }

    // ================= HELPERS =================

    private fun hideAll() {
        loginLayout.visibility = View.GONE
        signupLayout.visibility = View.GONE
        resetLayout.visibility = View.GONE
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

