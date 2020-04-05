package com.avengers.enterpriseexpensetracker

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton
import java.util.*

class VoiceBotActivity : AppCompatActivity() {

    var btnVoice: AppCompatImageButton? = null
    private var linearLayout: LinearLayout? = null

    private var tts: TextToSpeech? = null

    private val REQ_SPEECH_INPUT: Int = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        linearLayout = findViewById(R.id.layout)
        btnVoice = findViewById(R.id.btnListen)
        btnVoice?.setOnClickListener {
            promptSpeechInput()
        }

        initTTS()
    }

    private fun initTTS() {
        tts = TextToSpeech(applicationContext, TextToSpeech.OnInitListener { status ->
            if (status == TextToSpeech.SUCCESS) {
                val ttsLang = tts?.setLanguage(Locale.US)
                if (ttsLang == TextToSpeech.LANG_MISSING_DATA ||
                    ttsLang == TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE
                ) {
                    Log.e("Error: %s", "Language not supported !")
                } else {
                    Log.i("Success: %s", "Language Supported");
                }
            } else {
                Toast.makeText(
                    applicationContext,
                    "TTS initialization failed!",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQ_SPEECH_INPUT -> if (resultCode == Activity.RESULT_OK && data != null) {
                val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                val command = result[0]

                val inflater =
                    applicationContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val voiceText = inflater.inflate(R.layout.layout_command, null) as TextView
                voiceText.text = command
                linearLayout?.addView(voiceText)

                var answer = "Sorry I didn't get you!"
                if (command != null || !command.equals("")) {
                    when {
                        isSubmittingExpense(command) -> {
                            answer =
                                "What kind of expense you want to submit? Travel, Food or Other"
                        }
                        isExpenseType(command) -> {
                            answer = "How much amount you want to submit?"
                        }
                        isAmount(command) -> {
                            answer = "Yes, go ahead"
                        }
                    }
                }

                tts?.speak(answer, TextToSpeech.QUEUE_FLUSH, null, "1234")
            }
        }
    }

    private fun isSubmittingExpense(command: String?): Boolean {
        return command!!.contains("submit expenses", true) ||
                command.contains("submit my expenses", true) ||
                command.contains("submit expense", true) ||
                command.contains("submit my expense", true) ||
                command.contains("expenses", true) ||
                command.contains("submit", true)
    }

    private fun isAmount(command: String?): Boolean {
        return command!!.contains("dollar", false) ||
                command.contains("dollars", false) ||
                command.contains("$", false)
    }

    private fun isExpenseType(command: String?): Boolean {
        return command!!.contains("travel", false) ||
                command.contains("food", false) ||
                command.contains("other", false)
    }

    private fun promptSpeechInput() {
        intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Voice bot is now listening your command")
        startActivityForResult(intent, REQ_SPEECH_INPUT)
    }


    override fun onDestroy() {
        super.onDestroy()
        tts?.stop()
        tts?.shutdown()
    }
}

