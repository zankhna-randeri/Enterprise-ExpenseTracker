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
    private var toggleUpdateMode: Boolean = false
    private var isUploadEnabled: Boolean? = null
    private var expenseReport: ExpenseReport? = null
    private var currentExpense: Expense? = null
    private var expenses: MutableList<Expense>? = null

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

            if (toggleUpdateMode) {
                when {
                    isReqDateChange(command) -> {
                        answer = "Please provide with the changed date ? "
                    }
                    isReqAmtChange(command) -> {
                        answer = "Please provide with the changed amount ?"
                    }
                    isReqCategoryChange(command) -> {
                        answer =
                            "Please provide with the changed category ? Such as - Travel, Food, Accommodation or Other"
                    }
                    isExpenseType(command) -> {
                        toggleUpdateMode = false
                        expenseType?.let {
                            currentExpense?.setCategory(it)
                            answer = "Category is updated to $it. \n" +
                                    "Do you want to submit expense?"
                        }
                    }
                    isAmount(command) -> {
                        toggleUpdateMode = false
                        currentExpense?.setAmount(command.toFloat())
                        answer = "Amount is updated to ${currentExpense?.getAmount()}. \n" +
                                "Do you want to submit expense?"
                    }
                    isDate(command) -> {
                        toggleUpdateMode = false
                        currentExpense?.setDate(command)
                        answer = "Date is updated to ${currentExpense?.getDate()}. \n" +
                                "Do you want to submit this expense?"
                    }
                }
            } else {
                when {
                    isUserAgreed(command) -> {
                        answer = "Thank you! Your expense report will be submitted."
                    }
                    isSubmitRequest(command) -> {
                        answer =
                            "What kind of expense you want to submit? Travel, Food, Accommodation or Other."
                    }
                    isExpenseType(command) -> {
                        answer = "Ok, go ahead and upload expense receipt."
                        isUploadEnabled = true
                        isUploadEnabled?.let { (viewModel as AddExpenseViewModel).setUploadButtonVisibility(it) }
                        expenseType?.let { (viewModel as AddExpenseViewModel).setExpenseType(it) }
                        //answer = "How much amount you want to submit?"
                    }
                    isModify(command) -> {
                        toggleUpdateMode = true
                        answer = "What would you like to change - category, amount or date?"
                    }
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
                command.contains("expense", true) ||
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

    private fun isReqDateChange(command: String): Boolean {
        return command.contains("date", true)
    }

    private fun isReqAmtChange(command: String): Boolean {
        return command.contains("amount", true) ||
                command.contains("charge", true)
    }

    private fun isReqCategoryChange(command: String): Boolean {
        return command.contains("category", true) ||
                command.contains("type", true)
    }

    private fun isUserAgreed(command: String): Boolean {
        return command.contains("yes", true) ||
                command.contains("go ahead", true) ||
                command.contains("yaa", true) ||
                command.contains("confirmed", true) ||
                command.contains("yup", true) ||
                command.contains("confirm", true)
    }

    private fun isAmount(command: String): Boolean {
        return command.contains("dollar", true) ||
                command.contains("dollars", true) ||
                command.contains("$", true) ||
                command.contains("cent", true) ||
                command.contains("cents", true)
    }

    private fun isDate(command: String): Boolean {
        return command.contains("January", true) ||
                command.contains("February", true) ||
                command.contains("march", true) ||
                command.contains("april", true) ||
                command.contains("may", true) ||
                command.contains("june", true) ||
                command.contains("july", true) ||
                command.contains("august", true) ||
                command.contains("september", true) ||
                command.contains("october", true) ||
                command.contains("november", true) ||
                command.contains("december", true)
    }

    fun updateCurrentExpense(receiptScanResponse: ReceiptScanResponse) {
        //hide upload button
        (viewModel as AddExpenseViewModel).setUploadButtonVisibility(false)

        currentExpense?.setAmount(receiptScanResponse.getTotal())
        currentExpense?.setDate(receiptScanResponse.getExpenseDate())
        val answer = "Your expense details after receipt scan are as below: \n" +
                "Expense Amount : ${currentExpense?.getAmount()} $\n" +
                "Expense Date : ${currentExpense?.getDate()} \n" +
                "Do you want to submit these details?"

        val response = VoiceMessage(answer, true)
        (viewModel as AddExpenseViewModel).updateConversation(response)
        tts?.speak(answer, TextToSpeech.QUEUE_FLUSH, null, UTTERANCE_ID)
    }
}