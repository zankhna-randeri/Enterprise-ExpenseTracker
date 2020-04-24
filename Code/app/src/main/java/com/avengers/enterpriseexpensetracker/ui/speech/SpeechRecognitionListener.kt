package com.avengers.enterpriseexpensetracker.ui.speech

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
import com.avengers.enterpriseexpensetracker.util.EETrackerPreferenceManager
import com.avengers.enterpriseexpensetracker.util.Utility
import com.avengers.enterpriseexpensetracker.viewmodel.AddExpenseViewModel
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
        Confirmation
    }
//        Multiple

    init {
        expenses = ArrayList()
        currentMode =
            VoiceBotMode.Normal
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
        Log.e("EETracker *******", error.toString())
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
                        currentMode =
                            VoiceBotMode.Normal
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
                    isReqBusinessNameChange(command) -> {
                        answer =
                            "Please provide with the changed business name?"
                    }
                    isExpenseType(command) -> {
                        currentMode =
                            VoiceBotMode.Normal
                        expenseType = fetchExpenseType(command)
                        expenseType?.let {
                            currentExpense?.let { updatedExpense ->
                                updatedExpense.setCategory(it)
                                answer = formUpdateAnswer(updatedExpense)
                                currentMode =
                                    VoiceBotMode.Confirmation
                            }
                        }
                    }
                    isAmount(command) -> {
                        val changedAmt = getCurrencyAmount(command)
                        if (changedAmt != null && !changedAmt.isNaN()) {
                            currentMode =
                                VoiceBotMode.Normal
                            currentExpense?.let { updatedExpense ->
                                updatedExpense.setAmount(changedAmt)
                                answer = formUpdateAnswer(updatedExpense)
                                currentMode =
                                    VoiceBotMode.Confirmation
                            }
                        } else {
                            answer = "Invalid amount."
                        }
                    }
                    isDate(command) -> {
                        val changedDate = EETrackerDateFormatManager().parseDate(command)
                        if (!changedDate.isNullOrBlank()) {
                            currentMode =
                                VoiceBotMode.Normal
                            currentExpense?.let { updatedExpense ->
                                updatedExpense.setDate(changedDate)
                                answer = formUpdateAnswer(updatedExpense)
                                currentMode =
                                    VoiceBotMode.Confirmation
                            }
                        } else {
                            answer = "Invalid date."
                        }
                    }
                }
            } else {
                // Handle normal flow
                when {
                    (currentMode == VoiceBotMode.Name) -> {
                        expenseReport?.setName(command)
                        currentMode =
                            VoiceBotMode.Normal

                        answer =
                            "Please provide us with Expense Category such as Food, Travel, Accommodation or Other."
                    }
                    isDiscard(command) -> {
                        // reset all data if changes are discarded.
                        answer = "Your all changes are discarded. Your report is not submitted."
                        reset()
                    }
                    isSubmitReportRequest(command) -> {
                        // Submit Report is triggered

                        if (currentMode == VoiceBotMode.Confirmation && expenseReport != null) {
                            // invoke api if "submit report" is triggered as part of user confirmation
                            // save current expense in list
                            currentExpense?.let { expenses?.add(it) }

                            submitReport()
                            answer = "Thank you! Your expense report will be submitted."

                            // reset everything
                            reset()
                        } else if (expenseReport == null) {
                            // first initialization of expense report
                            expenseReport = ExpenseReport()

                            currentMode =
                                VoiceBotMode.Name
                            answer = "Sure, please say your Report Name?"
                        } else {
                            // ask for discard in case if current report is already in progress and
                            // submit report is triggered.
                            answer = "You already have current expense report in progress. " +
                                    "Do you want to discard all changes or continue?"
                        }
                    }
                    isSubmitExpenseRequest(command) -> {
                        // save current expense in list
                        currentExpense?.let { expenses?.add(it) }

                        // reset expense data if adding multiple expenses.
                        currentExpense = null
                        currentMode == VoiceBotMode.Normal

                        answer =
                            "Please provide us with Expense Category such as Food, Travel, Accommodation or Other."
                    }
                    isExpenseType(command) -> {
                        // initialize current expense
                        currentExpense = Expense()
                        expenseType = fetchExpenseType(command)
                        /*answer = "You have chosen category as $expenseType."
                        currentMode = VoiceBotMode.Name*/
                        answer = "You have chosen category as ${expenseType}. " +
                                "Please upload your receipt for auto scanning.\n"
                        (viewModel as AddExpenseViewModel).setUploadButtonVisibility(true)
                        expenseType?.let { (viewModel as AddExpenseViewModel).setExpenseType(it) }
                    }
                    isModify(command) -> {
                        // TODO: verify whether receipt scan already happened before user tries to modify details.
                        currentMode =
                            VoiceBotMode.Update
                        answer = "What would you like to change? \n" +
                                "Category, amount or date?"
                    }
                }
            }
        }

        val response = VoiceMessage(answer, true)
        (viewModel as AddExpenseViewModel).updateConversation(response)
        tts?.speak(answer, TextToSpeech.QUEUE_FLUSH, null,
                UTTERANCE_ID)
    }

    private fun formUpdateAnswer(expense: Expense): String {
        var answer = "Response has been changed with Category as "

        // append sub category if it is not null
        expense.getSubCategory()?.let {
            answer += "${expense.getSubCategory()} in "
        }

        // append category
        answer += "${expense.getCategory()} "

        // only append business name if it is not null
        expense.getBusinessName()?.let {
            answer += "at ${expense.getBusinessName()}, "
        }

        // append remaining answer
        answer += "amount as $${expense.getAmount()} and date as ${expense.getDate()}. \n" +
                "Do you want to submit the report, or add more expenses?"
        return answer
    }

    private fun submitReport() {
        expenses?.let { expenseReport?.setExpenses(it) }
        expenseReport?.let { (viewModel as AddExpenseViewModel).setExpenseReport(it) }
    }

    private fun isDeny(command: String): Boolean {
        return command.contains("no", true) ||
                command.contains("nope", true) ||
                command.contains("not yet", true) ||
                command.contains("not", true)
    }

    private fun isDiscard(command: String): Boolean {
        return command.contains("discard", true) || isDeny(command)
    }

    private fun getCurrencyAmount(command: String): Double? {
        return NumberFormat.getCurrencyInstance(Locale.US).parse(command)?.toDouble()
    }

    private fun isSubmitReportRequest(command: String): Boolean {
        return command.contains("report", true) ||
                command.contains("create report", true) ||
                command.contains("expense report", true) ||
                command.contains("submit expense report", true) ||
                command.contains("create expense report", true)
//                isUserAgreed(command)

    }

    private fun isSubmitExpenseRequest(command: String): Boolean {
        return command.contains("add expense", true) ||
                command.contains("add more expense", true) ||
                command.contains("add more expenses", true)

//        command.contains("submit expense", true) ||
//                command.contains("expenses", true) ||
//                command.contains("expense", true) ||
//                isUserAgreed(command)

    }

    private fun isExpenseType(command: String): Boolean {
        for (type in Constants.Companion.ExpenseType.values()) {
            if (command.equals(type.name, true)) {
                return true
            }
        }

        return false
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

    private fun isReqBusinessNameChange(command: String): Boolean {
        return command.contains("business name", true) ||
                command.contains("businessname", true) ||
                command.contains("name", true)
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

    private fun reset() {
        currentMode =
            VoiceBotMode.Normal
        expenseReport = null
        expenses = ArrayList()
        currentExpense = null
    }

    fun updateVoiceBotWithScanResponse(receiptScanResponse: ReceiptScanResponse) {
        //hide upload button
        (viewModel as AddExpenseViewModel).setUploadButtonVisibility(false)

        // update all latest fields from API response
        currentExpense = updateCurrentExpense(receiptScanResponse)

        var answer = "Receipt Scanned Details: Category as "

        // append subcategory if category is food and subcategory is not null
        currentExpense?.getSubCategory()?.let { subCategory ->
            answer += "$subCategory in "
        }

        answer += "${currentExpense?.getCategory()?.let { it }}, "

        // append businessname only if it is not null
        currentExpense?.getBusinessName()?.let {
            answer += "at ${currentExpense?.getBusinessName()}, "
        }

        answer = answer + "Amount as $${currentExpense?.getAmount()} and " +
                "Date as ${currentExpense?.getDate()} \n" +
                "Do you want to submit the report, or add more expenses?"

        currentMode =
            VoiceBotMode.Confirmation
        val response = VoiceMessage(answer, true)
        (viewModel as AddExpenseViewModel).updateConversation(response)
        tts?.speak(answer, TextToSpeech.QUEUE_FLUSH, null,
                UTTERANCE_ID)
    }

    private fun updateCurrentExpense(receiptScanResponse: ReceiptScanResponse): Expense? {
        return Expense(EETrackerPreferenceManager.getUserEmail(context),
                receiptScanResponse.getBusinessName(),
                receiptScanResponse.getBusinessAddress(),
                receiptScanResponse.getCategory(),
                receiptScanResponse.getSubCategory(),
                receiptScanResponse.getTotal(),
                receiptScanResponse.getExpenseDate(),
                receiptScanResponse.getExpenseTime(), receiptScanResponse.getReceiptUrl())
    }
}