package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface VendorDao {
    @Query("SELECT * FROM vendors")
    fun getAllVendorsFlow(): Flow<List<Vendor>>

    @Query("SELECT * FROM vendors WHERE id = :id")
    suspend fun getVendorById(id: String): Vendor?

    @Query("SELECT * FROM vendors")
    suspend fun getAllVendors(): List<Vendor>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVendors(vendors: List<Vendor>)
}

@Dao
interface ProductDao {
    @Query("SELECT * FROM products")
    fun getAllProductsFlow(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE category = :category")
    fun getProductsByCategoryFlow(category: String): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: String): Product?

    @Query("SELECT * FROM products WHERE vendorId = :vendorId")
    fun getProductsByVendorFlow(vendorId: String): Flow<List<Product>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<Product>)
}

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages WHERE vendorId = :vendorId ORDER BY timestamp ASC")
    fun getMessagesForVendorFlow(vendorId: String): Flow<List<ChatMessage>>

    @Query("SELECT * FROM chat_messages ORDER BY timestamp DESC")
    fun getAllMessagesFlow(): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage)

    @Query("DELETE FROM chat_messages WHERE vendorId = :vendorId")
    suspend fun clearChatWithVendor(vendorId: String)
}

@Dao
interface BusinessIssueDao {
    @Query("SELECT * FROM business_issues ORDER BY timestamp DESC")
    fun getAllIssuesFlow(): Flow<List<BusinessIssue>>

    @Query("SELECT * FROM business_issues WHERE id = :id")
    suspend fun getIssueById(id: Int): BusinessIssue?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIssue(issue: BusinessIssue): Long

    @Update
    suspend fun updateIssue(issue: BusinessIssue)

    @Query("DELETE FROM business_issues WHERE id = :id")
    suspend fun deleteIssueById(id: Int)
}
