package com.example.signiai

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.util.UUID

class GeneratedActivity : AppCompatActivity() {

    private lateinit var btnCreateMeeting: Button
    private lateinit var btnShareLink: Button
    private lateinit var btnJoinMeeting: Button
    private lateinit var tvMeetingLink: TextView
    private lateinit var etMeetingId: EditText

    private var meetingId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_generated)

        // Initialize views
        btnCreateMeeting = findViewById(R.id.btnCreateMeeting)
        btnShareLink = findViewById(R.id.btnShareLink)
        btnJoinMeeting = findViewById(R.id.btnJoinMeeting)
        tvMeetingLink = findViewById(R.id.tvMeetingLink)
        etMeetingId = findViewById(R.id.etMeetingId)

        // Generate Meeting ID
        btnCreateMeeting.setOnClickListener {
            meetingId = UUID.randomUUID().toString().substring(0, 8).uppercase()

            tvMeetingLink.text = "Meeting ID: $meetingId"
            etMeetingId.setText(meetingId) // ✅ ADD THIS

            Toast.makeText(this, "Meeting Created", Toast.LENGTH_SHORT).show()
        }
        // Share Meeting ID
        btnShareLink.setOnClickListener {
            if (meetingId.isEmpty()) {
                Toast.makeText(this, "Generate meeting first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Join my SignAI meeting\nMeeting ID: $meetingId"
                )
            }

            startActivity(Intent.createChooser(shareIntent, "Share via"))
        }

        // Join Meeting
        btnJoinMeeting.setOnClickListener {

            val enteredId = etMeetingId.text.toString().trim().ifEmpty {
                meetingId
            }

            if (enteredId.isEmpty()) {
                Toast.makeText(this, "Generate or enter Meeting ID", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(this, VideoCallActivity::class.java)
            intent.putExtra("MEETING_ID", enteredId)

            startActivity(intent)
        }
    }
}