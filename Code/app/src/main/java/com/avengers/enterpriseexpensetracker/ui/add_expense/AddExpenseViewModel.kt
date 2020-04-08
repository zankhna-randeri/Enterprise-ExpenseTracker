package com.avengers.enterpriseexpensetracker.ui.add_expense

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.avengers.enterpriseexpensetracker.modal.VoiceMessage

class AddExpenseViewModel : ViewModel() {
    private val conversations = MutableLiveData<MutableList<VoiceMessage>>()

    fun updateConversation(message: VoiceMessage) {
        if (message.isResponse) {
            conversations.value?.add(message)
        }
    }

    fun getConversation(): MutableLiveData<MutableList<VoiceMessage>> {
        return conversations
    }
}