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
import com.avengers.enterpriseexpensetracker.util.EETrackerDateFormatManager
import com.avengers.enterpriseexpensetracker.util.Utility
import java.text.NumberFormat
import java.util.*
import kotlin.collections.ArrayList

class SpeechRecognitionListener(private var context: Context?,
                                private var viewModel: ViewModel?) : RecognitionListener {
    private var tts: TextToSpeech? = null
    private var expenseType: String? = null
    private var expenseReport: ExpenseReport? = null
    private var currentExpense: Expense? = null
    private var expenses: MutableList<Expense>? = null

    // variable to hold current voice chatbot mode
    private var currentMode: VoiceBotMode

    // Enum for various chatbot mode
    enum class VoiceBotMode {
        Normal,
        Update,
        Name,
        Multiple
    }

    init {
        expenses = ArrayList()
        currentMode = VoiceBotMode.Normal
        initTTS()
    }

    companion object {
        private const val UTTERANCE_ID = "1234"
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
        var answer = "Sorry, I didn't get you!"

        if (!command.isNullOrBlank()) {
            if (viewModel != null) {
                val request = VoiceMessage(command, false)
                (viewModel as AddExpenseViewModel).updateConversation(request)
            }

            if (currentMode == VoiceBotMode.Update) {
                // Handle update mode
                when {
                    isDeny(command) -> {
                        // TODO: Handle update/modify deny
                        currentMode = VoiceBotMode.Normal
                    }
                    isReqDateChange(command) -> {
                        answer = "Please provide with the changed date? "
                    }
                    isReqAmtChange(command) -> {
                        answer = "Please provide with the changed amount?"
                    }
                    isReqCategoryChange(command) -> {
                        answer =
                            "Please provide with the changed category? Such as - Travel, Food, Accommodation or Other"
                    }
                    isExpenseType(command) -> {
                        currentMode = VoiceBotMode.Normal
                        expenseType?.let {
                            currentExpense?.setCategory(it)
                            answer = "Category is updated to $it. \n" +
                                    "Do you want to submit expense?"
                        }
                    }
                    isAmount(command) -> {
                        val changedAmt = getCurrencyAmount(command)
                        answer = if (changedAmt != null && !changedAmt.isNaN()) {
                            currentMode = VoiceBotMode.Normal
                            currentExpense?.setAmount(changedAmt)
                            "Amount is updated to $${currentExpense?.getAmount()}. \n" +
                                    "Do you want to submit expense?"
                        } else {
                            "Invalid amount."
                        }
                    }
                    isDate(command) -> {
                        val changedDate = EETrackerDateFormatManager().parseDate(command)
                        answer = if (!changedDate.isNullOrBlank()) {
                            currentMode = VoiceBotMode.Normal
                            currentExpense?.setDate(changedDate)
                            "Date is updated to ${currentExpense?.getDate()}. \n" + "Do you want to submit this expense?"
                        } else {
                            "Invalid date."
                        }
                    }
                }
            } else {
                // Handle normal flow
                when {
                    isDiscard(command) -> {
                        // reset all data if changes are discarded.
                        answer = "Your all changes are discarded. Your report is not submitted."
                        expenseReport = null
                        currentExpense = null
                    }
                    isUserAgreed(command) -> {
                        currentExpense?.let { expenses?.add(it) }
                        answer = "Do you want to add more expenses or submit report?"
                        currentMode = VoiceBotMode.Multiple
                    }
                    isSubmitReportRequest(command) -> {
                        // Submit Report is triggered

                        if (currentMode == VoiceBotMode.Multiple && expenseReport != null &&
                            !expenses.isNullOrEmpty()) {
                            // invoke api if "submit report" is triggered as part of user confirmation
                            submitReport(expenses!!, expenseReport!!)
                            answer = "Thank you! Your expense report will be submitted."
                            currentMode = VoiceBotMode.Normal
                        } else if (expenseReport == null) {
                            // first initialization of expense report
                            expenseReport = ExpenseReport()
                            answer = "Sure, please say your Report Name?"
                            currentMode = VoiceBotMode.Name
                        } else {
                            // ask for discard in case if current report is already in progress and
                            // submit report is triggered.
                            answer = "You already have current expense report in progress. " +
                                    "Do you want to discard all changes or continue?"
                        }
                    }
                    isSubmitExpenseRequest(command) -> {
                        // reset expense data if adding multiple expenses.
                        if (currentMode == VoiceBotMode.Multiple) {
                            currentMode = VoiceBotMode.Normal
                            currentExpense = null
                        }

                        // initialize current expense
                        currentExpense = Expense()
                        answer =
                            "What kind of expense you want to submit? Travel, Food, Accommodation or Other."
                    }
                    isExpenseType(command) -> {
                        answer = "Go ahead and upload expense receipt for auto-processing."
                        (viewModel as AddExpenseViewModel).setUploadButtonVisibility(true)
                        expenseType?.let { (viewModel as AddExpenseViewModel).setExpenseType(it) }
                    }
                    isModify(command) -> {
                        // TODO: verify whether receipt scan already happened before user tries to modify details.
                        currentMode = VoiceBotMode.Update
                        answer = "What would you like to change? \n" +
                                "Category, amount or date?"
                    }
                    (currentMode == VoiceBotMode.Name) -> {
                        expenseReport?.setName(command)
                        currentMode = VoiceBotMode.Normal
                        answer =
                            "What kind of expense you want to submit? Travel, Food, Accommodation or Other."
                    }
                }
            }
        }

        val response = VoiceMessage(answer, true)
        (viewModel as AddExpenseViewModel).updateConversation(response)
        tts?.speak(answer, TextToSpeech.QUEUE_FLUSH, null, UTTERANCE_ID)
    }

    private fun submitReport(expenses: MutableList<Expense>, expenseReport: ExpenseReport) {
        expenseReport.setExpenses(expenses)
        (viewModel as AddExpenseViewModel).setExpenseReport(expenseReport)
    }

    private fun isDeny(command: String): Boolean {
        return command.contains("no", true) ||
                command.contains("nope", true) ||
                command.contains("not yet", true) ||
                command.contains("not", true)
    }

    private fun isDiscard(command: String): Boolean {
        return command.contains("discard", true)
    }

    private fun getCurrencyAmount(command: String): Float? {
        return NumberFormat.getCurrencyInstance(Locale.US).parse(command)?.toFloat()
    }

    private fun isSubmitReportRequest(command: String): Boolean {
        return command.contains("report", true) ||
                command.contains("expense report", true) ||
                command.contains("submit expense report", true)
    }

    private fun isSubmitExpenseRequest(command: String): Boolean {
        return command.contains("submit expense", true) ||
                command.contains("add expense", true) ||
                command.contains("add more expense", true) ||
                command.contains("add more expenses", true) ||
                command.contains("expenses", true) ||
                command.contains("expense", true)
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
        receiptScanResponse.getExpenseDate()?.let { currentExpense?.setDate(it) }
        val answer = "Your expense details after receipt scan are as below: \n" +
                "Expense Category: ${currentExpense?.getCategory()?.let { it }}" +
                "Expense Amount : $${currentExpense?.getAmount()}\n" +
                "Expense Date : ${currentExpense?.getDate()} \n" +
                "Do you want to submit these details?"

        val response = VoiceMessage(answer, true)
        (viewModel as AddExpenseViewModel).updateConversation(response)
        tts?.speak(answer, TextToSpeech.QUEUE_FLUSH, null, UTTERANCE_ID)
    }
}