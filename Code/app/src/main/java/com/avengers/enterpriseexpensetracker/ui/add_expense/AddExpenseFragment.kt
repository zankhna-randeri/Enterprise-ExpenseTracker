package com.avengers.enterpriseexpensetracker.ui.add_expense

import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.avengers.enterpriseexpensetracker.R
import com.avengers.enterpriseexpensetracker.adapter.ConversationAdapter
import com.avengers.enterpriseexpensetracker.modal.VoiceMessage
import java.util.*
import kotlin.collections.ArrayList

class AddExpenseFragment : Fragment() {
    private var btnVoice: AppCompatImageButton? = null
    private var conversationView: RecyclerView? = null
    private var addExpenseViewModel: AddExpenseViewModel? = null
    private var conversationAdapter: ConversationAdapter? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private var speechRecognitionListener: SpeechRecognitionListener? = null
    private var speechRecognizerIntent: Intent? = null
    private var isListening = false
    private val conversations = ArrayList<VoiceMessage>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        addExpenseViewModel = ViewModelProvider(this).get(AddExpenseViewModel::class.java)
        return inflater.inflate(R.layout.fragment_add_expense, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        conversationView = view.findViewById(R.id.conversationView)
        conversationView?.layoutManager = LinearLayoutManager(activity)
        btnVoice = view.findViewById(R.id.btnListen)
        btnVoice?.setOnClickListener {
            promptSpeechInput()
        }

        initSpeechRecognizer()
        setUpConversations()
    }

    private fun setUpConversations() {
        val conversationObserver = Observer<ArrayList<VoiceMessage>>() {
            Log.d("EETracker ****", "Inside observer")
            conversations.clear()
            if (!it.isNullOrEmpty()) {
                conversations.addAll(it)
            }
            if (conversationAdapter == null) {
                conversationAdapter = ConversationAdapter(conversations)
                conversationView?.adapter = conversationAdapter
            } else {
                conversationAdapter?.notifyDataSetChanged()
            }
        }
        addExpenseViewModel?.getConversation()?.observe(viewLifecycleOwner, conversationObserver)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        speechRecognizer?.destroy()
        speechRecognitionListener?.shutDownTTs()
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
