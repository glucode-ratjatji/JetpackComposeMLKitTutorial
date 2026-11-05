package de.yanneckreiss.mlkittutorial.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.util.Locale

@Composable
fun WeatherScoreBadge(
    score: Double,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Score coloration: Lower is nominal, Higher indicates severe atmospheric load
    val (bgColor, textColor, icon) = when {
        score >= 7.5 -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            "🔥"
        )
        score >= 4.5 -> Triple(
            Color(0xFFFFECB3), // Amber 100
            Color(0xFF8D6E00),
            "⛅"
        )
        else -> Triple(
            Color(0xFFE8F5E9), // Green 50
            Color(0xFF2E7D32),
            "🌱"
        )
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = "$icon ${String.format(Locale.US, "%.1f", score)}",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
