package com.example.wordguessinggame

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var scoreTextView: TextView
    private lateinit var guessInput: EditText
    private lateinit var submitButton: Button
    private lateinit var clueButton: Button
    private lateinit var leaderboardButton: Button
    private lateinit var timerTextView: TextView  // Timer display
    private var secretWord: String = ""
    private var score: Int = 100
    private var guessesLeft: Int = 10
    private var clueCount: Int = 0
    private var clues: String = ""
    private lateinit var selectedCategory: String
    private var timer: CountDownTimer? = null  // Timer variable
    private var timeRemaining: Long = 60000  // 60 seconds in milliseconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        sharedPreferences = getSharedPreferences("WordGuessingGame", MODE_PRIVATE)
        scoreTextView = findViewById(R.id.scoreText)
        guessInput = findViewById(R.id.guessInput)
        submitButton = findViewById(R.id.submitGuessButton)
        clueButton = findViewById(R.id.clueButton)
        leaderboardButton = findViewById(R.id.leaderboardButton)
        timerTextView = findViewById(R.id.timerText)  // Timer display initialization

        // Ask for the user's name when the app starts
        askUserName()

        // Submit button action
        submitButton.setOnClickListener {
            val userGuess = guessInput.text.toString()
            checkGuess(userGuess)
        }

        // Clue button action
        clueButton.setOnClickListener {
            giveClue()
        }

        // Leaderboard button action
        leaderboardButton.setOnClickListener {
            val intent = Intent(this, LeaderboardActivity::class.java)
            startActivity(intent)
        }
    }

    private fun askUserName() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Enter Your Name")

        val input = EditText(this)
        builder.setView(input)

        builder.setPositiveButton("OK") { dialog, _ ->
            val userName = input.text.toString()
            sharedPreferences.edit().putString("userName", userName).apply()
            loadUserName() // Load the username after it's set
            fetchRandomWord() // Fetch random word after entering name
            startTimer()  // Start the timer after the user enters their name
            dialog.dismiss()
        }

        builder.setCancelable(false)
        builder.show()
    }

    private fun loadUserName() {
        val userName = sharedPreferences.getString("userName", "User")
        findViewById<TextView>(R.id.welcomeMessage).text = "Welcome, $userName!"
    }

    private fun fetchRandomWord() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://random-words5.p.rapidapi.com/")  // Correct base URL
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(WordApiService::class.java)
        val call = service.getRandomWord()

        call.enqueue(object : Callback<List<String>> {
            override fun onResponse(call: Call<List<String>>, response: Response<List<String>>) {
                if (response.isSuccessful && response.body() != null) {
                    secretWord = response.body()!![0]  // Access the first word from the list
                    selectedCategory = "Random Words"
                    clueCount = 0
                    clues = ""
                    showFirstClue()
                    updateScoreTextView()
                } else {
                    handleError()
                }
            }

            override fun onFailure(call: Call<List<String>>, t: Throwable) {
                handleError()
            }
        })
    }

    private fun handleError() {
        secretWord = "default"
        selectedCategory = "Default Category"
        showFirstClue()
        updateScoreTextView()
        startTimer()  // Start the timer even if there is an error
    }

    private fun updateScoreTextView() {
        scoreTextView.text = "Category: $selectedCategory \n Score: $score \n Guesses left: $guessesLeft\n\n$clues"
    }

    private fun showFirstClue() {
        val firstLetter = secretWord[0].uppercaseChar()
        clues = "\t\tTip : First letter is '$firstLetter'"
    }

    private fun checkGuess(guess: String) {
        if (guess.equals(secretWord, true)) {
            saveScore()
            timer?.cancel()  // Cancel the timer if the game is won
            showGameResultDialog("Congratulations!", "You've won! The correct word was: $secretWord\nFinal Score: $score")
        } else {
            guessesLeft--
            score -= 10
            updateScoreTextView()
            if (guessesLeft == 0) {
                timer?.cancel()  // Cancel the timer if game is over
                showGameResultDialog("Game Over!", "The correct word was: $secretWord\nFinal Score: $score")
            }
        }
    }

    private fun giveClue() {
        if (score >= 5) {
            score -= 5
            when (clueCount) {
                0 -> {
                    val hasDoubleLetter = secretWord.zipWithNext().any { it.first == it.second }
                    clues += if (hasDoubleLetter) {
                        "\n\t\tClue 1: The word contains double letters."
                    } else {
                        "\n\t\tClue 1: The word does not contain double letters."
                    }
                }
                1 -> {
                    val firstLetter = secretWord[0].uppercaseChar()
                    // Reveal a random letter that has not been guessed
                    val unrevealedLetters = secretWord.filter { !clues.contains(it, ignoreCase = true) }
                    if (unrevealedLetters.isNotEmpty()) {
                        val randomLetter = unrevealedLetters.random().uppercaseChar()
                        clues += "\n\t\tClue 2: A letter in the word is '$randomLetter'."
                    } else {
                        clues += "\n\t\tClue 2: No unrevealed letters left."
                    }
                }
                2 -> {
                    val lastLetter = secretWord.last().uppercaseChar()
                    clues += "\n\t\tClue 3: Last letter is '$lastLetter'"
                }
                3 -> {
                    val vowels = secretWord.filter { it in "aeiouAEIOU" }
                    clues += "\n\t\tClue 4: Vowels in the word are: $vowels"
                }
                4 -> {
                    val wordLength = secretWord.length
                    clues += "\n\t\tClue 5: The word has $wordLength letters."
                }
                5 -> {
                    val consonants = secretWord.filter { it.isLetter() && it !in "aeiouAEIOU" }
                    clues += "\n\t\tClue 6: Consonants in the word are: $consonants"
                }
                else -> {
                    clues += "\n\t\tNo more clues available."
                }
            }
            clueCount++
            updateScoreTextView()
        } else {
            scoreTextView.text = "Not enough points for a clue!"
        }
    }

    private fun startTimer() {
        timer?.cancel()  // Cancel any existing timer
        timeRemaining = 120000  // Reset timer to 60 seconds

        timer = object : CountDownTimer(timeRemaining, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeRemaining = millisUntilFinished
                timerTextView.text = "Time Remaining: ${timeRemaining / 1000} seconds"  // Update timer display
            }

            override fun onFinish() {
                timerTextView.text = "Time's up!"
                showGameResultDialog("Time's Up!", "The correct word was: $secretWord\nFinal Score: $score")
                guessesLeft = 0  // Set guesses left to 0
            }
        }.start()
    }

    private fun saveScore() {
        // Save the score in shared preferences only if the player has won
        val editor = sharedPreferences.edit()
        val userName = sharedPreferences.getString("userName", "User") ?: "User"

        // Generate a unique key for each score by appending a timestamp
        val uniqueKey = "score_${userName}_${System.currentTimeMillis()}"

        // Save the score
        if (score > 0) {
            editor.putInt(uniqueKey, score) // Use the player's name and timestamp as part of the key
            editor.apply()
        }
    }

    private fun showGameResultDialog(title: String, message: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
        builder.setCancelable(false)
        builder.show()
    }
}

