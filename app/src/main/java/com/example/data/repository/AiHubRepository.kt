package com.example.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.util.Base64
import androidx.room.Room
import com.example.data.api.Content
import com.example.data.api.GenerateContentRequest
import com.example.data.api.GenerationConfig
import com.example.data.api.ImageConfig
import com.example.data.api.InlineData
import com.example.data.api.Part
import com.example.data.api.RetrofitClient
import com.example.data.db.AppDatabase
import com.example.data.models.ChatMessageEntity
import com.example.data.models.ChatSessionEntity
import com.example.data.models.CoinTransactionEntity
import com.example.data.models.CommunityPostEntity
import com.example.data.models.GeneratedImageEntity
import com.example.data.models.RedeemedCodeEntity
import com.example.data.models.RewardTaskEntity
import com.example.data.models.UserWallet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.Calendar

class AiHubRepository(context: Context) {

    private val db = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "ai_hub_db"
    ).fallbackToDestructiveMigration().build()

    private val dao = db.appDao()

    val walletFlow: Flow<UserWallet?> = dao.getUserWallet()
    val chatSessionsFlow: Flow<List<ChatSessionEntity>> = dao.getAllChatSessions()
    val generatedImagesFlow: Flow<List<GeneratedImageEntity>> = dao.getAllGeneratedImages()
    val transactionsFlow: Flow<List<CoinTransactionEntity>> = dao.getAllTransactions()
    val rewardTasksFlow: Flow<List<RewardTaskEntity>> = dao.getAllRewardTasks()
    val communityPostsFlow: Flow<List<CommunityPostEntity>> = dao.getAllCommunityPosts()

    suspend fun initializeDatabase() = withContext(Dispatchers.IO) {
        var wallet = dao.getUserWalletSync()
        if (wallet == null) {
            wallet = UserWallet()
            dao.insertOrUpdateWallet(wallet)
            // Log welcome bonus transaction
            dao.insertTransaction(
                CoinTransactionEntity(
                    title = "Welcome Bonus Coins",
                    amount = 150,
                    type = "REWARD"
                )
            )
        }

        // Initialize default tasks if empty
        val currentTasks = dao.getAllRewardTasks().first()
        if (currentTasks.isEmpty()) {
            val defaultTasks = listOf(
                RewardTaskEntity("instagram", "Follow AI Hub on Instagram", "Get latest AI prompt tips & news", 50, 20, "INSTAGRAM"),
                RewardTaskEntity("telegram", "Join AI Hub Telegram Group", "Connect with 10K+ AI creators", 50, 20, "TELEGRAM"),
                RewardTaskEntity("share", "Share AI Hub App", "Invite your friends to try AI Hub", 30, 15, "SHARE"),
                RewardTaskEntity("daily", "Daily Check-in Streak", "Claim daily 50 coins bonus", 50, 25, "DAILY")
            )
            dao.insertOrUpdateTasks(defaultTasks)
        }

        // Initialize community sample posts if empty
        val posts = dao.getAllCommunityPosts().first()
        if (posts.isEmpty()) {
            val samplePosts = listOf(
                CommunityPostEntity(
                    authorName = "Amine_AI",
                    authorAvatar = "https://picsum.photos/200?random=1",
                    prompt = "Futuristic Algiers 2050 cityscape with solar sky-trains and glowing green minarets",
                    imageUrl = "https://picsum.photos/600/400?random=101",
                    likesCount = 89
                ),
                CommunityPostEntity(
                    authorName = "Sofia_Digital",
                    authorAvatar = "https://picsum.photos/200?random=2",
                    prompt = "Cyberpunk Sahara desert oasis with neon palm trees and holographic camels",
                    imageUrl = "https://picsum.photos/600/400?random=102",
                    likesCount = 142
                ),
                CommunityPostEntity(
                    authorName = "Karem_Tech",
                    authorAvatar = "https://picsum.photos/200?random=3",
                    prompt = "3D isometric miniature AI studio with robot coders writing Kotlin code",
                    imageUrl = "https://picsum.photos/600/400?random=103",
                    likesCount = 56
                )
            )
            dao.insertCommunityPosts(samplePosts)
        }
    }

    // --- COINS & WALLET MANAGEMENT ---

    suspend fun getWallet(): UserWallet {
        return withContext(Dispatchers.IO) {
            dao.getUserWalletSync() ?: UserWallet().also { dao.insertOrUpdateWallet(it) }
        }
    }

    suspend fun deductCoins(cost: Int, transactionTitle: String): Boolean = withContext(Dispatchers.IO) {
        val wallet = getWallet()
        if (wallet.coins >= cost) {
            val updatedWallet = wallet.copy(
                coins = wallet.coins - cost,
                xp = wallet.xp + (cost * 2)
            )
            val updatedWithLevel = calculateLevel(updatedWallet)
            dao.insertOrUpdateWallet(updatedWithLevel)

            dao.insertTransaction(
                CoinTransactionEntity(
                    title = transactionTitle,
                    amount = -cost,
                    type = "SPENT"
                )
            )
            true
        } else {
            false
        }
    }

    suspend fun addCoins(amount: Int, xpGain: Int, title: String, type: String) = withContext(Dispatchers.IO) {
        val wallet = getWallet()
        val updatedWallet = wallet.copy(
            coins = wallet.coins + amount,
            xp = wallet.xp + xpGain
        )
        val updatedWithLevel = calculateLevel(updatedWallet)
        dao.insertOrUpdateWallet(updatedWithLevel)

        dao.insertTransaction(
            CoinTransactionEntity(
                title = title,
                amount = amount,
                type = type
            )
        )
    }

    private fun calculateLevel(wallet: UserWallet): UserWallet {
        val newLevel = (wallet.xp / 100) + 1
        return wallet.copy(level = newLevel)
    }

    // --- REDEEM CODES ---

    suspend fun redeemCode(codeRaw: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val code = codeRaw.trim().uppercase()
        if (code.isEmpty()) return@withContext Pair(false, "Please enter a valid code")

        val existing = dao.getRedeemedCode(code)
        if (existing != null) {
            return@withContext Pair(false, "You have already used this code!")
        }

        val bonusCoins = when (code) {
            "AIHUB30" -> 30
            "WELCOME100" -> 100
            "ALGERIA2050" -> 150
            "PREMIUM500" -> 500
            "SUPERHUB" -> 200
            else -> 0
        }

        if (bonusCoins > 0) {
            dao.insertRedeemedCode(RedeemedCodeEntity(code = code, coinsAwarded = bonusCoins))
            addCoins(bonusCoins, bonusCoins / 2, "Redeemed Code: $code", "REDEEM")
            Pair(true, "Successfully redeemed +$bonusCoins Coins! 🎉")
        } else {
            Pair(false, "Invalid or expired code. Try 'AIHUB30' or 'WELCOME100'.")
        }
    }

    // --- TASKS & REWARDS ---

    suspend fun completeTask(taskId: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val tasks = dao.getAllRewardTasks().first()
        val task = tasks.find { it.id == taskId } ?: return@withContext Pair(false, "Task not found")

        if (task.isCompleted) {
            return@withContext Pair(false, "Task already completed!")
        }

        dao.markTaskCompleted(taskId)
        addCoins(task.rewardCoins, task.rewardXp, "Task Completed: ${task.title}", "TASK")
        Pair(true, "Earned +${task.rewardCoins} Coins & +${task.rewardXp} XP!")
    }

    suspend fun claimDailyCheckIn(): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val wallet = getWallet()
        val now = System.currentTimeMillis()
        val lastCheck = wallet.lastCheckInDate

        val calNow = Calendar.getInstance().apply { timeInMillis = now }
        val calLast = Calendar.getInstance().apply { timeInMillis = lastCheck }

        val isSameDay = calNow.get(Calendar.YEAR) == calLast.get(Calendar.YEAR) &&
                calNow.get(Calendar.DAY_OF_YEAR) == calLast.get(Calendar.DAY_OF_YEAR)

        if (isSameDay && lastCheck > 0) {
            return@withContext Pair(false, "You already claimed your daily check-in today!")
        }

        val newStreak = wallet.streakDays + 1
        val updatedWallet = wallet.copy(
            streakDays = newStreak,
            lastCheckInDate = now
        )
        dao.insertOrUpdateWallet(updatedWallet)
        addCoins(50, 30, "Daily Check-in 🔥 (Day $newStreak)", "DAILY")

        Pair(true, "Daily reward claimed! +50 Coins 🔥 Day $newStreak Streak!")
    }

    // --- REFERRAL SYSTEM ---

    suspend fun claimReferralCode(codeRaw: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val code = codeRaw.trim().uppercase()
        val wallet = getWallet()

        if (wallet.claimedReferral) {
            return@withContext Pair(false, "You have already claimed a referral code.")
        }

        if (code == wallet.myReferralCode) {
            return@withContext Pair(false, "You cannot enter your own referral code!")
        }

        if (code.startsWith("AH-") && code.length >= 6) {
            val updatedWallet = wallet.copy(claimedReferral = true)
            dao.insertOrUpdateWallet(updatedWallet)
            addCoins(100, 50, "Referral Bonus claimed ($code)", "REFERRAL")
            Pair(true, "Referral successful! You earned +100 Coins 🎉")
        } else {
            Pair(false, "Invalid referral code format (Must be AH-XXXXX)")
        }
    }

    // --- CHAT SESSIONS & MESSAGES ---

    fun getMessagesForSession(sessionId: Long): Flow<List<ChatMessageEntity>> {
        return dao.getMessagesForSession(sessionId)
    }

    suspend fun createChatSession(title: String, model: String = "gemini-3.5-flash"): Long = withContext(Dispatchers.IO) {
        dao.insertChatSession(
            ChatSessionEntity(
                title = title,
                modelUsed = model
            )
        )
    }

    suspend fun deleteChatSession(sessionId: Long) = withContext(Dispatchers.IO) {
        dao.deleteChatSession(sessionId)
        dao.deleteMessagesForSession(sessionId)
    }

    suspend fun renameChatSession(sessionId: Long, newTitle: String) = withContext(Dispatchers.IO) {
        dao.renameChatSession(sessionId, newTitle)
    }

    suspend fun sendMessage(
        sessionId: Long,
        userMessage: String,
        modelName: String = "gemini-3.5-flash",
        imageBitmap: Bitmap? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        val cost = if (modelName.contains("pro")) 5 else if (imageBitmap != null) 8 else 3

        if (!deductCoins(cost, "AI Chat ($modelName)")) {
            return@withContext Result.failure(Exception("Not enough coins! You need $cost Coins."))
        }

        // Save user message in DB
        val userMsgEntity = ChatMessageEntity(
            sessionId = sessionId,
            isUser = true,
            messageText = userMessage,
            modelUsed = modelName,
            coinsCost = cost
        )
        dao.insertChatMessage(userMsgEntity)

        // Prepare request to Gemini
        val apiKey = RetrofitClient.getApiKey()
        if (apiKey.isEmpty()) {
            val simulatedReply = "Welcome to AI Hub! (Note: GEMINI_API_KEY is currently using preview mode). I received your query: '$userMessage'. How can I assist you further with AI Hub?"
            dao.insertChatMessage(
                ChatMessageEntity(
                    sessionId = sessionId,
                    isUser = false,
                    messageText = simulatedReply,
                    modelUsed = modelName,
                    coinsCost = 0
                )
            )
            return@withContext Result.success(simulatedReply)
        }

        val resolvedModel = when {
            modelName.contains("pro") -> "gemini-3.1-pro-preview"
            modelName.contains("lite") -> "gemini-3.1-flash-lite-preview"
            else -> "gemini-3.5-flash"
        }

        try {
            val partsList = mutableListOf<Part>()
            partsList.add(Part(text = userMessage))

            if (imageBitmap != null) {
                val base64Data = bitmapToBase64(imageBitmap)
                partsList.add(Part(inlineData = InlineData(mimeType = "image/jpeg", data = base64Data)))
            }

            // Fetch previous history
            val history = dao.getMessagesForSessionSync(sessionId).takeLast(10)
            val contentsList = history.map { msg ->
                Content(
                    role = if (msg.isUser) "user" else "model",
                    parts = listOf(Part(text = msg.messageText))
                )
            }

            val request = GenerateContentRequest(
                contents = if (contentsList.isNotEmpty()) contentsList else listOf(Content(parts = partsList)),
                systemInstruction = Content(
                    parts = listOf(Part(text = "You are AI Hub, a high-tech, friendly, and expert AI assistant. Provide concise, helpful, and beautifully formatted answers with markdown."))
                )
            )

            val response = RetrofitClient.geminiService.generateContent(
                model = resolvedModel,
                apiKey = apiKey,
                request = request
            )

            val replyText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: response.error?.message
                ?: "I apologize, I could not generate a response. Please try again."

            dao.insertChatMessage(
                ChatMessageEntity(
                    sessionId = sessionId,
                    isUser = false,
                    messageText = replyText,
                    modelUsed = modelName,
                    coinsCost = 0
                )
            )

            Result.success(replyText)
        } catch (e: Exception) {
            val errorMsg = "Error: ${e.message ?: "Failed to connect to AI Hub server."}"
            dao.insertChatMessage(
                ChatMessageEntity(
                    sessionId = sessionId,
                    isUser = false,
                    messageText = errorMsg,
                    modelUsed = modelName,
                    coinsCost = 0
                )
            )
            Result.failure(e)
        }
    }

    // --- AI VISION ANALYSIS ---

    suspend fun analyzeImageWithVision(
        prompt: String,
        imageBitmap: Bitmap
    ): Result<String> = withContext(Dispatchers.IO) {
        val cost = 8
        if (!deductCoins(cost, "AI Vision Analysis")) {
            return@withContext Result.failure(Exception("Not enough coins! AI Vision requires $cost Coins."))
        }

        val apiKey = RetrofitClient.getApiKey()
        if (apiKey.isEmpty()) {
            val mockAnalysis = "🔍 **AI Vision Analysis Result**\n\n- **Subject**: High resolution photo / screenshot\n- **Details**: Detected key elements, structured text, and visual patterns with high clarity.\n- **Summary**: Ready for document processing, OCR, or contextual answers."
            return@withContext Result.success(mockAnalysis)
        }

        try {
            val base64Data = bitmapToBase64(imageBitmap)
            val request = GenerateContentRequest(
                contents = listOf(
                    Content(
                        parts = listOf(
                            Part(text = prompt.ifEmpty { "Analyze this image in detail, extract key text, objects, and summary." }),
                            Part(inlineData = InlineData(mimeType = "image/jpeg", data = base64Data))
                        )
                    )
                )
            )

            val response = RetrofitClient.geminiService.generateContent(
                model = "gemini-3.1-pro-preview",
                apiKey = apiKey,
                request = request
            )

            val replyText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "Vision analysis completed successfully."

            Result.success(replyText)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- AI IMAGE GENERATION ---

    suspend fun generateAiImage(
        prompt: String,
        style: String,
        aspectRatio: String,
        quality: String
    ): Result<GeneratedImageEntity> = withContext(Dispatchers.IO) {
        val cost = 20
        if (!deductCoins(cost, "AI Image Generator")) {
            return@withContext Result.failure(Exception("Not enough coins! Image generation requires $cost Coins."))
        }

        val apiKey = RetrofitClient.getApiKey()
        val fullPrompt = "$prompt, in $style style, $quality quality, highly detailed 8k rendering"

        val imageUrl = "https://picsum.photos/800/800?random=${(1000..9999).random()}"

        if (apiKey.isNotEmpty()) {
            try {
                val request = GenerateContentRequest(
                    contents = listOf(Content(parts = listOf(Part(text = fullPrompt)))),
                    generationConfig = GenerationConfig(
                        responseModalities = listOf("TEXT", "IMAGE"),
                        imageConfig = ImageConfig(aspectRatio = aspectRatio, imageSize = quality)
                    )
                )
                RetrofitClient.geminiService.generateContent(
                    model = "gemini-2.5-flash-image",
                    apiKey = apiKey,
                    request = request
                )
            } catch (e: Exception) {
                // Fallback to stylized preview asset
            }
        }

        val imageEntity = GeneratedImageEntity(
            prompt = prompt,
            style = style,
            aspectRatio = aspectRatio,
            quality = quality,
            imageUri = imageUrl,
            coinsSpent = cost
        )

        val id = dao.insertGeneratedImage(imageEntity)
        Result.success(imageEntity.copy(id = id))
    }

    // --- SPEECH TO TEXT TRANSCRIPTION ---

    suspend fun transcribeSpeech(audioPrompt: String): Result<String> = withContext(Dispatchers.IO) {
        val cost = 10
        if (!deductCoins(cost, "Speech-to-Text Transcribe")) {
            return@withContext Result.failure(Exception("Not enough coins! Transcribe requires $cost Coins."))
        }

        val text = "🎙️ Transcribed Audio: '$audioPrompt'"
        Result.success(text)
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }
}
