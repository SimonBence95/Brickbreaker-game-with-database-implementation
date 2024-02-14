package hu.nye.android.zhbeadando

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var scoreText: TextView
    private lateinit var paddle: View
    private lateinit var ball: View
    private lateinit var brickContainer: LinearLayout

    private var ballX = 0f
    private var ballY = 0f
    private var ballSpeedX = 0f
    private var ballSpeedY = 0f
    private var paddleX = 0f
    private var score = 0
    private val brickRows = 9
    private val brickColumns = 10
    private val brickWidth = 100
    private val brickHeight = 40
    private val brickMargin = 4
    private var isGameOver = true
    private var lives = 3
    private val handler = Handler(Looper.getMainLooper())

    private var startTime: Long = 0
    private var stopperRunning = false
    private var elapsedTime = 0L

    private fun startStopper() {
        startTime = System.currentTimeMillis()
        stopperRunning = true

        handler.postDelayed(object : Runnable {
            override fun run() {
                if (stopperRunning) {
                    val currentTime = System.currentTimeMillis()
                    elapsedTime = (currentTime - startTime) /1000

                    val stopperTextView = findViewById<TextView>(R.id.stopperText)
                    stopperTextView.text = "$elapsedTime mp"

                    handler.postDelayed(this, 100) // Refresh every second
                }
            }
        }, 1000)
    }

    private fun stopStopper() {
        if (stopperRunning) {
            stopperRunning = false
            val stopperTextView = findViewById<TextView>(R.id.stopperText)
            stopperTextView.text = "$elapsedTime mp"
        }
    }

    private var mediaPlayer: MediaPlayer? = null

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()

    }

    private var playerName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        paddle = findViewById(R.id.paddle)
        scoreText = findViewById(R.id.scoreText)
        ball = findViewById(R.id.ball)
        brickContainer = findViewById(R.id.brickContainer)

        val username = intent.getStringExtra("username")
        if (!username.isNullOrEmpty()) {
            playerName = username
            Toast.makeText(this, "Hello, $username!", Toast.LENGTH_SHORT).show()

            intent.putExtra("username", username)

            val databaseHandler = DatabaseHandler(this)

            val username = intent.getStringExtra("username")

            if (username != null) {
                val userNameText: TextView = findViewById(R.id.textView_UserName)
                userNameText.text = username
            } else {
                val userNameText: TextView = findViewById(R.id.textView_UserName)
                userNameText.text = "Nem található felhasználói név"
            }

        } else {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        val buttonNewGame = findViewById<Button>(R.id.button_Newgame)
        val buttonHighScores = findViewById<Button>(R.id.button_HighScores)

        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val result = audioManager.requestAudioFocus(
            null,
            AudioManager.STREAM_MUSIC,
            AudioManager.AUDIOFOCUS_GAIN
        )

        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            mediaPlayer = MediaPlayer.create(this, R.raw.game_music)
            mediaPlayer?.isLooping = true
            mediaPlayer?.start()
            mediaPlayer?.seekTo(0)
            Toast.makeText(this, "Hangfókusz megszerzése sikerült", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Hangfókusz megszerzése nem sikerült", Toast.LENGTH_SHORT).show()
        }

        //kezdődhet a játék
        buttonNewGame.setOnClickListener {
            if (isGameOver) {
                brickContainer.removeAllViews()
                initializeBricks()
                resetBallPosition()
                isGameOver = false
                score = 0
                scoreText.text = "Score: $score"
                lives = 3
                start()
                startStopper()
            }
            buttonNewGame.visibility = View.INVISIBLE
            buttonHighScores.visibility = View.INVISIBLE
        }

        buttonHighScores.setOnClickListener {
            val intent = Intent(this, HighScoresActivity::class.java)
            intent.putExtra("username", username)
            startActivity(intent)
        }

        // Csúszkamozgásának vezérlése
        paddle.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_MOVE -> {
                    if (!isGameOver) {
                        val x = event.rawX
                        val viewWidth = view.width.toFloat()
                        val containerWidth = brickContainer.width.toFloat()
                        val normalizedX = x / containerWidth

                        // Példa: A csúszka színét változtatjuk az érintés helye alapján
                        val red = (normalizedX * 255).toInt()
                        val green = 0
                        val blue = 255 - (normalizedX * 255).toInt()

                        view.setBackgroundColor(Color.rgb(red, green, blue))

                        if (!isGameOver) movePaddle(event.rawX)
                    }
                }
            }
            true
        }
    }

    private fun initializeBricks() {

        for (row in 0 until brickRows) {
            val rowLayout = LinearLayout(this)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            rowLayout.layoutParams = params

            for (col in 0 until brickColumns) {
                val brick = View(this)
                val brickParams = LinearLayout.LayoutParams(brickWidth, brickHeight)
                brickParams.setMargins(brickMargin, brickMargin, brickMargin, brickMargin)
                brick.layoutParams = brickParams
                brick.setBackgroundResource(R.drawable.ic_launcher_background)
                rowLayout.addView(brick)
            }

            brickContainer.addView(rowLayout)
        }
    }

    private fun moveBall() {
        ballX += ballSpeedX
        ballY += ballSpeedY

        ball.x = ballX
        ball.y = ballY
    }

    private fun movePaddle(x: Float) {
        paddleX = x - paddle.width / 2
        paddle.x = paddleX
    }

    private fun checkCollision() {
        val screenWidth = resources.displayMetrics.widthPixels.toFloat()
        val screenHeight = resources.displayMetrics.heightPixels.toFloat()

        if (ballX <= 0 || ballX + ball.width >= screenWidth) {
            ballSpeedX *= -1
        }

        if (ballY <= 0) {
            ballSpeedY *= -1
        }

        if (ballY + ball.height >= paddle.y && ballY + ball.height <= paddle.y + paddle.height
            && ballX + ball.width >= paddle.x && ballX <= paddle.x + paddle.width
        ) {
            // Az ütközéskor pattanjon vissza a golyó
            val paddleCenterX = paddle.x + paddle.width / 2
            val ballCenterX = ball.x + ball.width / 2
            val deviation = ballCenterX - paddleCenterX

            //pattanjon vissza a labda
            ballSpeedX = deviation * 0.2f // Az itteni értékek a pattanás erősségét szabályozzák
            ballSpeedY *= -1

            //visszapattanás után egyre jobban gyorsuljon fel a labda
            ballSpeedY *= 1.033f
            ballSpeedX *= 1.033f

            score++
            scoreText.text = "Score: $score"
        }

        if (ballY + ball.height >= screenHeight - 100) {
            lives--
            if (lives > 0) {
                Toast.makeText(this, "$lives lives left", Toast.LENGTH_SHORT).show()
                resetBallPosition()
            } else {
                gameOver()
            }
        }

        var allBricksInvisible = true

        for (row in 0 until brickRows) {
            val rowLayout = brickContainer.getChildAt(row) as LinearLayout
            for (col in 0 until brickColumns) {
                val brick = rowLayout.getChildAt(col) as View
                if (brick.visibility == View.VISIBLE) {
                    allBricksInvisible = false
                    val brickLeft = brick.x + rowLayout.x
                    val brickRight = brickLeft + brick.width
                    val brickTop = brick.y + rowLayout.y
                    val brickBottom = brickTop + brick.height
                    if (ballX + ball.width >= brickLeft && ballX <= brickRight
                        && ballY + ball.height >= brickTop && ballY <= brickBottom
                    ) {
                        brick.visibility = View.INVISIBLE
                        ballSpeedY *= -1
                        score++
                        scoreText.text = "Score: $score"
                    }
                }
            }
        }

        if (allBricksInvisible) {
            winGame()
            stopStopper()
        }
    }

    private fun resetBallPosition() {
        val displayMetrics = resources.displayMetrics
        val screenDensity = displayMetrics.density
        val screenWidth = displayMetrics.widthPixels.toFloat()
        val screenHeight = displayMetrics.heightPixels.toFloat()

        ballX = screenWidth / 2 - ball.width / 2
        ballY = screenHeight / 2 - ball.height / 2

        ball.x = ballX
        ball.y = ballY

        ballSpeedX = 0f
        ballSpeedY = 0f

        paddleX = screenWidth / 2 - paddle.width / 2
        paddle.x = paddleX

        ballSpeedX = 3 * screenDensity
        ballSpeedY = -3 * screenDensity
    }

    private fun gameOver() {
        scoreText.text = "GAME OVER"
        isGameOver = true
        stopStopper()

        playerName?.let {
            val databaseHandler = DatabaseHandler(this)
            val finalScore = calculateScore()
            databaseHandler.addScore(it, finalScore)
        }

        val buttonNewGame = findViewById<Button>(R.id.button_Newgame)
        buttonNewGame.visibility = View.VISIBLE
        val buttonHighScores = findViewById<Button>(R.id.button_HighScores)
        buttonHighScores.visibility = View.VISIBLE
    }

    private fun winGame() {
        scoreText.text = "You won!"
        isGameOver = true
        stopStopper()

        playerName?.let {
            val databaseHandler = DatabaseHandler(this)
            val finalScore = calculateScore()
            databaseHandler.addScore(it, finalScore)
        }

        val buttonNewGame = findViewById<Button>(R.id.button_Newgame)
        buttonNewGame.visibility = View.VISIBLE
        val buttonHighScores = findViewById<Button>(R.id.button_HighScores)
        buttonHighScores.visibility = View.VISIBLE
    }

    private fun start() {
        val displayMetrics = resources.displayMetrics
        val screenDensity = displayMetrics.density
        val screenWidth = displayMetrics.widthPixels.toFloat()
        val screenHeight = displayMetrics.heightPixels.toFloat()

        paddleX = screenWidth / 2 - paddle.width / 2
        paddle.x = paddleX

        ballX = screenWidth / 2 - ball.width / 2
        ballY = screenHeight / 2 - ball.height / 2

        ballSpeedX = 3 * screenDensity
        ballSpeedY = -3 * screenDensity

        startStopper()

        val updateInterval = 8 // Milliseconds (about 120 FPS)
        handler.post(object : Runnable {
            override fun run() {
                if (!isGameOver) {
                    moveBall()
                    checkCollision()
                    handler.postDelayed(this, updateInterval.toLong())
                } else {
                    stopStopper()
                }
            }
        })
    }

    private fun calculateScore(): Int {
        val baseScore = score // Base score from hits
        val timePenalty = elapsedTime.toInt()
        val lifeScore = (3 - lives) * 100

        return baseScore - timePenalty + lifeScore
    }
}