package com.example.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.example.data.*
import com.example.data.gemini.GeminiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class WholesaleViewModel(private val repository: WholesaleRepository) : ViewModel() {

    // --- Tab / Screen Navigation State ---
    private val _currentTab = MutableStateFlow(WholesaleTab.CATALOG)
    val currentTab: StateFlow<WholesaleTab> = _currentTab.asStateFlow()

    fun selectTab(tab: WholesaleTab) {
        _currentTab.value = tab
    }

    // --- Wholesale Hub / Product Catalog State ---
    private val _selectedProductCategory = MutableStateFlow("All")
    val selectedProductCategory: StateFlow<String> = _selectedProductCategory.asStateFlow()

    val allVendors: StateFlow<List<Vendor>> = repository.allVendors
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredProducts: StateFlow<List<Product>> = combine(
        repository.allProducts,
        _selectedProductCategory
    ) { products, category ->
        if (category == "All") {
            products
        } else {
            products.filter { it.category.equals(category, ignoreCase = true) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectProductCategory(category: String) {
        _selectedProductCategory.value = category
    }

    // --- Active Chat & Vendor Inbox Communication State ---
    private val _activeChatVendor = MutableStateFlow<Vendor?>(null)
    val activeChatVendor: StateFlow<Vendor?> = _activeChatVendor.asStateFlow()

    // Retrieve active messages reactively
    val activeChatMessages: StateFlow<List<ChatMessage>> = _activeChatVendor
        .flatMapLatest { vendor ->
            if (vendor == null) flowOf(emptyList())
            else repository.getMessagesForVendor(vendor.id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectChatVendor(vendor: Vendor) {
        _activeChatVendor.value = vendor
        _currentTab.value = WholesaleTab.CHAT
    }

    fun exitActiveChat() {
        _activeChatVendor.value = null
    }

    // Sending user messages + simulating supplier answers
    fun sendMessage(vendorId: String, text: String) {
        if (text.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            // 1. Insert User Message
            val userMsg = ChatMessage(
                vendorId = vendorId,
                sender = "user",
                message = text
            )
            repository.insertMessage(userMsg)

            // 2. Schedule Supplier Reply simulation
            delay(1200) // Realistic delay
            val supplierReply = getSimulatedSupplierReply(vendorId, text)
            repository.insertMessage(
                ChatMessage(
                    vendorId = vendorId,
                    sender = "vendor",
                    message = supplierReply
                )
            )
        }
    }

    private suspend fun getSimulatedSupplierReply(vendorId: String, userText: String): String {
        val vendor = repository.getVendorById(vendorId)
        val name = vendor?.contactPerson ?: "Vendor Representative"
        val lowercaseText = userText.lowercase()

        return when (vendorId) {
            "vendor_aura" -> {
                when {
                    "price" in lowercaseText || "discount" in lowercaseText || "cost" in lowercaseText -> {
                        "Hi! For quantities of 100+ units of our Boxy Tee, we discount it from $8.50 to $7.80. Free sea shipping is included for bulk counts."
                    }
                    "moq" in lowercaseText || "size" in lowercaseText || "minimum" in lowercaseText -> {
                        "Our minimum order quantity (MOQ) is 50 pieces. We carry sizes S through XXL, and you can absolutely mix-and-match sizes and colors to hit MOQ!"
                    }
                    "design" in lowercaseText || "custom" in lowercaseText || "print" in lowercaseText -> {
                        "Yes! We offer silk screen printing and custom woven neck tags. Send us your high-res design file (.AI or PDF) so our production team can mock it up."
                    }
                    else -> "Thanks for reaching out! Marcus here. I can write up a custom order draft for you. What quantities or colors were you looking for?"
                }
            }
            "vendor_solecraft" -> {
                when {
                    "sample" in lowercaseText || "test" in lowercaseText -> {
                        "Elena here! Yes, we ship quality samples at $30 a pair. It takes 4 days to prepare and we credit that sample price back when you lock in the bulk order."
                    }
                    "moq" in lowercaseText || "minimum" in lowercaseText -> {
                        "The standard MOQ is 30 pairs. We package shoes in mixed cartons containing standard size runs (US Men's 8-12 / US Women's 6-10). Let me know what you need."
                    }
                    "logo" in lowercaseText || "brand" in lowercaseText -> {
                        "Absolutely, we offer embossed branding on the tongue and custom shoe box printing. Brand customization starts at 100 pairs with a 15-day turnaround."
                    }
                    else -> "Hi! Doing wholesale on shoes? Our Nebula Runners are selling out. Happy to help you with sizes and options!"
                }
            }
            "vendor_ecoknit" -> {
                when {
                    "material" in lowercaseText || "hemp" in lowercaseText || "organic" in lowercaseText -> {
                        "We use 100% GOTS certified organic hemp and cotton blends. Extremely soft, pre-shrunk, and colored with non-toxic botanical dyes."
                    }
                    "sample" in lowercaseText || "price" in lowercaseText -> {
                        "Tariq here. Samples of the hemp shirt are $15 each. For wholesale orders above 500 units, we can drop the unit price from $11.20 to $9.95."
                    }
                    else -> "Greetings! EcoKnit provides top-tier sustainable blanks. All products are in stock and ready to ship out from our California warehouse."
                }
            }
            "vendor_ascent" -> {
                when {
                    "material" in lowercaseText || "leather" in lowercaseText -> {
                        "Ciao! Giovanni here. We source full-grain aniline calfskin directly from our historic Florence tanneries. The linings are fully breathable."
                    }
                    "price" in lowercaseText || "shipping" in lowercaseText -> {
                        "Hi there! The Heritage Derby is priced at $45.00 for wholesale due to its Goodyear welded build. Air freight to US/Europe is about $8 per pair, taking 5 days."
                    }
                    else -> "Incredible luxury designs are our passion at Ascent. Let me know if you would like custom hand-burnishing on your derby shoes order."
                }
            }
            else -> "Thank you for your message. How can I help you resolve this order question today?"
        }
    }

    // --- Gemini Business Issues state ---
    val allIssues: StateFlow<List<BusinessIssue>> = repository.allIssues
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedIssue = MutableStateFlow<BusinessIssue?>(null)
    val selectedIssue: StateFlow<BusinessIssue?> = _selectedIssue.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    fun selectIssue(issue: BusinessIssue) {
        _selectedIssue.value = issue
    }

    fun clearSelectedIssue() {
        _selectedIssue.value = null
    }

    // Submit new issue and analyze immediately via Gemini AI
    fun submitAndAnalyzeIssue(title: String, description: String, category: String) {
        if (title.isBlank() || description.isBlank()) return

        viewModelScope.launch {
            // 1. Save issue locally first
            val id = repository.createBusinessIssue(title, description, category)
            val freshIssue = BusinessIssue(
                id = id.toInt(),
                title = title,
                description = description,
                category = category,
                status = "Open"
            )

            // Focus on this issue
            _selectedIssue.value = freshIssue
            // Switch view to issues tab
            _currentTab.value = WholesaleTab.ISSUES

            // 2. Trigger Gemini Analysis (async on background thread)
            _isAnalyzing.value = true
            try {
                val result = GeminiClient.analyzeBusinessIssue(title, description, category)
                val analyzedIssue = freshIssue.copy(
                    aiAnalysis = result.first,
                    draftLetter = result.second
                )
                repository.updateBusinessIssue(analyzedIssue)

                // Update selected issue to show analysis
                if (_selectedIssue.value?.id == freshIssue.id) {
                    _selectedIssue.value = analyzedIssue
                }
            } catch (e: Exception) {
                // If failed, make sure local state is updated
                Log.e("WholesaleVM", "Gemini analysis error", e)
            } finally {
                _isAnalyzing.value = false
            }
        }
    }

    fun toggleIssueStatus(issue: BusinessIssue) {
        viewModelScope.launch(Dispatchers.IO) {
            val newStatus = if (issue.status == "Open") "Resolved" else "Open"
            val updated = issue.copy(status = newStatus)
            repository.updateBusinessIssue(updated)
            if (_selectedIssue.value?.id == issue.id) {
                _selectedIssue.value = updated
            }
        }
    }

    fun deleteIssue(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteBusinessIssue(id)
            if (_selectedIssue.value?.id == id) {
                _selectedIssue.value = null
            }
        }
    }

    fun clearChat(vendorId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearChatWithVendor(vendorId)
        }
    }
}

enum class WholesaleTab {
    CATALOG,  // Wholesale Hub: Scroll clothes and shoes
    CHAT,     // Direct Connect: Connect with vendors
    ISSUES    // Business Consultant: Submit & resolve business issues with Gemini AI
}

@Suppress("UNCHECKED_CAST")
class WholesaleViewModelFactory(private val repository: WholesaleRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WholesaleViewModel::class.java)) {
            return WholesaleViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
