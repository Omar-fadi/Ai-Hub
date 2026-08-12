package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.models.ChatMessageEntity
import com.example.data.models.ChatSessionEntity
import com.example.data.models.CoinTransactionEntity
import com.example.data.models.CommunityPostEntity
import com.example.data.models.GeneratedImageEntity
import com.example.data.models.RewardTaskEntity
import com.example.data.models.UserWallet
import com.example.data.repository.AiHubRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AiHubRepository(application)

    // Wallet State
    val walletState: StateFlow<UserWallet?> = repository.walletFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserWallet())

    // Chat Sessions State
    val chatSessionsState: StateFlow<List<ChatSessionEntity>> = repository.chatSessionsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Current Session ID
    private val _currentSessionId = MutableStateFlow<Long?>(null)
    val currentSessionId: StateFlow<Long?> = _currentSessionId.asStateFlow()

    // Current Messages for selected session
    val currentMessages: StateFlow<List<ChatMessageEntity>> = _currentSessionId
        .flatMapLatest { id ->
            if (id != null) repository.getMessagesForSession(id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected Model for Chat
    private val _selectedModel = MutableStateFlow("gemini-3.5-flash")
    val selectedModel: StateFlow<String> = _selectedModel.asStateFlow()

    // Chat Sending / Loading State
    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    // Generated Images State
    val generatedImages: StateFlow<List<GeneratedImageEntity>> = repository.generatedImagesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isImageGenerating = MutableStateFlow(false)
    val isImageGenerating: StateFlow<Boolean> = _isImageGenerating.asStateFlow()

    // Vision Analysis State
    private val _isVisionLoading = MutableStateFlow(false)
    val isVisionLoading: StateFlow<Boolean> = _isVisionLoading.asStateFlow()

    private val _visionResult = MutableStateFlow<String?>(null)
    val visionResult: StateFlow<String?> = _visionResult.asStateFlow()

    // Speech-to-Text State
    private val _sttResult = MutableStateFlow<String?>(null)
    val sttResult: StateFlow<String?> = _sttResult.asStateFlow()

    private val _isSttLoading = MutableStateFlow(false)
    val isSttLoading: StateFlow<Boolean> = _isSttLoading.asStateFlow()

    // Tasks & Transactions
    val rewardTasks: StateFlow<List<RewardTaskEntity>> = repository.rewardTasksFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<CoinTransactionEntity>> = repository.transactionsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val communityPosts: StateFlow<List<CommunityPostEntity>> = repository.communityPostsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Toast / Message Events
    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent: SharedFlow<String> = _toastEvent.asSharedFlow()

    init {
        viewModelScope.launch {
            repository.initializeDatabase()
        }
    }

    fun selectModel(model: String) {
        _selectedModel.value = model
    }

    fun selectChatSession(sessionId: Long) {
        _currentSessionId.value = sessionId
    }

    fun startNewChat(title: String = "New Chat") {
        viewModelScope.launch {
            val newId = repository.createChatSession(title, _selectedModel.value)
            _currentSessionId.value = newId
        }
    }

    fun deleteChatSession(sessionId: Long) {
        viewModelScope.launch {
            repository.deleteChatSession(sessionId)
            if (_currentSessionId.value == sessionId) {
                _currentSessionId.value = null
            }
            _toastEvent.emit("Chat session deleted")
        }
    }

    fun sendChatMessage(
        messageText: String,
        imageBitmap: Bitmap? = null
    ) {
        if (messageText.isBlank() && imageBitmap == null) return

        viewModelScope.launch {
            var sessionId = _currentSessionId.value
            if (sessionId == null) {
                val title = if (messageText.length > 25) messageText.take(25) + "..." else messageText.ifEmpty { "Vision Chat" }
                sessionId = repository.createChatSession(title, _selectedModel.value)
                _currentSessionId.value = sessionId
            }

            _isChatLoading.value = true
            val result = repository.sendMessage(
                sessionId = sessionId,
                userMessage = messageText,
                modelName = _selectedModel.value,
                imageBitmap = imageBitmap
            )
            _isChatLoading.value = false

            result.onFailure {
                _toastEvent.emit(it.message ?: "Chat failed")
            }
        }
    }

    fun analyzeVisionImage(prompt: String, bitmap: Bitmap) {
        viewModelScope.launch {
            _isVisionLoading.value = true
            val result = repository.analyzeImageWithVision(prompt, bitmap)
            _isVisionLoading.value = false

            result.onSuccess { text ->
                _visionResult.value = text
                _toastEvent.emit("Vision analysis finished!")
            }.onFailure { err ->
                _toastEvent.emit(err.message ?: "Vision analysis failed")
            }
        }
    }

    fun clearVisionResult() {
        _visionResult.value = null
    }

    fun generateImage(
        prompt: String,
        style: String,
        aspectRatio: String,
        quality: String
    ) {
        if (prompt.isBlank()) return
        viewModelScope.launch {
            _isImageGenerating.value = true
            val result = repository.generateAiImage(prompt, style, aspectRatio, quality)
            _isImageGenerating.value = false

            result.onSuccess {
                _toastEvent.emit("AI Image generated successfully! ✨ (-20 Coins)")
            }.onFailure { err ->
                _toastEvent.emit(err.message ?: "Image generation failed")
            }
        }
    }

    fun transcribeAudioPrompt(audioPrompt: String) {
        viewModelScope.launch {
            _isSttLoading.value = true
            val result = repository.transcribeSpeech(audioPrompt)
            _isSttLoading.value = false

            result.onSuccess { text ->
                _sttResult.value = text
                _toastEvent.emit("Audio transcribed successfully! (-10 Coins)")
            }.onFailure { err ->
                _toastEvent.emit(err.message ?: "Transcribe failed")
            }
        }
    }

    fun clearSttResult() {
        _sttResult.value = null
    }

    fun redeemCode(code: String) {
        viewModelScope.launch {
            val (success, message) = repository.redeemCode(code)
            _toastEvent.emit(message)
        }
    }

    fun completeTask(taskId: String) {
        viewModelScope.launch {
            val (success, message) = repository.completeTask(taskId)
            _toastEvent.emit(message)
        }
    }

    fun claimDailyCheckIn() {
        viewModelScope.launch {
            val (success, message) = repository.claimDailyCheckIn()
            _toastEvent.emit(message)
        }
    }

    fun claimReferralCode(code: String) {
        viewModelScope.launch {
            val (success, message) = repository.claimReferralCode(code)
            _toastEvent.emit(message)
        }
    }

    fun buyCoinPack(coins: Int, priceLabel: String) {
        viewModelScope.launch {
            repository.addCoins(coins, coins / 2, "Purchased Coin Pack ($priceLabel)", "STORE")
            _toastEvent.emit("Successfully purchased +$coins Coins! 🎉")
        }
    }
}
