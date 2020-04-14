package com.avengers.enterpriseexpensetracker.ui.add_expense

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.avengers.enterpriseexpensetracker.modal.VoiceMessage

class AddExpenseViewModel : ViewModel() {
    private var conversations: ArrayList<VoiceMessage>? = null
    private var conversationLiveData = MutableLiveData<ArrayList<VoiceMessage>>()
    private var isUploadButtonEnabled = MutableLiveData<Boolean>()

    init {
        conversations = ArrayList()
        conversationLiveData.value = conversations
        isUploadButtonEnabled.value = false
    }

    fun updateConversation(message: VoiceMessage) {
        conversations?.add(message)
        conversationLiveData.value = conversations
    }

    fun getConversation(): MutableLiveData<ArrayList<VoiceMessage>> {
        return conversationLiveData
    }

    fun updateUploadButtonVisibility(enabled: Boolean) {
        isUploadButtonEnabled.value = enabled
    }

    fun getUploadButtonVisibility() : MutableLiveData<Boolean> {
        return isUploadButtonEnabled
    }
}