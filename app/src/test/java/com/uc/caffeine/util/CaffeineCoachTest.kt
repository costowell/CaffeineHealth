package com.uc.caffeine.util

import com.uc.caffeine.data.model.ConsumptionEntry
import java.time.Instant
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CaffeineCoachTest {

    private val zone = ZoneId.of("UTC")

    private fun millis(iso: String): Long = Instant.parse(iso).toEpochMilli()

    private fun input(
        now: String,
        wake: String,
        bedtime: String = "2026-06-16T23:00:00Z",
        lastNightSleepMinutes: Int? = 480,
        entries: List<ConsumptionEntry> = emptyList(),
        sleepThresholdMg: Int = 60,
        halfLifeMinutes: Int = 300,
    ) = CoachInput(
        nowMillis = millis(now),
        entries = entries,
        halfLifeMinutes = halfLifeMinutes,
        absorptionRateMinutes = 45,
        sleepThresholdMg = sleepThresholdMg,
        bedtimeMillis = millis(bedtime),
        wakeMillis = millis(wake),
        lastNightSleepMinutes = lastNightSleepMinutes,
        zoneId = zone,
    )

    // ── Alertness model invariants ────────────────────────────────────────────

    @Test
    fun caffeine_raises_alertness() {
        val inp = input(now = "2026-06-16T14:30:00Z", wake = "2026-06-16T07:00:00Z")
        val t = millis("2026-06-16T14:30:00Z")
        val without = CaffeineCoach.alertnessAt(t, inp, sleepDebtMinutes = 0, caffeineLoadMg = 0.0)
        val with = CaffeineCoach.alertnessAt(t, inp, sleepDebtMinutes = 0, caffeineLoadMg = 100.0)
        assertTrue("caffeine should raise alertness", with > without)
    }

    @Test
    fun sleep_debt_lowers_alertness() {
        val inp = input(now = "2026-06-16T10:00:00Z", wake = "2026-06-16T07:00:00Z")
        val t = millis("2026-06-16T10:00:00Z")
        val rested = CaffeineCoach.alertnessAt(t, inp, sleepDebtMinutes = 0, caffeineLoadMg = 0.0)
        val deprived = CaffeineCoach.alertnessAt(t, inp, sleepDebtMinutes = 300, caffeineLoadMg = 0.0)
        assertTrue("sleep debt should lower alertness", deprived < rested)
    }

    @Test
    fun afternoon_dip_is_lower_than_late_morning_at_equal_time_awake() {
        // Same hours-awake, different clock hour: the post-lunch slot must be lower.
        val morning = input(now = "2026-06-16T10:30:00Z", wake = "2026-06-16T07:00:00Z")
        val afternoon = input(now = "2026-06-16T14:30:00Z", wake = "2026-06-16T11:00:00Z")
        val mAlert = CaffeineCoach.alertnessAt(morning.nowMillis, morning, 0, 0.0)
        val aAlert = CaffeineCoach.alertnessAt(afternoon.nowMillis, afternoon, 0, 0.0)
        assertTrue("post-lunch dip should reduce alertness", aAlert < mAlert)
    }

    // ── Recommendation behaviour ──────────────────────────────────────────────

    @Test
    fun well_rested_evening_holds_no_caffeine_needed() {
        // Late riser, evening, no debt → nothing useful to add before wind-down.
        val rec = CaffeineCoach.recommend(input(now = "2026-06-16T18:00:00Z", wake = "2026-06-16T10:00:00Z"))
        assertTrue(rec is CoachRecommendation.Hold)
        assertEquals(CoachReason.ALREADY_ALERT, (rec as CoachRecommendation.Hold).reason)
    }

    @Test
    fun severe_sleep_debt_recommends_now_for_recovery() {
        val rec = CaffeineCoach.recommend(
            input(now = "2026-06-16T10:00:00Z", wake = "2026-06-16T06:00:00Z", lastNightSleepMinutes = 120)
        )
        assertTrue("should recommend a dose", rec is CoachRecommendation.Recommend)
        rec as CoachRecommendation.Recommend
        assertTrue("dose should be taken now", rec.isNow)
        assertEquals(CoachReason.SLEEP_DEBT, rec.reason)
        assertTrue(rec.personalized)
        assertTrue("dose within sane bounds", rec.doseMg in 20..200)
        // Safety invariant: never push residual over the sleep threshold.
        assertTrue("residual must stay under threshold", rec.residualAtBedtimeMg <= 60)
    }

    @Test
    fun rested_morning_times_a_future_dose_for_the_afternoon_dip() {
        val rec = CaffeineCoach.recommend(input(now = "2026-06-16T09:00:00Z", wake = "2026-06-16T07:00:00Z"))
        assertTrue(rec is CoachRecommendation.Recommend)
        rec as CoachRecommendation.Recommend
        assertFalse("dose should be scheduled for later, not now", rec.isNow)
        assertEquals(CoachReason.AFTERNOON_DIP, rec.reason)
        assertTrue("dose time is timed into the afternoon", rec.atMillis > millis("2026-06-16T11:00:00Z"))
        assertTrue(rec.residualAtBedtimeMg <= 60)
    }

    @Test
    fun past_focus_window_protects_bedtime() {
        // Inside the 2h wind-down before bed → no caffeine.
        val rec = CaffeineCoach.recommend(input(now = "2026-06-16T22:00:00Z", wake = "2026-06-16T07:00:00Z"))
        assertTrue(rec is CoachRecommendation.Hold)
        assertEquals(CoachReason.BEDTIME_PROTECTION, (rec as CoachRecommendation.Hold).reason)
    }

    @Test
    fun without_sleep_data_recommendation_is_not_personalized() {
        val rec = CaffeineCoach.recommend(
            input(now = "2026-06-16T09:00:00Z", wake = "2026-06-16T07:00:00Z", lastNightSleepMinutes = null)
        )
        assertFalse(rec.personalized)
    }

    @Test
    fun existing_caffeine_already_covering_the_day_holds() {
        // A big recent dose keeps predicted alertness up → no extra needed.
        val entry = ConsumptionEntry(
            drinkName = "Coffee",
            caffeineMg = 200,
            emoji = "☕",
            startedAtMillis = millis("2026-06-16T13:00:00Z"),
            durationMinutes = 5,
            absorptionRate = 45,
        )
        val rec = CaffeineCoach.recommend(
            input(now = "2026-06-16T13:30:00Z", wake = "2026-06-16T07:00:00Z", entries = listOf(entry))
        )
        assertTrue(rec is CoachRecommendation.Hold)
    }
}
