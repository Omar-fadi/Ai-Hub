package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_wallet")
data class UserWallet(
    @PrimaryKey val id: Int = 1,
    val username: String = "AIHubUser",
    val avatarUrl: String = "",
    val coins: Int = 150,
    val xp: Int = 120,
    val level: Int = 1,
    val streakDays: Int = 3,
    val lastCheckInDate: Long = 0,
    val myReferralCode: String = "AH-7K92X",
    val claimedReferral: Boolean = false,
    val totalMessagesSent: Int = 0,
    val totalImagesCreated: Int = 0,
    val badgesJson: String = "[\"Early User\"]"
)

@Entity(tableName = "chat_sessions")
data class ChatSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val modelUsed: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val isUser: Boolean,
    val messageText: String,
    val imageUri: String? = null,
    val modelUsed: String = "gemini-3.5-flash",
    val timestamp: Long = System.currentTimeMillis(),
    val coinsCost: Int = 3
)

@Entity(tableName = "generated_images")
data class GeneratedImageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val prompt: String,
    val style: String,
    val aspectRatio: String,
    val quality: String,
    val imageUri: String,
    val createdAt: Long = System.currentTimeMillis(),
    val coinsSpent: Int = 20
)

@Entity(tableName = "transactions")
data class CoinTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amount: Int, // Positive for gain, negative for spent
    val type: String, // "REDEEM", "TASK", "REFERRAL", "CHAT", "IMAGE", "STT", "VIDEO"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "redeemed_codes")
data class RedeemedCodeEntity(
    @PrimaryKey val code: String,
    val coinsAwarded: Int,
    val redeemedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "reward_tasks")
data class RewardTaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val rewardCoins: Int,
    val rewardXp: Int,
    val actionType: String, // "INSTAGRAM", "TELEGRAM", "SHARE", "DAILY"
    val isCompleted: Boolean = false
)

@Entity(tableName = "community_posts")
data class CommunityPostEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val authorName: String,
    val authorAvatar: String,
    val prompt: String,
    val imageUrl: String,
    val likesCount: Int = 12,
    val createdAt: Long = System.currentTimeMillis()
)
