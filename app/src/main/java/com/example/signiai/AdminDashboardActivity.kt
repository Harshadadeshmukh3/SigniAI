package com.example.signiai

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import java.util.Calendar

@Suppress("DEPRECATION")
class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    // 🔹 Drawer
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var btnMenu: ImageView
    // ====== DASHBOARD + USER MANAGEMENT SWITCHING ======

    private lateinit var dashboardLayout: ConstraintLayout
    private lateinit var userManagementLayout: LinearLayout

    // Activity Logs Layout
    private lateinit var layoutActivityLogs: ConstraintLayout
    private lateinit var logsRecyclerView: RecyclerView

    // Filter Buttons
    private lateinit var btnAll: Button
    private lateinit var btnLogins: Button
    private lateinit var btnUsage: Button
    private lateinit var btnErrors: Button

    private lateinit var menuDashboard: TextView
    private lateinit var menuUsers: TextView

    private lateinit var etSearchUser: EditText
    private lateinit var recyclerUsers: androidx.recyclerview.widget.RecyclerView
    // 🔹 Dashboard TextViews
    private lateinit var cardTotalUsers: androidx.cardview.widget.CardView
    private lateinit var cardActiveUsers: androidx.cardview.widget.CardView
    private lateinit var cardNewUsers: androidx.cardview.widget.CardView
    private lateinit var cardAI: androidx.cardview.widget.CardView
    private lateinit var cardLastUser: androidx.cardview.widget.CardView

    // ✅ TextViews INSIDE cards
    private lateinit var txtTotalUsers: TextView
    private lateinit var txtActiveUsers: TextView
    private lateinit var txtNewUsers: TextView
    private lateinit var txtAIRequestsText: TextView
    private lateinit var lastLoginText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val user = mAuth.currentUser
        if (user == null) {
            finish()
            return
        }

        // hide UI until admin verified
        window.decorView.visibility = View.INVISIBLE

        db.collection("users")
            .document(user.uid)
            .get()
            .addOnSuccessListener { doc ->

                if (!doc.exists()) {
                    finish()
                    return@addOnSuccessListener
                }

                val role = doc.getString("role")

                if (role == "ADMIN") {
                    window.decorView.visibility = View.VISIBLE
                    setupViews()
                    loadAdminDashboardData()
                } else {
                    finish()
                }
            }
            .addOnFailureListener {
                finish()
            }
    }

    private fun setupViews() {
        // 🔹 FIND DRAWER VIEWS
        drawerLayout = findViewById(R.id.drawerLayout)
        btnMenu = findViewById(R.id.btnMenu)

        // 🔹 FIND DASHBOARD VIEWS

        cardTotalUsers = findViewById(R.id.cardTotalUsers)
        cardActiveUsers = findViewById(R.id.cardActiveUsers)
        cardNewUsers = findViewById(R.id.cardNewUsers)
        cardAI = findViewById(R.id.cardAI)
        cardLastUser = findViewById(R.id.cardLastUser)

        txtTotalUsers = findViewById(R.id.txtTotalUsers)
        txtActiveUsers = findViewById(R.id.tvActiveUsers)
        txtNewUsers = findViewById(R.id.txvNewUsers)
        txtAIRequestsText = findViewById(R.id.txtAiRequestsText)
        lastLoginText = findViewById(R.id.lastLoginText)
        // ===== FIND MAIN LAYOUTS =====
        dashboardLayout = findViewById(R.id.dashboardLayout)
        userManagementLayout = findViewById(R.id.layoutUserManagement)
// ===== FIND MENU ITEMS =====
        menuDashboard = findViewById(R.id.menuDashboard)
        menuUsers = findViewById(R.id.menuUsers)

// ===== FIND USER MANAGEMENT VIEWS =====
        etSearchUser = findViewById(R.id.etSearchUser)
        recyclerUsers = findViewById(R.id.recyclerUsers)
//activity logs
        layoutActivityLogs = findViewById(R.id.layoutActivityLogs)
        logsRecyclerView = findViewById(R.id.logsRecyclerView)

        btnAll = findViewById(R.id.btnAll)
        btnLogins = findViewById(R.id.btnLogins)
        btnUsage = findViewById(R.id.btnUsage)
        btnErrors = findViewById(R.id.btnErrors)
        // RecyclerView setup
        logsRecyclerView.layoutManager = LinearLayoutManager(this)

        // 🔹 FIND MENU ITEMS
        val menuLogs = findViewById<TextView>(R.id.menuLogs)
        val menuSettings = findViewById<TextView>(R.id.menuSettings)
        val menuAIFeatures = findViewById<TextView>(R.id.menuAIFeatures)
        val menuContentManagement = findViewById<TextView>(R.id.menuContentManagement)
        val menuNotifications = findViewById<TextView>(R.id.menuNotifications)
        val menuReports = findViewById<TextView>(R.id.menuReports)
        val menuFeedback = findViewById<TextView>(R.id.menuFeedback)
        val menuAdminRoles = findViewById<TextView>(R.id.menuAdminRoles)

        val menuLogout = findViewById<TextView>(R.id.menuLogout)
        // Hide everything first
        hideAllSections()

// Show dashboard by default
        dashboardLayout.visibility = View.VISIBLE


        // 🔹 OPEN DRAWER
        btnMenu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
        // ===== DASHBOARD / USER MANAGEMENT SWITCH =====
        menuDashboard.setOnClickListener {
            hideAllSections()
            dashboardLayout.visibility = View.VISIBLE
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        menuUsers.setOnClickListener {
            hideAllSections()
            userManagementLayout.visibility = View.VISIBLE
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        menuLogs.setOnClickListener {
            hideAllSections()
            layoutActivityLogs.visibility = View.VISIBLE
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        menuSettings.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show()
        }

        menuAIFeatures.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            Toast.makeText(this, "AI / Features", Toast.LENGTH_SHORT).show()
        }

        menuContentManagement.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            Toast.makeText(this, "Content Management", Toast.LENGTH_SHORT).show()
        }

        menuNotifications.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            Toast.makeText(this, "Notifications", Toast.LENGTH_SHORT).show()
        }

        menuReports.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            Toast.makeText(this, "Reports & Analysis", Toast.LENGTH_SHORT).show()
        }

        menuFeedback.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            Toast.makeText(this, "Feedback & Support", Toast.LENGTH_SHORT).show()
        }

        menuAdminRoles.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            Toast.makeText(this, "Admin & Roles", Toast.LENGTH_SHORT).show()
        }


        menuLogout.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            Toast.makeText(this, "Logout", Toast.LENGTH_SHORT).show()

            // TODO FirebaseAuth.getInstance().signOut()
            // startActivity(Intent(this, LoginActivity::class.java))
            // finish()
        }

        // 🔹 LOAD DASHBOARD DATA
        loadAdminDashboardData()
    }

    // 🔹 FIRESTORE DATA
    @SuppressLint("SetTextI18n")
    private fun loadAdminDashboardData() {

        // 🔹 TOTAL USERS
        db.collection("users")
            .get()
            .addOnSuccessListener {
                txtTotalUsers.text = "${it.size()}\nTotal Users"
            }

        // 🔹 ACTIVE USERS
        db.collection("users")
            .whereEqualTo("isActive", true)
            .get()
            .addOnSuccessListener {
                txtActiveUsers.text = "${it.size()}\nActive Users"
            }

        // 🔹 NEW USERS TODAY
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val startOfDay = calendar.time

        db.collection("users")
            .whereGreaterThan("createdAt", com.google.firebase.Timestamp(startOfDay))
            .get()
            .addOnSuccessListener {
                txtNewUsers.text = "${it.size()}\nNew Users"
            }

        // 🔹 AI REQUESTS
        db.collection("gesture_history")
            .get()
            .addOnSuccessListener {
                txtAIRequestsText.text = "${it.size()}\nAI Requests"
            }

        // 🔹 LAST LOGIN
        db.collection("users")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    val date = snapshot.documents[0]
                        .getTimestamp("createdAt")
                        ?.toDate()

                    lastLoginText.text = "Last Login\n$date"
                }
            }
    }

    private fun hideAllSections() {
        dashboardLayout.visibility = View.GONE
        userManagementLayout.visibility = View.GONE
        layoutActivityLogs.visibility = View.GONE
    }


    // 🔹 BACK PRESS HANDLING
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}

