package com.avengers.enterpriseexpensetracker.ui.add_expense

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.avengers.enterpriseexpensetracker.modal.VoiceMessage

class AddExpenseViewModel : ViewModel() {
    private var conversations: ArrayList<VoiceMessage>? = null
    private var conversationLiveData = MutableLiveData<ArrayList<VoiceMessage>>()
    var tmp = MutableLiveData<String>()

    init {
        conversations = ArrayList()
        conversationLiveData.value = conversations
        tmp.value = "First"
    }

    fun updateConversation(message: VoiceMessage) {
        conversations?.add(message)
        conversationLiveData.value = conversations
    }

    fun getConversation(): MutableLiveData<ArrayList<VoiceMessage>> {
        return conversationLiveData
    }
}