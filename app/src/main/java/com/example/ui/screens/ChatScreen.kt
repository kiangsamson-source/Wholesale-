package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ChatMessage
import com.example.data.Vendor
import com.example.viewmodel.WholesaleViewModel
import kotlinx.coroutines.launch

private val AvatarColors = listOf(
    Color(0xFF3F51B5), // Indigo
    Color(0xFF009688), // Teal Emerald
    Color(0xFFE91E63), // Rose
    Color(0xFFFF9800), // Amber Warm
    Color(0xFF9C27B0)  // Purple Royal
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: WholesaleViewModel,
    modifier: Modifier = Modifier
) {
    val vendors by viewModel.allVendors.collectAsState()
    val activeVendor by viewModel.activeChatVendor.collectAsState()
    val messages by viewModel.activeChatMessages.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("chat_screen")
    ) {
        if (activeVendor == null) {
            // Roster Screen: List of Vendors
            ChatRoster(vendors = vendors, onVendorClick = { viewModel.selectChatVendor(it) })
        } else {
            // Direct Stream Screen
            DirectChatStream(
                vendor = activeVendor!!,
                messages = messages,
                onBack = { viewModel.exitActiveChat() },
                onSend = { text -> viewModel.sendMessage(activeVendor!!.id, text) },
                onClear = { viewModel.clearChat(activeVendor!!.id) }
            )
        }
    }
}

@Composable
fun ChatRoster(
    vendors: List<Vendor>,
    onVendorClick: (Vendor) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                .padding(20.dp)
        ) {
            Column {
                Text(
                    text = "Direct Connect",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Real-time communication channels with manufacturing plants",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (vendors.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No vendors available", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(vendors) { vendor ->
                    VendorRosterItem(vendor = vendor, onClick = { onVendorClick(vendor) })
                }
            }
        }
    }
}

@Composable
fun VendorRosterItem(
    vendor: Vendor,
    onClick: () -> Unit
) {
    val avatarColor = AvatarColors[vendor.avatarColorIdx % AvatarColors.size]

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("roster_item_${vendor.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circle Avatar block
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(avatarColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = vendor.name.take(1),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = vendor.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Contact: " + vendor.contactPerson,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = vendor.responseTime,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Color(0xFFFFB300)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = vendor.rating.toString(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChatBubbleOutline,
                contentDescription = "Open Chat",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun DirectChatStream(
    vendor: Vendor,
    messages: List<ChatMessage>,
    onBack: () -> Unit,
    onSend: (String) -> Unit,
    onClear: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    var inputMessage by remember { mutableStateOf("") }

    // Scroll to latest message on load or on message change
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(start = 8.dp, top = 8.dp, end = 8.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("chat_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = vendor.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF4CAF50))
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Active support: ${vendor.contactPerson} (${vendor.responseTime})",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onClear,
                        modifier = Modifier.testTag("clear_chat_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear Chat logs",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Divider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 1.dp)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Chat Balloon Flow
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (messages.isEmpty()) {
                    item {
                        EmptyChatState(vendorName = vendor.name, contact = vendor.contactPerson)
                    }
                } else {
                    items(messages) { message ->
                        ChatBalloon(message = message)
                    }
                }
            }

            // Quick Wholesale Recommendation Rows (Frictionless templates)
            QuickWholesaleInquiries(
                onInquirySelected = { inquiryText ->
                    onSend(inquiryText)
                }
            )

            // Text Input bar (Compose interactive elements)
            InputConsole(
                inputVal = inputMessage,
                onValueChange = { inputMessage = it },
                onSend = {
                    if (inputMessage.isNotBlank()) {
                        onSend(inputMessage)
                        inputMessage = ""
                    }
                }
            )
        }
    }
}

@Composable
fun EmptyChatState(vendorName: String, contact: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.People,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Secure Direct Connection",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Ask $contact for bulk pricing, customized labels, or design files.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 24.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun ChatBalloon(message: ChatMessage) {
    val isUser = message.sender == "user"
    val alignment = if (isUser) Alignment.End else Alignment.Start
    val containerColor = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    val shape = if (isUser) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 2.dp)
    } else {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 2.dp, bottomEnd = 16.dp)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Card(
            shape = shape,
            colors = CardDefaults.cardColors(containerColor = containerColor),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Text(
                text = message.message,
                modifier = Modifier.padding(12.dp),
                fontSize = 14.sp,
                color = textColor
            )
        }
    }
}

@Composable
fun QuickWholesaleInquiries(
    onInquirySelected: (String) -> Unit
) {
    val quickQuestions = listOf(
        "Do you carry color catalog files and size sheets?",
        "What is your price list for 100+ volume quantity?",
        "Do you offer custom branding options on standard items?",
        "Can we order quality testing samples first?"
    )

    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
        Text("Quick questions:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 6.dp)
        ) {
            items(quickQuestions) { question ->
                Card(
                    modifier = Modifier
                        .clickable { onInquirySelected(question) }
                        .testTag("quick_question_${question.take(6)}"),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Text(
                        text = question,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun InputConsole(
    inputVal: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = inputVal,
            onValueChange = onValueChange,
            placeholder = { Text("Write wholesale inquiry...") },
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(24.dp))
                .testTag("chat_input_field"),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            ),
            maxLines = 3
        )
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(
            onClick = onSend,
            enabled = inputVal.isNotBlank(),
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(if (inputVal.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                .testTag("send_message_button")
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Default.Send,
                contentDescription = "Send",
                tint = if (inputVal.isNotBlank()) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}
