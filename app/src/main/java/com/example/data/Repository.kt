package com.example.data

import kotlinx.coroutines.flow.Flow

class WholesaleRepository(private val database: AppDatabase) {
    val allVendors: Flow<List<Vendor>> = database.vendorDao().getAllVendorsFlow()
    val allProducts: Flow<List<Product>> = database.productDao().getAllProductsFlow()
    val allIssues: Flow<List<BusinessIssue>> = database.businessIssueDao().getAllIssuesFlow()

    fun getProductsByCategory(category: String): Flow<List<Product>> {
        return database.productDao().getProductsByCategoryFlow(category)
    }

    fun getProductsByVendor(vendorId: String): Flow<List<Product>> {
        return database.productDao().getProductsByVendorFlow(vendorId)
    }

    suspend fun getVendorById(vendorId: String): Vendor? {
        return database.vendorDao().getVendorById(vendorId)
    }

    fun getMessagesForVendor(vendorId: String): Flow<List<ChatMessage>> {
        return database.chatMessageDao().getMessagesForVendorFlow(vendorId)
    }

    suspend fun insertMessage(message: ChatMessage) {
        database.chatMessageDao().insertMessage(message)
    }

    suspend fun createBusinessIssue(title: String, description: String, category: String): Long {
        val issue = BusinessIssue(
            title = title,
            description = description,
            category = category,
            status = "Open"
        )
        return database.businessIssueDao().insertIssue(issue)
    }

    suspend fun updateBusinessIssue(issue: BusinessIssue) {
        database.businessIssueDao().updateIssue(issue)
    }

    suspend fun deleteBusinessIssue(id: Int) {
        database.businessIssueDao().deleteIssueById(id)
    }

    suspend fun clearChatWithVendor(vendorId: String) {
        database.chatMessageDao().clearChatWithVendor(vendorId)
    }

    // Helper to get raw non-flow list of vendors
    suspend fun queryAllVendors(): List<Vendor> {
        return database.vendorDao().getAllVendors()
    }
}
