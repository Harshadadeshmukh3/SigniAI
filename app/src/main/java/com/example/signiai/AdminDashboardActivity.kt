package com.example.signiai

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.Calendar

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore

    private lateinit var tvTotalUsers: TextView
    private lateinit var tvActiveUsers: TextView
    private lateinit var tvNewUsers: TextView
    private lateinit var tvAiRequests: TextView
    private lateinit var tvLastLogin: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        db = FirebaseFirestore.getInstance()

        tvTotalUsers = findViewById(R.id.tvTotalUsers)
        tvActiveUsers = findViewById(R.id.tvActiveUsers)
        tvNewUsers = findViewById(R.id.tvNewUsers)
        tvAiRequests = findViewById(R.id.tvAiRequests)
        tvLastLogin = findViewById(R.id.tvLastLogin)

        loadAdminDashboardData()
    }

    private fun loadAdminDashboardData() {

        db.collection("users").get()
            .addOnSuccessListener {
                tvTotalUsers.text = it.size().toString()
            }

        db.collection("users")
            .whereEqualTo("isActive", true)
            .get()
            .addOnSuccessListener {
                tvActiveUsers.text = it.size().toString()
            }

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        db.collection("users")
            .whereGreaterThan("createdAt", calendar.time)
            .get()
            .addOnSuccessListener {
                tvNewUsers.text = it.size().toString()
            }

        db.collection("gesture_history").get()
            .addOnSuccessListener {
                tvAiRequests.text = it.size().toString()
            }

        db.collection("users")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener {
                if (!it.isEmpty) {
                    tvLastLogin.text =
                        it.documents[0].getTimestamp("createdAt")
                            ?.toDate().toString()
                }
            }
    }
}
