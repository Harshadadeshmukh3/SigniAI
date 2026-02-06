package com.example.signiai

import android.annotation.SuppressLint
import android.os.Bundle
import android.content.res.Resources
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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

    private lateinit var layoutSettings: ConstraintLayout

    private lateinit var logsRecyclerView: RecyclerView
    private lateinit var menuDashboard: TextView
    private lateinit var menuUsers: TextView

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

    //user management
    private lateinit var etSearchUser: EditText

    // ✅ AUTO GENERATED USERS LIST
    private val users = mutableListOf<User>()

    // Filter Buttons
    private lateinit var btnAll: Button
    private lateinit var btnLogins: Button
    private lateinit var btnUsage: Button
    private lateinit var btnErrors: Button

    //setting
    private lateinit var userContainer: LinearLayout
    private lateinit var switchAI: SwitchMaterial
    private lateinit var switchNotification: SwitchMaterial
    private lateinit var switchRegister: SwitchMaterial

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

    private lateinit var menuLogs: TextView
    private lateinit var menuSettings: TextView
    private lateinit var menuAIFeatures: TextView
    private lateinit var menuContentManagement: TextView
    private lateinit var menuNotifications: TextView
    private lateinit var menuReports: TextView
    private lateinit var menuFeedback: TextView
    private lateinit var menuAdminRoles: TextView
    private lateinit var menuLogout: TextView

    private fun setupViews() {

        drawerLayout = findViewById(R.id.drawerLayout)
        btnMenu = findViewById(R.id.btnMenu)
        menuDashboard = findViewById(R.id.menuDashboard)
        menuUsers = findViewById(R.id.menuUsers)
        menuLogs = findViewById(R.id.menuLogs)
        menuSettings = findViewById(R.id.menuSettings)
        menuAIFeatures = findViewById(R.id.menuAIFeatures)
        menuContentManagement = findViewById(R.id.menuContentManagement)
        menuNotifications = findViewById(R.id.menuNotifications)
        menuReports = findViewById(R.id.menuReports)
        menuFeedback = findViewById(R.id.menuFeedback)
        menuAdminRoles = findViewById(R.id.menuAdminRoles)
        menuLogout = findViewById(R.id.menuLogout)

        dashboardLayout = findViewById(R.id.dashboardLayout)
        userManagementLayout = findViewById(R.id.layoutUserManagement)
        layoutActivityLogs = findViewById(R.id.layoutActivityLogs)
        layoutSettings = findViewById(R.id.layoutSettings)

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


        etSearchUser = findViewById(R.id.etSearch)
        btnAll = findViewById(R.id.btnAll)
        btnLogins = findViewById(R.id.btnLogins)
        btnUsage = findViewById(R.id.btnUsage)
        btnErrors = findViewById(R.id.btnErrors)

        logsRecyclerView = findViewById(R.id.logsRecyclerView)
        logsRecyclerView.layoutManager = LinearLayoutManager(this)

        userContainer = findViewById(R.id.userContainer)

        switchAI = findViewById(R.id.switchAI)
        switchNotification = findViewById(R.id.switchNotification)
        switchRegister = findViewById(R.id.switchRegister)

        switchRegister.setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(
                this,
                if (isChecked) "User registration allowed"
                else "User registration disabled",
                Toast.LENGTH_SHORT
            ).show()
        }

        switchAI.setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(
                this,
                if (isChecked) "AI feature enabled"
                else "AI feature disabled",
                Toast.LENGTH_SHORT
            ).show()
        }

        switchNotification.setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(
                this,
                if (isChecked) "Notifications enabled"
                else "Notifications disabled",
                Toast.LENGTH_SHORT
            ).show()
        }


        hideAllSections()
        dashboardLayout.visibility = View.VISIBLE

        btnMenu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        menuDashboard.setOnClickListener {
            hideAllSections()
            dashboardLayout.visibility = View.VISIBLE
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        menuUsers.setOnClickListener {
            hideAllSections()
            userManagementLayout.visibility = View.VISIBLE
            loadUsers()
            drawerLayout.closeDrawer(GravityCompat.START)
        }
        menuLogs.setOnClickListener {
            hideAllSections()
            layoutActivityLogs.visibility = View.VISIBLE
            drawerLayout.closeDrawer(GravityCompat.START)
            // AUTO SHOW USERS WHEN OPEN USER MANAGEMENT
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
        menuSettings.setOnClickListener {
            hideAllSections()
            layoutSettings.visibility = View.VISIBLE
            drawerLayout.closeDrawer(GravityCompat.START)
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
    private fun renderUsers(users: List<User>) {
        userContainer.removeAllViews()

        for (user in users) {
            addUserRow(user)
        }
    }

    private fun addUserRow(user: User) {

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(12.dp, 12.dp, 12.dp, 12.dp)
        }

        val tvName = TextView(this).apply {
            text = user.name
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val tvEmail = TextView(this).apply {
            text = user.email
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.5f)
        }

        val tvUid = TextView(this).apply {
            text = user.uid
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val switchStatus = SwitchMaterial(this).apply {
            isChecked = user.isActive
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

            setOnCheckedChangeListener { _, isChecked ->
                db.collection("users")
                    .document(user.uid)
                    .update("isActive", isChecked)
            }
        }

        row.addView(tvName)
        row.addView(tvEmail)
        row.addView(tvUid)
        row.addView(switchStatus)

        userContainer.addView(row)
    }

    private fun loadUsers() {
        db.collection("users")
            .get()
            .addOnSuccessListener { snapshot ->
                users.clear()

                for (doc in snapshot) {
                    users.add(
                        User(
                            uid = doc.id,
                            name = doc.getString("name") ?: "No Name",
                            email = doc.getString("email") ?: "No Email",
                            isActive = doc.getBoolean("isActive") ?: true
                        )
                    )
                }

                // ✅ THIS IS THE MISSING LINE
                renderUsers(users)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load users", Toast.LENGTH_SHORT).show()
            }
    }

    private fun hideAllSections() {
        dashboardLayout.visibility = View.GONE
        userManagementLayout.visibility = View.GONE
        layoutActivityLogs.visibility = View.GONE
        layoutSettings.visibility = View.GONE
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
// ---------- DATA MODEL ----------
data class User(
    val name: String,
    val email: String,
    val uid: String,
    val isActive: Boolean
)
val Int.dp: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()







