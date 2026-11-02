package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.UniversalQrDialog
import com.example.ui.theme.TerminalBackground
import com.example.ui.theme.TerminalCard
import com.example.ui.theme.TerminalCardSecondary
import com.example.ui.theme.TerminalCyan
import com.example.ui.theme.TerminalOrange
import com.example.ui.theme.TerminalOrangeBright
import com.example.ui.theme.TerminalTextSecondary
import com.example.ui.theme.TerminalWhite
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class GeneratedArt(
    val prompt: String,
    val seed: Long,
    val timestamp: Long,
    val downloadLink: String
)

@Composable
fun ImageGeneratorScreen(
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val scope = rememberCoroutineScope()
    var promptInput by remember { mutableStateOf("Cybernetic AI assistant terminal with neon orange visor in Chennai") }
    var isGenerating by remember { mutableStateOf(false) }
    var currentArt by remember {
        mutableStateOf(
            GeneratedArt(
                prompt = "Cybernetic AI assistant terminal with neon orange visor in Chennai",
                seed = 42L,
                timestamp = System.currentTimeMillis(),
                downloadLink = "https://semicircle.ai/art/download?seed=42&prompt=cyber_chennai"
            )
        )
    }
    var showQrDialog by remember { mutableStateOf(false) }

    val samplePrompts = listOf(
        "Cybernetic AI terminal with orange visor in Chennai",
        "Futuristic Realme X7 5G robot assistant in Tamil Nadu",
        "Sci-fi quantum holographic radar terminal",
        "Neon orange cybernetic owl with glowing biometric eyes",
        "Solar storm weather telemetry satellite over India"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TerminalBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("image_gen_back_button")) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TerminalOrangeBright)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = "AI IMAGE GENERATOR",
                        color = TerminalOrangeBright,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "NEURAL ART SYNTHESIZER + QR TRANSFER",
                        color = TerminalTextSecondary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Prompt Input Card
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = TerminalCard),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, TerminalOrange.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "ENTER VISUAL PROMPT:",
                        color = TerminalWhite,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = promptInput,
                        onValueChange = { promptInput = it },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TerminalOrange,
                            unfocusedBorderColor = TerminalCardSecondary,
                            focusedTextColor = TerminalWhite,
                            unfocusedTextColor = TerminalWhite
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Preset Chips
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(samplePrompts) { p ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = TerminalCardSecondary),
                                modifier = Modifier
                                    .clickable { promptInput = p }
                                    .border(1.dp, TerminalOrange.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            ) {
                                Text(
                                    text = p.take(30) + "...",
                                    color = TerminalOrangeBright,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            if (promptInput.isNotBlank() && !isGenerating) {
                                isGenerating = true
                                scope.launch {
                                    delay(1600) // Simulated generative rendering
                                    val newSeed = System.currentTimeMillis()
                                    currentArt = GeneratedArt(
                                        prompt = promptInput,
                                        seed = newSeed,
                                        timestamp = newSeed,
                                        downloadLink = "https://semicircle.ai/download/art_${newSeed}.png?prompt=${promptInput.replace(" ", "_")}"
                                    )
                                    isGenerating = false
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TerminalOrange,
                            contentColor = TerminalBackground
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("generate_image_button")
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = TerminalBackground, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("SYNTHESIZING NEURAL PIXELS...", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        } else {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("GENERATE AI VISUAL", fontWeight = FontWeight.Bold, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Generated Visual Display Card
            Text(
                text = "GENERATED VISUAL & WIRELESS QR DOWNLOAD",
                color = TerminalOrangeBright,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = TerminalCard),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, TerminalOrange, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Sci-Fi Generative Canvas Drawing
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .background(Color(0xFF0C0A07), RoundedCornerShape(12.dp))
                            .border(1.dp, TerminalOrange.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            val seed = currentArt.seed

                            // Generative sci-fi abstract art with vibrant orange, amber, and cyan geometry
                            drawRect(
                                brush = Brush.radialGradient(
                                    colors = listOf(TerminalOrange.copy(alpha = 0.35f), Color.Transparent),
                                    center = Offset(w * 0.5f, h * 0.5f),
                                    radius = w * 0.6f
                                )
                            )

                            // Cyber geometric shapes based on seed
                            for (i in 0..6) {
                                val radius = (40f + i * 22f)
                                drawCircle(
                                    color = if (i % 2 == 0) TerminalOrange else TerminalCyan,
                                    radius = radius,
                                    center = Offset(w * 0.5f, h * 0.5f),
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx())
                                )
                            }

                            // Glowing grid lines
                            drawLine(TerminalOrangeBright, Offset(0f, h * 0.5f), Offset(w, h * 0.5f), 1.5.dp.toPx())
                            drawLine(TerminalOrangeBright, Offset(w * 0.5f, 0f), Offset(w * 0.5f, h), 1.5.dp.toPx())
                            drawLine(TerminalCyan, Offset(0f, 0f), Offset(w, h), 1.dp.toPx())
                            drawLine(TerminalCyan, Offset(w, 0f), Offset(0f, h), 1.dp.toPx())
                        }

                        Text(
                            text = "SEMICIRCLE AI ART ENGINE · V1.0",
                            color = TerminalOrangeBright,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(10.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "PROMPT: \"${currentArt.prompt}\"",
                        color = TerminalWhite,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "SEED: #${currentArt.seed} · FORMAT: 1024x1024 RGB",
                        color = TerminalTextSecondary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // QR Code Transfer Button requested by user
                    Button(
                        onClick = { showQrDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TerminalCardSecondary,
                            contentColor = TerminalOrangeBright
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, TerminalOrange, RoundedCornerShape(8.dp))
                            .testTag("show_art_qr_button")
                    ) {
                        Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SHOW DOWNLOADABLE QR CODE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Universal QR Dialog with download link
        if (showQrDialog) {
            UniversalQrDialog(
                title = "DOWNLOAD GENERATED ART QR",
                content = currentArt.downloadLink,
                onDismiss = { showQrDialog = false }
            )
        }
    }
}
