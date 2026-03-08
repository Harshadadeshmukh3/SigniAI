package com.example.signiai

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.UUID

class GeneratedActivity : AppCompatActivity() {

    private lateinit var btnCreateMeeting: Button
    private lateinit var btnShareLink: Button
    private lateinit var btnstartvideocall: Button
    private lateinit var tvMeetingLink: TextView

    private var meetingLink: String = ""

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // This must load your Generate Link screen
        setContentView(R.layout.activity_generated)

        btnCreateMeeting = findViewById(R.id.btnCreateMeeting)
        btnShareLink = findViewById(R.id.btnShareLink)
        tvMeetingLink = findViewById(R.id.tvMeetingLink)
        btnstartvideocall = findViewById(R.id.btnstartvideocall)

        // Generate Meeting Link
        btnCreateMeeting.setOnClickListener {

            val meetingId = UUID.randomUUID().toString().substring(0, 8)

            meetingLink = "https://signiai.app/join/$meetingId"

            tvMeetingLink.text = meetingLink

            Toast.makeText(this, "Meeting Created", Toast.LENGTH_SHORT).show()
        }

        // Share Link
        btnShareLink.setOnClickListener {

            if (meetingLink.isEmpty()) {
                Toast.makeText(this, "Generate link first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"

            shareIntent.putExtra(
                Intent.EXTRA_SUBJECT,
                "Join my SignAI Video Call"
            )

            shareIntent.putExtra(
                Intent.EXTRA_TEXT,
                "Hello,\n\nJoin my SignAI video meeting:\n$meetingLink"
            )
            // SAFE SHARE (prevents crash)
            if (shareIntent.resolveActivity(packageManager) != null) {
                startActivity(Intent.createChooser(shareIntent, "Share via"))
            } else {
                Toast.makeText(this, "No app available to share", Toast.LENGTH_SHORT).show()
            }
        }
        btnstartvideocall.setOnClickListener {

            if (meetingLink.isEmpty()) {
                Toast.makeText(this, "Generate meeting link first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val meetingId = meetingLink.substringAfterLast("/")

            val intent = Intent(this, VideoCallActivity::class.java)
            intent.putExtra("MEETING_ID", meetingId)

            startActivity(intent)
        }
    }
}