package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Vendor::class, Product::class, ChatMessage::class, BusinessIssue::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun vendorDao(): VendorDao
    abstract fun productDao(): ProductDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun businessIssueDao(): BusinessIssueDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "wholesale_connect_database"
                )
                .addCallback(DatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database)
                }
            }
        }

        private suspend fun populateDatabase(db: AppDatabase) {
            val vendors = listOf(
                Vendor(
                    id = "vendor_aura",
                    name = "Aura Outfitters",
                    category = "Premium Clothes",
                    contactPerson = "Marcus Sterling",
                    rating = 4.8,
                    address = "902 Fashion District, New York, NY",
                    responseTime = "Within 10 mins",
                    avatarColorIdx = 0
                ),
                Vendor(
                    id = "vendor_solecraft",
                    name = "SoleCraft Wholesale",
                    category = "Athletic & Lifestyle Shoes",
                    contactPerson = "Elena Rostova",
                    rating = 4.9,
                    address = "404 Cobbler Rd, Portland, OR",
                    responseTime = "Within 30 mins",
                    avatarColorIdx = 1
                ),
                Vendor(
                    id = "vendor_ecoknit",
                    name = "EcoKnit Textiles",
                    category = "Sustainable Basics",
                    contactPerson = "Tariq Mahmood",
                    rating = 4.6,
                    address = "12 Green Way, San Francisco, CA",
                    responseTime = "Within 1 hour",
                    avatarColorIdx = 2
                ),
                Vendor(
                    id = "vendor_ascent",
                    name = "Ascent Footwear",
                    category = "Leather Boots & Formal Shoes",
                    contactPerson = "Giovanni Rossi",
                    rating = 4.7,
                    address = "77 Tuscany Blvd, Florence, Italy",
                    responseTime = "Within 2 hours",
                    avatarColorIdx = 3
                )
            )
            db.vendorDao().insertVendors(vendors)

            val products = listOf(
                Product(
                    id = "prod_boxy_tee",
                    name = "Heavyweight Boxy Tee",
                    category = "Clothes",
                    price = 8.50,
                    moq = 50,
                    imageUrl = "https://images.unsplash.com/photo-1521572267360-ee0c2909d518?w=500&auto=format&fit=crop&q=60",
                    vendorId = "vendor_aura",
                    description = "100% organic cotton, 280GSM heavyweight streetwear t-shirt. Pre-shrunk, drop-shoulder luxury streetwear fit, double-stitched seams. Available in 10 colorways."
                ),
                Product(
                    id = "prod_fleece_hoodie",
                    name = "Oversized Fleece Hoodie",
                    category = "Clothes",
                    price = 16.50,
                    moq = 40,
                    imageUrl = "https://images.unsplash.com/photo-1556821840-3a63f95609a7?w=500&auto=format&fit=crop&q=60",
                    vendorId = "vendor_aura",
                    description = "Ultra-soft inner fleece basic hoodie. Relaxed cozy aesthetic, double-lined hood with custom drawstrings. Tear-away tags for rebranding."
                ),
                Product(
                    id = "prod_nebula_runner",
                    name = "Nebula Runner 2.0",
                    category = "Shoes",
                    price = 24.00,
                    moq = 30,
                    imageUrl = "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=500&auto=format&fit=crop&q=60",
                    vendorId = "vendor_solecraft",
                    description = "High-performance air mesh shoes optimized for lifestyle and athletics. Dynamic custom-molded EVA insoles and rubber outsole for strong traction."
                ),
                Product(
                    id = "prod_street_sneaker",
                    name = "Neo Street Sneaker",
                    category = "Shoes",
                    price = 19.90,
                    moq = 30,
                    imageUrl = "https://images.unsplash.com/photo-1549298916-b41d501d3772?w=500&auto=format&fit=crop&q=60",
                    vendorId = "vendor_solecraft",
                    description = "Classic flat-soled court sneaker crafted from premium vegan leather details. Extra durable vulcanized rubber, perfect for everyday skateboarding custom designs."
                ),
                Product(
                    id = "prod_hemp_shirt",
                    name = "Hemp Linen Leisure Shirt",
                    category = "Clothes",
                    price = 11.20,
                    moq = 100,
                    imageUrl = "https://images.unsplash.com/photo-1596755094514-f87e34085b2c?w=500&auto=format&fit=crop&q=60",
                    vendorId = "vendor_ecoknit",
                    description = "Breathable organic hemp and linen blend lightweight long sleeve. Finished with naturally sourced coconut shell buttons and a relaxed resort collar."
                ),
                Product(
                    id = "prod_leather_derby",
                    name = "Heritage Leather Derby",
                    category = "Shoes",
                    price = 45.00,
                    moq = 20,
                    imageUrl = "https://images.unsplash.com/photo-1533867617858-e7b97e060509?w=500&auto=format&fit=crop&q=60",
                    vendorId = "vendor_ascent",
                    description = "Full-grain Italian calfskin leather with a hand-stitched Goodyear welt. Premium cork filling adapts to the wearer's foot, lined with breathable calf leather."
                )
            )
            db.productDao().insertProducts(products)

            // Seed initial greeting message
            db.chatMessageDao().insertMessage(
                ChatMessage(
                    vendorId = "vendor_aura",
                    sender = "vendor",
                    message = "Hi there! I'm Marcus from Aura Outfitters. Thanks for checking out our premium blank clothes. Let me know if you are looking to place a bulk custom embroidery or print order!"
                )
            )
            db.chatMessageDao().insertMessage(
                ChatMessage(
                    vendorId = "vendor_solecraft",
                    sender = "vendor",
                    message = "Hello! Elena here. Doing wholesale on shoes? We can custom label with your logo on orders starting at 100 pairs. What sizing breakdown do you need?"
                )
            )
        }
    }
}
