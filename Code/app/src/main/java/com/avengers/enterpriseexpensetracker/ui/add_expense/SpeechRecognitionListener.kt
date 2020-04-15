package com.avengers.enterpriseexpensetracker.ui.add_expense

import android.content.Context
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.lifecycle.ViewModel
import com.avengers.enterpriseexpensetracker.R
import com.avengers.enterpriseexpensetracker.modal.Expense
import com.avengers.enterpriseexpensetracker.modal.ExpenseReport
import com.avengers.enterpriseexpensetracker.modal.VoiceMessage
import com.avengers.enterpriseexpensetracker.modal.response.ReceiptScanResponse
import com.avengers.enterpriseexpensetracker.util.Constants
import com.avengers.enterpriseexpensetracker.util.Utility
import java.util.*
import kotlin.collections.ArrayList

class SpeechRecognitionListener(private var context: Context?,
                                private var viewModel: ViewModel?) : RecognitionListener {
    private var tts: TextToSpeech? = null
    private var expenseType: String? = null
    private var amount: Int? = -1
    private var date: String? = null
    private var isUploadEnabled: Boolean? = null
    private var expenseReport: ExpenseReport? = null
    private var currentExpense: Expense? = null
    private var expenses: List<Expense>? = null

    init {
        isUploadEnabled = false
        expenseReport = ExpenseReport()
        expenses = ArrayList()
        currentExpense = Expense()
        initTTS()
    }

    companion object {
        private const val UTTERANCE_ID = "1234"
    }

    fun setUploadEnabled(enabled: Boolean?) {
        this.isUploadEnabled = enabled
    }

    private fun initTTS() {
        tts = TextToSpeech(context, TextToSpeech.OnInitListener { status ->
            if (status == TextToSpeech.SUCCESS) {
                val ttsLang = tts?.setLanguage(Locale.US)
                if (ttsLang == TextToSpeech.LANG_MISSING_DATA ||
                    ttsLang == TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE) {
                    Log.e("Error: %s", "Language not supported !")
                } else {
                    Log.i("Success: %s", "Language Supported");
                }
            } else {
                context?.let {
                    Utility.getInstance().showMsg(it, context?.resources?.getString(R.string.tts_init_failed))
                }
            }
        })
    }

    fun shutDownTTs() {
        tts?.stop()
        tts?.shutdown()
    }

    override fun onReadyForSpeech(params: Bundle?) {
    }

    override fun onRmsChanged(rmsdB: Float) {
    }

    override fun onBufferReceived(buffer: ByteArray?) {
    }

    override fun onPartialResults(partialResults: Bundle?) {
    }

    override fun onEvent(eventType: Int, params: Bundle?) {
    }

    override fun onBeginningOfSpeech() {
    }

    override fun onEndOfSpeech() {
    }

    override fun onError(error: Int) {
        Log.e("EETracker ****** ", error.toString())
    }

    override fun onResults(results: Bundle?) {
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        Log.d("Speech output: ", matches.toString())
        processUserRequest(matches?.get(0))
    }

    private fun processUserRequest(command: String?) {
        var answer = "Sorry I didn't get you!"

        if (!command.isNullOrBlank()) {
            if (viewModel != null) {
                val request = VoiceMessage(command, false)
                (viewModel as AddExpenseViewModel).updateConversation(request)
            }
            when {
                isUserAgreed(command) -> {
                    answer = "Thank you! Your expense report will be submitted."
                }
                isSubmitRequest(command) -> {
                    answer =
                        "What kind of expense you want to submit? Travel, Food or Other."
                }
                isExpenseType(command) -> {
                    answer = "Ok, go ahead and upload expense receipt."
                    isUploadEnabled = true
                    isUploadEnabled?.let { (viewModel as AddExpenseViewModel).setUploadButtonVisibility(it) }
                    expenseType?.let { (viewModel as AddExpenseViewModel).setExpenseType(it) }
                    //answer = "How much amount you want to submit?"
                }
                isAmount(command) -> {
                    answer = "Yes, go ahead."
                }
                isModify(command) -> {

                }
            }
        }

        val response = VoiceMessage(answer, true)
        (viewModel as AddExpenseViewModel).updateConversation(response)
        tts?.speak(answer, TextToSpeech.QUEUE_FLUSH, null, UTTERANCE_ID)
    }

    private fun isSubmitRequest(command: String): Boolean {
        return command.contains("submit expenses", true) ||
                command.contains("submit my expenses", true) ||
                command.contains("submit expense", true) ||
                command.contains("submit my expense", true) ||
                command.contains("expenses", true) ||
                command.contains("submit", true)
    }

    private fun isExpenseType(command: String): Boolean {
        expenseType = fetchExpenseType(command)
        return !expenseType.isNullOrBlank()
    }

    private fun fetchExpenseType(command: String): String? {
        for (type in Constants.Companion.ExpenseType.values()) {
            if (command.equals(type.name, true)) {
                return type.name
            }
        }

        return null
    }

    private fun isModify(command: String): Boolean {
        return command.contains("modify", true) ||
                command.contains("update", true) ||
                command.contains("edit", true) ||
                command.contains("change", true)
    }

    private fun isUserAgreed(command: String): Boolean {
        return command.contains("yes", true) || command.contains("go ahead", true)
    }

    private fun isAmount(command: String): Boolean {
        return command.contains("dollar", false) ||
                command.contains("dollars", false) ||
                command.contains("$", false)
    }

    fun updateCurrentExpense(receiptScanResponse: ReceiptScanResponse) {
        //hide upload button
        (viewModel as AddExpenseViewModel).setUploadButtonVisibility(false)

        currentExpense?.setAmount(receiptScanResponse.getTotal())
        currentExpense?.setDate(receiptScanResponse.getExpenseDate())
        val answer = "Your expense details after receipt scan is as below: \n " +
                "Expense Amount : ${currentExpense?.getAmount()} \n" +
                "Expense Date : ${currentExpense?.getDate()} \n" +
                "Do you want to submit these details?"

        val response = VoiceMessage(answer, true)
        (viewModel as AddExpenseViewModel).updateConversation(response)
        tts?.speak(answer, TextToSpeech.QUEUE_FLUSH, null, UTTERANCE_ID)
    }
}