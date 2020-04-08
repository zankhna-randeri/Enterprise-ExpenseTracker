package com.avengers.enterpriseexpensetracker.modal

data class VoiceMessage(private var text: String? = "",
                        private var isResponse: Boolean = false)