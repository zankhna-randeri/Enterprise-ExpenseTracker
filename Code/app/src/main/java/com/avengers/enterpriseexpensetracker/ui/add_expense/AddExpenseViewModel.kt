package com.avengers.enterpriseexpensetracker.ui.add_expense

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.avengers.enterpriseexpensetracker.modal.VoiceMessage

class AddExpenseViewModel : ViewModel() {
    private var conversations: ArrayList<VoiceMessage>? = null
    private var conversationLiveData = MutableLiveData<ArrayList<VoiceMessage>>()
    private var isUploadButtonEnabled = MutableLiveData<Boolean>()
    private var expenseType = MutableLiveData<String>()

    init {
        conversations = ArrayList()
        conversationLiveData.value = conversations
        isUploadButtonEnabled.value = false
        expenseType.value = null
    }

    fun updateConversation(message: VoiceMessage) {
        conversations?.add(message)
        conversationLiveData.value = conversations
    }

    fun getConversation(): MutableLiveData<ArrayList<VoiceMessage>> {
        return conversationLiveData
    }

    fun setUploadButtonVisibility(enabled: Boolean) {
        isUploadButtonEnabled.value = enabled
    }

    fun getUploadButtonVisibility(): MutableLiveData<Boolean> {
        return isUploadButtonEnabled
    }

    fun setExpenseType(expenseType: String) {
        this.expenseType.value = expenseType
    }

    fun getExpenseType(): MutableLiveData<String>? {
        return expenseType
    }
}