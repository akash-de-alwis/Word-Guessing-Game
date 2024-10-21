package com.example.wordguessinggame

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class LeaderboardActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var leaderboardTextView: TextView

    companion object {
        const val PREFS_NAME = "WordGuessingGame"
        const val SCORE_PREFIX = "score_"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboard)

        // Initialize SharedPreferences and views
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        leaderboardTextView = findViewById(R.id.leaderboardText)

        // Display the leaderboard
        displayLeaderboard()

        // Set listeners for the buttons
        findViewById<Button>(R.id.backButton).setOnClickListener {
            finish() // Close the leaderboard activity and return to the previous one
        }

        findViewById<Button>(R.id.restartButton).setOnClickListener {
            showRestartConfirmation() // Show confirmation dialog before restarting
        }
    }

    private fun displayLeaderboard() {
        val leaderboard = mutableListOf<Pair<String, Int>>() // List to hold names and scores

        // Retrieve all entries from SharedPreferences
        sharedPreferences.all.forEach { (key, value) ->
            if (key.startsWith(SCORE_PREFIX) && value is Int) {
                // Extract player name by removing the "score_" prefix and any extra part after the name
                val name = key.substringAfter(SCORE_PREFIX).substringBefore("_")
                leaderboard.add(Pair(name, value)) // Add the player name and score to the list
            }
        }

        // Sort the leaderboard by scores in descending order
        leaderboard.sortByDescending { it.second }

        // Build the display string
        val leaderboardString = if (leaderboard.isNotEmpty()) {
            leaderboard.joinToString("\n") { "${it.first} scored: ${it.second}" } // Proper format
        } else {
            "No scores available."
        }

        // Display the leaderboard on the TextView
        leaderboardTextView.text = leaderboardString
    }


    private fun showRestartConfirmation() {
        // Create an AlertDialog for confirmation
        AlertDialog.Builder(this).apply {
            setTitle("Restart Game")
            setMessage("Are you sure you want to restart the game?")
            setPositiveButton("Yes") { _, _ -> restartGame() }
            setNegativeButton("No", null)
            create()
            show()
        }
    }

    private fun restartGame() {
        // Restart the game by launching the MainActivity
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK) // Clear activity stack
        startActivity(intent)
        finish() // Finish this activity
    }
}
