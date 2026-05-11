package com.uc.caffeine.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import com.uc.caffeine.R

@Composable
fun CaffeineCircularView(
    currentMg: Double,
    maxMg: Double,
    modifier: Modifier = Modifier,
) {
    val progress = if (maxMg > 0.0) (currentMg / maxMg).toFloat().coerceIn(0f, 1f) else 0f
    val strokeWidthPx = with(LocalDensity.current) { 16.dp.toPx() }
    val indicatorStroke = Stroke(width = strokeWidthPx)

    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        // Size the indicator to fit within whichever dimension is smaller,
        // leaving room for the threshold label below.
        val indicatorSize = min(maxWidth * 0.88f, maxHeight * 0.78f)

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(contentAlignment = Alignment.Center) {
                CircularWavyProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.size(indicatorSize),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    stroke = indicatorStroke,
                    trackStroke = indicatorStroke,
                    gapSize = 8.dp,
                    wavelength = 60.dp,
                    waveSpeed = 60.dp,
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    RollingNumberText(
                        text = "%.0f".format(currentMg),
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        ),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        labelPrefix = "circular_view",
                    )
                    Text(
                        text = stringResource(R.string.home_circular_mg_unit),
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.home_circular_threshold_format, maxMg.toInt()),
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
        }
    }
}
