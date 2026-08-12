package com.example.data.db

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update
import com.example.data.models.ChatMessageEntity
import com.example.data.models.ChatSessionEntity
import com.example.data.models.CoinTransactionEntity
import com.example.data.models.CommunityPostEntity
import com.example.data.models.GeneratedImageEntity
import com.example.data.models.RedeemedCodeEntity
import com.example.data.models.RewardTaskEntity
import com.example.data.models.UserWallet
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // User Wallet & Progression
    @Query("SELECT * FROM user_wallet WHERE id = 1")
    fun getUserWallet(): Flow<UserWallet?>

    @Query("SELECT * FROM user_wallet WHERE id = 1")
    suspend fun getUserWalletSync(): UserWallet?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateWallet(wallet: UserWallet)

    // Chat Sessions
    @Query("SELECT * FROM chat_sessions ORDER BY updatedAt DESC")
    fun getAllChatSessions(): Flow<List<ChatSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatSession(session: ChatSessionEntity): Long

    @Query("UPDATE chat_sessions SET title = :newTitle, updatedAt = :timestamp WHERE id = :sessionId")
    suspend fun renameChatSession(sessionId: Long, newTitle: String, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM chat_sessions WHERE id = :sessionId")
    suspend fun deleteChatSession(sessionId: Long)

    @Query("DELETE FROM chat_messages WHERE sessionId = :sessionId")
    suspend fun deleteMessagesForSession(sessionId: Long)

    // Chat Messages
    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getMessagesForSession(sessionId: Long): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    suspend fun getMessagesForSessionSync(sessionId: Long): List<ChatMessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(message: ChatMessageEntity): Long

    // Generated Images
    @Query("SELECT * FROM generated_images ORDER BY createdAt DESC")
    fun getAllGeneratedImages(): Flow<List<GeneratedImageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGeneratedImage(image: GeneratedImageEntity): Long

    @Query("DELETE FROM generated_images WHERE id = :imageId")
    suspend fun deleteGeneratedImage(imageId: Long)

    // Transactions
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<CoinTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: CoinTransactionEntity)

    // Redeem Codes
    @Query("SELECT * FROM redeemed_codes WHERE code = :code")
    suspend fun getRedeemedCode(code: String): RedeemedCodeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRedeemedCode(redeemedCode: RedeemedCodeEntity)

    // Reward Tasks
    @Query("SELECT * FROM reward_tasks")
    fun getAllRewardTasks(): Flow<List<RewardTaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateTasks(tasks: List<RewardTaskEntity>)

    @Query("UPDATE reward_tasks SET isCompleted = 1 WHERE id = :taskId")
    suspend fun markTaskCompleted(taskId: String)

    // Community Posts
    @Query("SELECT * FROM community_posts ORDER BY createdAt DESC")
    fun getAllCommunityPosts(): Flow<List<CommunityPostEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommunityPosts(posts: List<CommunityPostEntity>)
}

@Database(
    entities = [
        UserWallet::class,
        ChatSessionEntity::class,
        ChatMessageEntity::class,
        GeneratedImageEntity::class,
        CoinTransactionEntity::class,
        RedeemedCodeEntity::class,
        RewardTaskEntity::class,
        CommunityPostEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao
}
