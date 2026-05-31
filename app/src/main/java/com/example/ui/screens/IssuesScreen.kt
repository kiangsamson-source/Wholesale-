package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BusinessIssue
import com.example.viewmodel.WholesaleViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IssuesScreen(
    viewModel: WholesaleViewModel,
    modifier: Modifier = Modifier
) {
    val activeIssue by viewModel.selectedIssue.collectAsState()
    val listIssues by viewModel.allIssues.collectAsState()
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()

    var showForm by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("issues_screen")
    ) {
        if (activeIssue != null) {
            // Detailed Analysis View
            IssueAnalysisDetail(
                issue = activeIssue!!,
                isAnalyzing = isAnalyzing,
                onBack = { viewModel.clearSelectedIssue() },
                onToggleResolved = { viewModel.toggleIssueStatus(it) },
                onDelete = {
                    viewModel.deleteIssue(it.id)
                    viewModel.clearSelectedIssue()
                }
            )
        } else if (showForm) {
            // Issue creation form
            IssueCreationForm(
                onBack = { showForm = false },
                onSubmit = { title, desc, cat ->
                    viewModel.submitAndAnalyzeIssue(title, desc, cat)
                    showForm = false
                }
            )
        } else {
            // Standard Dashboard Lists
            ConsultantDashboard(
                issues = listIssues,
                onCreateLog = { showForm = true },
                onIssueClick = { viewModel.selectIssue(it) }
            )
        }
    }
}

@Composable
fun ConsultantDashboard(
    issues: List<BusinessIssue>,
    onCreateLog: () -> Unit,
    onIssueClick: (BusinessIssue) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Business Consultant",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Resolve shipping, defects, and payment disputes with Gemini AI",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Action Banner
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("consult_hero_card"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.SmartToy,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Lodge Wholesale Dispute",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Experienced an issue with shipping bounds, defects, or contract terms? Submit full specifications and our Gemini AI agent will diagnose commercial risk variables and draft a customized message directly for that manufacturer.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onCreateLog,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("file_dispute_button")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("New AI Consultation Log")
                        }
                    }
                }
            }

            // Historical List Title
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.EventNote,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Your Dispute Logs (${issues.size})",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            if (issues.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Help,
                                contentDescription = null,
                                modifier = Modifier.size(44.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "No disputes logged yet",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(issues) { issue ->
                    IssueDashboardItem(issue = issue, onClick = { onIssueClick(issue) })
                }
            }
        }
    }
}

