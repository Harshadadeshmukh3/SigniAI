package com.example.signiai

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.drawerlayout.widget.DrawerLayout
import android.widget.Switch
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.GravityCompat
import android.widget.LinearLayout
import android.widget.Toast
import android.widget.Button


import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeActivity : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    @SuppressLint("MissingInflatedId", "SetTextI18n", "UseSwitchCompatOrMaterialCode")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        val uid = mAuth.currentUser?.uid

        if (uid != null) {

            db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener { doc ->

                    if (doc.exists()) {

                        val name = doc.getString("name") ?: "User"
                        val username = doc.getString("username") ?: ""
                        val about = doc.getString("about") ?: ""

                        // HOME SCREEN
                        findViewById<TextView>(R.id.helloText).text = "Hello, $name 👋"

                        findViewById<TextView>(R.id.totalCallsText).text =
                            (doc.getLong("totalCalls") ?: 0).toString()

                        findViewById<TextView>(R.id.contactsText).text =
                            (doc.getLong("contactsCount") ?: 0).toString()

                        // PROFILE SCREEN
                        findViewById<TextView>(R.id.profileName).text = name
                        findViewById<TextView>(R.id.profileUsername).text = username
                        findViewById<TextView>(R.id.profileAbout).text = about

                    }
                }
        }

        // Layouts
        val homeLayout = findViewById<ScrollView>(R.id.homeLayout)
        val callHistoryLayout = findViewById<ScrollView>(R.id.callHistoryLayout)
        val profileLayout = findViewById<ScrollView>(R.id.profileLayout)
        val userProfileLayout = findViewById<ScrollView>(R.id.userProfileLayout)

        // Toolbar Icons
        val menuIcon = findViewById<ImageView>(R.id.menuIcon)
        val profileIcon = findViewById<ImageView>(R.id.profileIcon)

        // Bottom Navigation
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        // Edit Button
        val editBtn = findViewById<TextView>(R.id.editBtn)
        val btnNormal = findViewById<Button>(R.id.btnNormal)
        val btnMute = findViewById<Button>(R.id.btnMute)
        val btnDeaf = findViewById<Button>(R.id.btnDeaf)
        var userType = "Normal"

        btnNormal.setOnClickListener {
            userType = "Normal"
            Toast.makeText(this, "User type: Normal", Toast.LENGTH_SHORT).show()
        }

        btnMute.setOnClickListener {
            userType = "Mute"
            Toast.makeText(this, "User type: Mute", Toast.LENGTH_SHORT).show()
        }

        btnDeaf.setOnClickListener {
            userType = "Deaf"
            Toast.makeText(this, "User type: Deaf", Toast.LENGTH_SHORT).show()
        }
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)
        val settingsLayout = findViewById<ScrollView>(R.id.settingsLayout)
        val darkSwitch = findViewById<Switch>(R.id.switchDark)
        val helpBtn = findViewById<LinearLayout>(R.id.helpBtn)
        val aboutBtn = findViewById<LinearLayout>(R.id.aboutBtn)
        darkSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {

                AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_YES
                )

            } else {

                AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_NO
                )
            }
        }
        // Drawer menu items
        val menuSettings = findViewById<LinearLayout>(R.id.menuSettings)
        val feedback = findViewById<LinearLayout>(R.id.menuFeedback)
        val menuLogout = findViewById<LinearLayout>(R.id.menuLogout)
        val startCallCard =
            findViewById<com.google.android.material.card.MaterialCardView>(R.id.startCallCard)
        startCallCard.setOnClickListener {

            val intent = Intent(this, GeneratedActivity::class.java)
            startActivity(intent)

        }
        // Default Screen
        // Default Screen (App start)
        homeLayout.visibility = View.VISIBLE
        callHistoryLayout.visibility = View.GONE
        profileLayout.visibility = View.GONE
        userProfileLayout.visibility = View.GONE
        settingsLayout.visibility = View.GONE

        menuIcon.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
        menuSettings.setOnClickListener {

            drawerLayout.closeDrawer(GravityCompat.START)

            drawerLayout.post {

                homeLayout.visibility = View.GONE
                callHistoryLayout.visibility = View.GONE
                profileLayout.visibility = View.GONE
                userProfileLayout.visibility = View.GONE

                settingsLayout.visibility = View.VISIBLE
            }
        }
        helpBtn.setOnClickListener {
            Toast.makeText(this, "Help & Support", Toast.LENGTH_SHORT).show()
        }

        aboutBtn.setOnClickListener {
            Toast.makeText(this, "SigniAI v1.0", Toast.LENGTH_SHORT).show()
        }

        feedback.setOnClickListener {
            Toast.makeText(this, "Feedback clicked", Toast.LENGTH_SHORT).show()
        }

        menuLogout.setOnClickListener {

            drawerLayout.closeDrawer(GravityCompat.START)

            FirebaseAuth.getInstance().signOut()

            Toast.makeText(this, "Logged Out", Toast.LENGTH_SHORT).show()

            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        // Bottom Navigation Click
        bottomNav.setOnItemSelectedListener {

            when (it.itemId) {

                R.id.nav_home -> {

                    settingsLayout.visibility = View.GONE

                    homeLayout.visibility = View.VISIBLE
                    callHistoryLayout.visibility = View.GONE
                    profileLayout.visibility = View.GONE
                    userProfileLayout.visibility = View.GONE
                    true
                }

                R.id.nav_video -> {

                    settingsLayout.visibility = View.GONE

                    homeLayout.visibility = View.GONE
                    callHistoryLayout.visibility = View.VISIBLE
                    profileLayout.visibility = View.GONE
                    userProfileLayout.visibility = View.GONE
                    true
                }

                R.id.nav_profile -> {

                    settingsLayout.visibility = View.GONE

                    homeLayout.visibility = View.GONE
                    callHistoryLayout.visibility = View.GONE
                    profileLayout.visibility = View.VISIBLE
                    userProfileLayout.visibility = View.GONE

                    true
                }

                else -> false
            }
        }

        // Toolbar profile icon click
        profileIcon.setOnClickListener {

            settingsLayout.visibility = View.GONE

            homeLayout.visibility = View.GONE
            callHistoryLayout.visibility = View.GONE
            profileLayout.visibility = View.VISIBLE
            loadUserProfile()
            userProfileLayout.visibility = View.GONE
        }

        // Edit Profile Click
        editBtn.setOnClickListener {

            profileLayout.visibility = View.GONE
            userProfileLayout.visibility = View.VISIBLE
        }
        val saveBtn = findViewById<Button>(R.id.btnSave)

        saveBtn.setOnClickListener {

            val name = findViewById<android.widget.EditText>(R.id.etName).text.toString()
            val username = findViewById<android.widget.EditText>(R.id.etUsername).text.toString()

            val uid = mAuth.currentUser?.uid

            if (uid != null) {
                val userMap = hashMapOf(
                    "name" to name,
                    "username" to username,
                    "userType" to userType
                )

                db.collection("users")
                    .document(uid)
                    .update(userMap as Map<String, Any>)
                    .addOnSuccessListener {

                        Toast.makeText(this, "Profile Updated", Toast.LENGTH_SHORT).show()

                        userProfileLayout.visibility = View.GONE
                        profileLayout.visibility = View.VISIBLE
                    }
                    .addOnFailureListener {

                        Toast.makeText(this, "Update Failed", Toast.LENGTH_SHORT).show()
                    }
            }
        }

    }

    private fun loadUserProfile() {

        val uid = FirebaseAuth.getInstance().currentUser?.uid

        if (uid != null) {

            FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener { doc ->

                    if (doc.exists()) {

                        val name = doc.getString("name") ?: ""
                        val username = doc.getString("username") ?: ""
                        val about = doc.getString("about") ?: ""

                        findViewById<TextView>(R.id.profileName).text = name
                        findViewById<TextView>(R.id.profileUsername).text = username
                        findViewById<TextView>(R.id.profileAbout).text = about
                    }
                }
        }
    }
}
