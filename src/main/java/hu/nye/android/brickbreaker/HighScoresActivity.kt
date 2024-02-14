package hu.nye.android.zhbeadando

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity

class HighScoresActivity : AppCompatActivity() {

    private lateinit var databaseHandler: DatabaseHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_high_scores)

        val listTest: ListView = findViewById(R.id.listView_Teszt)
        databaseHandler = DatabaseHandler(this)

        val username = intent.getStringExtra("username")
        if (username != null) {
            val userScores = databaseHandler.getUserScores(username)
            val userScoresWithUsername = userScores.map { score -> "$username: $score" }

            val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, userScoresWithUsername)
            listTest.adapter = adapter
        }
    }
}

