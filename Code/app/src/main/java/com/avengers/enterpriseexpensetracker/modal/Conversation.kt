package com.avengers.enterpriseexpensetracker.modal

class Conversation() {

    private var conversations: MutableList<VoiceMessage>? = null

    init {
        conversations = ArrayList()
    }

    fun addMessage(message: VoiceMessage) {
        if (conversations != null) {
            conversations!!.add(message)
            return
        }
        throw RuntimeException("Uninitialized conversation object !!")
    }

    fun getMessages(): MutableList<VoiceMessage>? {
        return conversations
    }
}