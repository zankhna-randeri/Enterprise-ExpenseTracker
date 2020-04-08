package com.avengers.enterpriseexpensetracker.ui.add_expense

import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.avengers.enterpriseexpensetracker.R
import java.util.*

class AddExpenseFragment : Fragment() {
    private lateinit var addExpenseViewModel: AddExpenseViewModel
    private var btnVoice: AppCompatImageButton? = null
    private var linearLayout: LinearLayout? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private var speechRecognizerIntent: Intent? = null
    private var isListening = false
    private lateinit var speechRecognitionListener: SpeechRecognitionListener

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

        initSpeechRecognizer()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        speechRecognizer?.destroy()
        speechRecognitionListener.shutDownTTs()
    }

    private fun initSpeechRecognizer() {
        speechRecognitionListener = SpeechRecognitionListener(activity, addExpenseViewModel)
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(activity)
        speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Voice bot is now listening your command")
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, activity?.packageName)
        }
        speechRecognizer?.setRecognitionListener(speechRecognitionListener)
    }

    private fun promptSpeechInput() {
        if (!isListening) {
            speechRecognizer?.startListening(speechRecognizerIntent)
        }
    }
}