@Composable
fun IssueDashboardItem(
    issue: BusinessIssue,
    onClick: () -> Unit
) {
    val isResolved = issue.status == "Resolved"
    val badgeBg = if (isResolved) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
    val badgeFg = if (isResolved) Color(0xFF2E7D32) else Color(0xFFE65100)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("issue_item_${issue.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Category Tag
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = issue.category,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                // Resolved status indicator
                Card(
                    colors = CardDefaults.cardColors(containerColor = badgeBg),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = issue.status,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeFg,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = issue.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = issue.description,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (issue.aiAnalysis != null) Icons.Default.Check else Icons.Default.SmartToy,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = if (issue.aiAnalysis != null) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (issue.aiAnalysis != null) "AI Consultant Report Ready" else "Ready to analyze",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (issue.aiAnalysis != null) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.UnfoldMore,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IssueCreationForm(
    onBack: () -> Unit,
    onSubmit: (String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Shipping Delay") }

    val categories = listOf("Shipping Delay", "Defective Goods", "Contract Dispute", "Pricing Issue", "Other")

    Scaffold(
        topBar = {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("form_back_button")) {
                        Icon(imageVector = Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back")
                    }
                    Text(
                        text = "AI Consult Request",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Divider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 1.dp)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Category selectors
                item {
                    Text("1. Select Issue Category", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(categories) { cat ->
                            val isSel = cat == selectedCategory
                            FilterChip(
                                selected = isSel,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                modifier = Modifier.testTag("chip_cat_$cat")
                            )
                        }
                    }
                }

                // Title field code
                item {
                    Text("2. Issue Title", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    TextField(
                        value = title,
                        onValueChange = { title = it },
                        placeholder = { Text("e.g. Broken seams on hoodie cargo (Order #33)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_issue_title"),
                        maxLines = 2,
                        shape = RoundedCornerShape(8.dp),
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                }

                // Description field code
                item {
                    Text("3. Detailed Description & Specifications", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = { Text("What happened? Detail the agreements, product lines, timestamps, communication records, or quantity defect ratios.") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .testTag("input_issue_description"),
                        colors = OutlinedTextFieldDefaults.colors()
                    )
                }

                // Friendly Guidance banner
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(modifier = Modifier.padding(12.dp)) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Tip: High-grade descriptions yield legally robust templates and accurate Incoterm liability assessments from our AI Consultant.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }

            // Big Action triggers
            Button(
                onClick = {
                    if (title.isNotBlank() && description.isNotBlank()) {
                        onSubmit(title, description, selectedCategory)
                    }
                },
                enabled = title.isNotBlank() && description.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(50.dp)
                    .testTag("submit_dispute_button")
            ) {
                Icon(imageVector = Icons.Default.Psychology, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Analyze with Gemini AI consultant", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IssueAnalysisDetail(
    issue: BusinessIssue,
    isAnalyzing: Boolean,
    onBack: () -> Unit,
    onToggleResolved: (BusinessIssue) -> Unit,
    onDelete: (BusinessIssue) -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    // Animated scrolling dots for loading
    val loadingStages = listOf(
        "Filing dispute terms locally...",
        "Evaluating commercial trade risk matrices...",
        "Identifying liability under standard Incoterms...",
        "Drafting firm communication letter template..."
    )
    var activeLoadingStageIndex by remember { mutableStateOf(0) }

    LaunchedEffect(isAnalyzing) {
        if (isAnalyzing) {
            activeLoadingStageIndex = 0
            while (isAnalyzing) {
                delay(2000)
                activeLoadingStageIndex = (activeLoadingStageIndex + 1) % loadingStages.size
            }
        }
    }

    Scaffold(
        topBar = {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("detail_back_button")) {
                        Icon(imageVector = Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back")
                    }
                    Text(
                        text = "Consultation Report",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = { onDelete(issue) },
                        modifier = Modifier.testTag("delete_issue_button")
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Issue")
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
                .background(MaterialTheme.colorScheme.background)
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header block: Title, Category, Status badges
                item {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = issue.category,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            val isResolved = issue.status == "Resolved"
                            val statusBg = if (isResolved) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                            val statusFg = if (isResolved) Color(0xFF2E7D32) else Color(0xFFE65100)
                            Card(
                                colors = CardDefaults.cardColors(containerColor = statusBg),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier
                                    .clickable { onToggleResolved(issue) }
                                    .testTag("toggle_status_badge")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = issue.status,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = statusFg
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.UnfoldMore,
                                        contentDescription = "Change Status",
                                        tint = statusFg,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = issue.title,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                // Initial request description
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Logged Description",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = issue.description,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // AI ANALYSIS CONTAINER
                item {
                    Text(
                        text = "Consultant Report (Gemini AI)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (isAnalyzing) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("analyzing_loading_panel"),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(44.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = loadingStages[activeLoadingStageIndex],
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else if (issue.aiAnalysis == null) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Missing Analysis",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Something interrupted the automated diagnosis. Tap 'Regenerate' below to invoke Gemini AI.",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                } else {
                    // LIVE Gemini output section
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("ai_diagnosis_card"),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.SmartToy,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Trade & Risk Diagnosis",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = issue.aiAnalysis!!,
                                    fontSize = 14.sp,
                                    lineHeight = 20.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // Direct copy letters drafted by Gemini
                    if (issue.draftLetter != null) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("draft_letter_card"),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                ),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Psychology,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.secondary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Direct Vendor Message Template",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = MaterialTheme.colorScheme.secondary
                                            )
                                        }

                                        IconButton(
                                            onClick = {
                                                clipboardManager.setText(AnnotatedString(issue.draftLetter!!))
                                                Toast.makeText(context, "Direct message copied!", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.testTag("copy_template_button")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ContentCopy,
                                                contentDescription = "Copy draft text",
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                color = MaterialTheme.colorScheme.surface,
                                                shape = RoundedCornerShape(6.dp)
                                            )
                                            .padding(12.dp)
                                    ) {
                                        Text(
                                            text = issue.draftLetter!!,
                                            fontSize = 12.sp,
                                            fontFamily = FontFamily.Monospace,
                                            lineHeight = 18.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Quick Status Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onToggleResolved(issue) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("resolve_button")
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (issue.status == "Open") "Resolve Dispute" else "Mark Open",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
