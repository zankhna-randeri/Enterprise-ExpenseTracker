package com.avengers.enterpriseexpensetracker.ui.add_expense

import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.avengers.enterpriseexpensetracker.R
import com.avengers.enterpriseexpensetracker.util.Utility
import java.util.*

class AddExpenseFragment : Fragment() {

    private lateinit var addExpenseViewModel: AddExpenseViewModel
    private var btnVoice: AppCompatImageButton? = null
    private var linearLayout: LinearLayout? = null

    private var tts: TextToSpeech? = null

    companion object {
        private const val REQ_SPEECH_INPUT: Int = 120
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        addExpenseViewModel =
            ViewModelProvider(this).get(AddExpenseViewModel::class.java)
        return inflater.inflate(R.layout.fragment_add_expense, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        linearLayout = view.findViewById(R.id.layout)
        btnVoice = view.findViewById(R.id.btnListen)
        btnVoice?.setOnClickListener {
            promptSpeechInput()
        }

        initTTS()
    }

    private fun initTTS() {
        tts = TextToSpeech(activity, TextToSpeech.OnInitListener { status ->
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
                Utility.getInstance().showMsg(activity, getString(R.string.tts_init_failed))
            }
        })
    }

    private fun promptSpeechInput() {
        activity?.intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Voice bot is now listening your command")
        }
        startActivityForResult(activity?.intent, Companion.REQ_SPEECH_INPUT)

    }
}
