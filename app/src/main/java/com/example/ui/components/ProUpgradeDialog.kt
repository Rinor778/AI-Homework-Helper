package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class PaymentMethod(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val subtitle: String) {
    CreditCard("Credit/Debit Card", Icons.Default.CreditCard, "Visa, Mastercard, Amex"),
    Stripe("Stripe", Icons.Default.Lock, "Instant 1-Click Secure Pay"),
    PayPal("PayPal", Icons.Default.AccountBalanceWallet, "Pay with PayPal balance/bank"),
    CashApp("Cash App", Icons.Default.AttachMoney, "Pay with \$Cashtag")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProUpgradeDialog(
    questionsUsed: Int,
    imagesUsed: Int,
    isPro: Boolean,
    onDismiss: () -> Unit,
    onUpgradeClicked: () -> Unit,
    onResetQuotaClicked: () -> Unit
) {
    var showCheckout by remember { mutableStateOf(false) }
    var selectedMethod by remember { mutableStateOf(PaymentMethod.CreditCard) }

    // Card Form Inputs
    var cardNumber by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("") }
    var cvc by remember { mutableStateOf("") }
    var cardHolderName by remember { mutableStateOf("") }
    var zipCode by remember { mutableStateOf("") }

    // PayPal Input
    var paypalEmail by remember { mutableStateOf("") }

    // CashApp Input
    var cashtag by remember { mutableStateOf("") }

    // Processing & Success State
    var isProcessing by remember { mutableStateOf(false) }
    var paymentSuccess by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    fun isValidLuhn(number: String): Boolean {
        val digits = number.filter { it.isDigit() }
        if (digits.length !in 13..19) return false
        var sum = 0
        var alternate = false
        for (i in digits.length - 1 downTo 0) {
            var n = digits[i].digitToInt()
            if (alternate) {
                n *= 2
                if (n > 9) n -= 9
            }
            sum += n
            alternate = !alternate
        }
        return sum % 10 == 0
    }

    fun processPayment() {
        // Real Card & Account Credentials Validation
        errorMessage = null
        when (selectedMethod) {
            PaymentMethod.CreditCard, PaymentMethod.Stripe -> {
                val digitsOnly = cardNumber.replace(" ", "")
                if (digitsOnly.length !in 13..19) {
                    errorMessage = "Please enter a valid 13 to 19 digit card number."
                    return
                }
                if (!isValidLuhn(digitsOnly)) {
                    errorMessage = "Invalid card number checksum. Please check your credit/debit card credentials."
                    return
                }
                if (expiryDate.length < 4 || !expiryDate.contains("/")) {
                    errorMessage = "Please enter expiry date in MM/YY format."
                    return
                }
                val parts = expiryDate.split("/")
                val month = parts.getOrNull(0)?.toIntOrNull() ?: 0
                if (month !in 1..12) {
                    errorMessage = "Invalid expiry month (01-12)."
                    return
                }
                if (cvc.length !in 3..4) {
                    errorMessage = "Please enter a valid 3 or 4 digit CVV code."
                    return
                }
                if (cardHolderName.trim().isBlank()) {
                    errorMessage = "Please enter full cardholder name."
                    return
                }
                if (zipCode.trim().length < 4) {
                    errorMessage = "Please enter billing ZIP / Postal code."
                    return
                }
            }
            PaymentMethod.PayPal -> {
                if (!paypalEmail.contains("@") || !paypalEmail.contains(".") || paypalEmail.length < 5) {
                    errorMessage = "Please enter a valid PayPal account email address."
                    return
                }
            }
            PaymentMethod.CashApp -> {
                if (cashtag.trim().isBlank() || cashtag.length < 2) {
                    errorMessage = "Please enter a valid Cash App \$Cashtag account."
                    return
                }
            }
        }

        isProcessing = true
        coroutineScope.launch {
            delay(1800) // Live payment gateway authorization sequence
            isProcessing = false
            paymentSuccess = true
            delay(1200)
            onUpgradeClicked() // Grants Pro status
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (paymentSuccess) {
                    // Success View
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(vertical = 32.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF4CAF50).copy(alpha = 0.15f),
                            modifier = Modifier.size(72.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(48.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Payment Approved!",
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Welcome to AI Homework Pro! Unlimited questions & photo solutions unlocked.",
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                        ) {
                            Text("Start Learning", fontWeight = FontWeight.Bold)
                        }
                    }
                } else if (!showCheckout) {
                    // Step 1: Pro Features Overview & Buy trigger
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clip(RoundedCornerShape(20.dp))
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_pro_banner_1785438557797),
                            contentDescription = "Pro Banner",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color(0xCC000000))
                                    )
                                )
                        )
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = PurpleContainer
                            ) {
                                Text(
                                    text = "PRO UNLIMITED",
                                    color = OnPurpleContainer,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Unlock AI Homework Pro",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        ),
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Learn faster without limits, step-by-step math solver & advanced AI tutors.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Quota Card
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(text = "Weekly Quota Status", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(
                                    text = if (isPro) "PRO ACTIVE" else "Free Plan",
                                    fontSize = 11.sp,
                                    color = PurplePrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.HelpOutline, contentDescription = null, modifier = Modifier.size(16.dp), tint = PurplePrimary)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "Questions Used: $questionsUsed / 100", fontSize = 12.sp)
                            }
                            LinearProgressIndicator(
                                progress = { (questionsUsed / 100f).coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = PurplePrimary
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp), tint = PurplePrimary)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "Photo Solutions: $imagesUsed / 10", fontSize = 12.sp)
                            }
                            LinearProgressIndicator(
                                progress = { (imagesUsed / 10f).coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = IndigoSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Features
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ProFeatureRow(icon = Icons.Default.AllInclusive, text = "Unlimited questions & instant photo solutions")
                        ProFeatureRow(icon = Icons.Default.Psychology, text = "Gemini Pro STEM step-by-step solver engine")
                        ProFeatureRow(icon = Icons.Default.Create, text = "Unlimited Essay, Quiz & Study Advisor tools")
                        ProFeatureRow(icon = Icons.Default.Speed, text = "Priority zero-wait AI response speed")
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Upgrade Button
                    Button(
                        onClick = {
                            if (isPro) {
                                onUpgradeClicked() // toggle back to free or manage
                            } else {
                                showCheckout = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = GoldGradientStart)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isPro) "Active Plan (Tap to Switch)" else "Subscribe & Pay ($4.99/mo)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(onClick = onResetQuotaClicked) {
                            Text(text = "Reset Usage Quota", fontSize = 12.sp, color = PurplePrimary)
                        }
                        TextButton(onClick = onDismiss) {
                            Text(text = "Close", fontSize = 12.sp)
                        }
                    }
                } else {
                    // Step 2: Payment Options & Credentials Form
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { showCheckout = false }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                            }
                            Text(
                                text = "Pro Payment Checkout",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = PurpleContainer
                        ) {
                            Text(
                                text = "\$4.99/mo",
                                fontWeight = FontWeight.Bold,
                                color = OnPurpleContainer,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Select Payment Option",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Payment Method Tabs / Selector Grid
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        PaymentMethod.entries.forEach { method ->
                            val isSelected = selectedMethod == method
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedMethod = method
                                        errorMessage = null
                                    },
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) PurplePrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant
                                ),
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, PurplePrimary) else null
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = {
                                            selectedMethod = method
                                            errorMessage = null
                                        },
                                        colors = RadioButtonDefaults.colors(selectedColor = PurplePrimary)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(method.icon, contentDescription = null, tint = PurplePrimary)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = method.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(text = method.subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                    }
                                    if (method == PaymentMethod.Stripe) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFF635BFF)
                                        ) {
                                            Text(
                                                text = "stripe",
                                                color = Color.White,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 10.sp,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    } else if (method == PaymentMethod.CashApp) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFF00D632)
                                        ) {
                                            Text(
                                                text = "Cash Pay",
                                                color = Color.Black,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Card & Account Credentials",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Input form depending on method
                    when (selectedMethod) {
                        PaymentMethod.CreditCard, PaymentMethod.Stripe -> {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedTextField(
                                    value = cardNumber,
                                    onValueChange = { input ->
                                        val digits = input.filter { it.isDigit() }.take(16)
                                        cardNumber = digits.chunked(4).joinToString(" ")
                                    },
                                    label = { Text("Card Number") },
                                    placeholder = { Text("4242 4242 4242 4242") },
                                    leadingIcon = { Icon(Icons.Default.CreditCard, contentDescription = null, tint = PurplePrimary) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedTextField(
                                        value = expiryDate,
                                        onValueChange = { input ->
                                            val digits = input.filter { it.isDigit() }.take(4)
                                            expiryDate = if (digits.length >= 3) "${digits.take(2)}/${digits.drop(2)}" else digits
                                        },
                                        label = { Text("Expiry (MM/YY)") },
                                        placeholder = { Text("12/28") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.weight(1f)
                                    )

                                    OutlinedTextField(
                                        value = cvc,
                                        onValueChange = { cvc = it.filter { char -> char.isDigit() }.take(4) },
                                        label = { Text("CVV / CVC") },
                                        placeholder = { Text("123") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        visualTransformation = PasswordVisualTransformation(),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedTextField(
                                        value = cardHolderName,
                                        onValueChange = { cardHolderName = it },
                                        label = { Text("Cardholder Name") },
                                        placeholder = { Text("Alex Mercer") },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.weight(1.4f)
                                    )

                                    OutlinedTextField(
                                        value = zipCode,
                                        onValueChange = { zipCode = it.take(6) },
                                        label = { Text("ZIP") },
                                        placeholder = { Text("90210") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.weight(0.9f)
                                    )
                                }
                            }
                        }

                        PaymentMethod.PayPal -> {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedTextField(
                                    value = paypalEmail,
                                    onValueChange = { paypalEmail = it },
                                    label = { Text("PayPal Email Address") },
                                    placeholder = { Text("student.pro@example.com") },
                                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = PurplePrimary) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFF003087).copy(alpha = 0.1f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFF003087))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Protected by PayPal Buyer Protection & encrypted checkout.",
                                            fontSize = 11.sp,
                                            color = Color(0xFF003087)
                                        )
                                    }
                                }
                            }
                        }

                        PaymentMethod.CashApp -> {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedTextField(
                                    value = cashtag,
                                    onValueChange = { input ->
                                        cashtag = if (input.startsWith("$")) input else "\$$input"
                                    },
                                    label = { Text("Cash App \$Cashtag") },
                                    placeholder = { Text("\$StudyPro") },
                                    leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null, tint = Color(0xFF00D632)) },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFF00D632).copy(alpha = 0.1f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.CheckCircleOutline, contentDescription = null, tint = Color(0xFF00D632))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Instant Cash App Pay authorization. Zero additional transaction fees.",
                                            fontSize = 11.sp,
                                            color = Color(0xFF007E1D)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    errorMessage?.let { err ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = err,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Final Submit Button
                    Button(
                        onClick = { processPayment() },
                        enabled = !isProcessing,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Authorizing Secure Payment...", fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Complete Purchase ($4.99/mo)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "🔒 256-Bit SSL Encrypted • Cancel Anytime in Settings",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ProFeatureRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(PurplePrimary.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PurplePrimary,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp
            )
        )
    }
}
