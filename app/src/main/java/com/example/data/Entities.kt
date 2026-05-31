package com.example.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "vendors")
data class Vendor(
    @PrimaryKey val id: String,
    val name: String,
    val category: String,
    val contactPerson: String,
    val rating: Double,
    val address: String,
    val responseTime: String,
    val avatarColorIdx: Int // For custom colorful avatar generation if no image
)

@Entity(
    tableName = "products",
    foreignKeys = [
        ForeignKey(
            entity = Vendor::class,
            parentColumns = ["id"],
            childColumns = ["vendorId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Product(
    @PrimaryKey val id: String,
    val name: String,
    val category: String, // "Clothes" or "Shoes"
    val price: Double,
    val moq: Int, // Minimum Order Quantity
    val imageUrl: String,
    val vendorId: String,
    val description: String
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val vendorId: String,
    val sender: String, // "user" or "vendor"
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "business_issues")
data class BusinessIssue(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val category: String, // "Shipping Delay", "Defective Goods", "Contract Dispute", "Pricing Issue", "Other"
    val status: String = "Open", // "Open" or "Resolved"
    val aiAnalysis: String? = null,
    val draftLetter: String? = null, // Suggested letter drafted by Gemini
    val timestamp: Long = System.currentTimeMillis()
)
