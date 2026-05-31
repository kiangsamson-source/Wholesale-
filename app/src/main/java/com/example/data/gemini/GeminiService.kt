package com.example.data.gemini

import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class Part(val text: String? = null)

@JsonClass(generateAdapter = true)
data class Content(val parts: List<Part>)

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    val contents: List<Content>,
    val systemInstruction: Content? = null
)

@JsonClass(generateAdapter = true)
data class PartResponse(val text: String? = null)

@JsonClass(generateAdapter = true)
data class ContentResponse(val parts: List<PartResponse>? = null)

@JsonClass(generateAdapter = true)
data class Candidate(val content: ContentResponse? = null)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(val candidates: List<Candidate>? = null)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object GeminiClient {
    private const val TAG = "GeminiClient"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val api: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }

    /**
     * Checks if the API key is set and valid
     */
    fun isApiKeyConfigured(): Boolean {
        val key = BuildConfig.GEMINI_API_KEY
        return !key.isNullOrBlank() && key != "MY_GEMINI_API_KEY"
    }

    /**
     * Performs direct REST API call to Gemini to analyze a business issue
     */
    suspend fun analyzeBusinessIssue(
        title: String,
        description: String,
        category: String
    ): Pair<String, String> {
        if (!isApiKeyConfigured()) {
            Log.w(TAG, "Gemini API Key is not set or placeholder. Using local simulation fallback.")
            return generateSimulationFallback(category, title, description)
        }

        val prompt = """
            You are an expert wholesale consultant, supply chain analyst, and professional legal draftsman.
            A business owner is dealing with a critical issue on our wholesale marketplace app.
            
            Issue Details:
            - Category: $category
            - Title: $title
            - Description: $description
            
            Please provide two distinct sections in your response, strictly separated by a special divider token "---LETTER-DIVIDER---".
            
            Section 1: Expert Business Advice & Diagnosis 
            Analyze this issue. Identify what went wrong (e.g., supply chain risk, contract liability, standard incoterms like FOB vs CIF if relevant, communication gaps). Limit this to 3 short, actionable, and concrete steps high-quality recommendations in clean, readable bullets.
            
            "---LETTER-DIVIDER---"
            
            Section 2: Professional Direct Communication Draft
            Draft a highly professional, firm yet diplomatic direct letter or message template that the buyer can copy-paste and send to this wholesale vendor to address and resolve this dispute. It must contain placeholders like [Vendor Name], [Your Company], [Date], and specific details from the issue.
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            systemInstruction = Content(
                parts = listOf(
                    Part(text = "You are a pragmatic, authoritative supply chain expert and commercial business advisor.")
                )
            )
        )

        return try {
            val response = api.generateContent(BuildConfig.GEMINI_API_KEY, request)
            val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: throw Exception("Empty response from Gemini")

            val parts = responseText.split("---LETTER-DIVIDER---")
            val rawAnalysis = parts.getOrNull(0)?.trim() ?: responseText
            val rawLetter = parts.getOrNull(1)?.trim() ?: "Dear [Vendor Name],\n\nWe would like to resolve the issue regarding: $title..."

            Pair(rawAnalysis, rawLetter)
        } catch (e: Exception) {
            Log.e(TAG, "Gemini API failed: ", e)
            generateSimulationFallback(category, title, description)
        }
    }

    private fun generateSimulationFallback(
        category: String,
        title: String,
        description: String
    ): Pair<String, String> {
        val analysis = when (category) {
            "Shipping Delay" -> """
                🎯 **Wholesale Logistics Diagnosis (Key is unconfigured)**:
                - **Incoterms Assessment**: Under EXW or FOB terms, transport risk shifts to you upon carrier collection. Under CIF/DAP, risk is held by the vendor until delivery.
                - **Mitigation Action**: Immediately request the carrier bill of lading (BOL) and container tracking code from the vendor. Establish a strict liquidated damages clause for delays in future purchase orders (POs).
                - **Diversification Plan**: Maintain active backups (e.g., SoleCraft or EcoKnit) for secondary emergency lead times.
            """.trimIndent()
            "Defective Goods" -> """
                🎯 **Quality Assurance Diagnosis (Key is unconfigured)**:
                - **Return & Refund terms**: Standard wholesale allows a 'Remedy Period' (typically 7-14 days upon delivery) to submit inspection logs.
                - **Evidence Checklist**: Take high-resolution photos and video and compile a formal QC Inspection Report detailing defect rate (%) vs. acceptable quality limit (AQL).
                - **Resolution Track**: Request replacement with the next freight run or set an immediate credit balance on your account.
            """.trimIndent()
            "Contract Dispute" -> """
                🎯 **Commercial Legal Diagnosis (Key is unconfigured)**:
                - **Vagueness Exposure**: Most wholesale disputes emerge from lacking written Agreements, relying purely on verbal logs or raw platform messages.
                - **Platform Rights**: Review the Wholesale Connect arbitration terms on material non-performance.
                - **Future Protection**: Insist on pre-shipment inspections and escrow or escrow-style milestone payments.
            """.trimIndent()
            else -> """
                🎯 **Wholesale Advisory Diagnosis (Key is unconfigured)**:
                - **Communication Gap**: 80% of supplier issues stem from unrecorded specifications or vague delivery parameters.
                - **Action Standard**: Establish structured milestones with SLA deadlines and written sign-offs.
                - **Next Steps**: File a formal dispute and initiate a mediation call with the account manager.
            """.trimIndent()
        }

        val subject = when (category) {
            "Shipping Delay" -> "URGENT: Shipment Tracking and SLA Inquiry for PO #$title"
            "Defective Goods" -> "NOTICE: Quality Conformity Issue - Inspection Report for PO #$title"
            "Contract Dispute" -> "NOTICE OF DISPUTE: Breach of Agreed Order Specifications - PO #$title"
            else -> "Inquiry: Resolution of Wholesale SLA Issues - PO #$title"
        }

        val letter = """
            Dear [Vendor Contact Name],
            
            I am writing on behalf of [Your Company/Brand Name] regarding $subject.
            
            We received/ordered the most recent batch under $title. However, we have experienced critical challenges as described below:
            "$description"
            
            We expect high operational standards from our production partners. To resolve this constructively, we propose the following steps within the next 48 business hours:
            1. Provide immediate official updates or tracking credentials.
            2. For defective components, credit our account with ${'$'}[0.00] or schedule a shipping run for replacement components.
            3. Arrange a brief technical alignment call to secure future shipments.
            
            Please confirm receipt of this notice and provide your proposed resolution timeline.
            
            Sincerely,
            [Your Name]
            [Your Title / Purchasing Manager]
            [Your Contact Details]
        """.trimIndent()

        return Pair(analysis, letter)
    }
}
