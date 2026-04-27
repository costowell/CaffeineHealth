package com.uc.caffeine.ui.components

import com.uc.caffeine.util.ChartConsumptionMarker
import com.uc.caffeine.util.CaffeineDataPoint
import com.uc.caffeine.util.ChartData
import org.junit.Assert.assertEquals
import org.junit.Test

class CaffeineChartTest {

    @Test
    fun buildHomeChartDisplaySeries_keepsRegularSampleGridForLineSeries() {
        val series = buildHomeChartDisplaySeries(
            chartData = testChartData(),
            liveNowMillis = FIVE_MINUTES_MILLIS + 123L,
        )

        assertEquals(listOf(0.0, 1.0, 2.0), series.xValues)
        assertEquals(listOf(40.0, 20.0, 0.0), series.yValues)
    }

    @Test
    fun buildHomeChartDisplaySeries_keepsLiveMarkerAtExactCurrentTime() {
        val series = buildHomeChartDisplaySeries(
            chartData = testChartData(),
            liveNowMillis = FIVE_MINUTES_MILLIS + 123L,
        )

        assertEquals(0.33347, series.currentTimeX, 0.00001)
    }

    private fun testChartData(): ChartData {
        return ChartData(
            dataPoints = listOf(
                CaffeineDataPoint(
                    timestampMillis = 0L,
                    caffeineLevel = 40.0,
                    isHistorical = true,
                ),
                CaffeineDataPoint(
                    timestampMillis = FIFTEEN_MINUTES_MILLIS,
                    caffeineLevel = 20.0,
                    isHistorical = true,
                ),
                CaffeineDataPoint(
                    timestampMillis = THIRTY_MINUTES_MILLIS,
                    caffeineLevel = 0.0,
                    isHistorical = false,
                ),
            ),
            consumptionMarkers = listOf(
                ChartConsumptionMarker(
                    xValue = 0.0,
                    emojiLabel = "\u2615",
                    timestampMillis = 0L,
                )
            ),
            thresholdLevel = 60.0,
            bedtimeMillis = THIRTY_MINUTES_MILLIS,
            currentTimeMillis = FIFTEEN_MINUTES_MILLIS,
            domainStartMillis = 0L,
        )
    }

    private companion object {
        const val FIVE_MINUTES_MILLIS = 5 * 60 * 1000L
        const val FIFTEEN_MINUTES_MILLIS = 15 * 60 * 1000L
        const val THIRTY_MINUTES_MILLIS = 30 * 60 * 1000L
    }
}
