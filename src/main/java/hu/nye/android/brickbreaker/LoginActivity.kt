package hu.nye.android.zhbeadando

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity

class LoginActivity : AppCompatActivity() {

    lateinit var databaseHandler: DatabaseHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        databaseHandler = DatabaseHandler(this) // Adatbázis kezelő létrehozása

        val editTextUsername = findViewById<EditText>(R.id.editTextUsername)
        val buttonStartGame = findViewById<Button>(R.id.buttonStartGame)

        buttonStartGame.setOnClickListener {
            val username = editTextUsername.text.toString()

            if (username.isNotEmpty()) {
                val success = databaseHandler.addUsername(username)
                if (success > 0) {
                    Toast.makeText(this, "Username added to database", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("username",username)
                    startActivity(intent)

                } else {
                    Toast.makeText(this, "Failed to add username", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please enter a username", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
