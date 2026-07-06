package com.uc.caffeine.widget

import android.content.Context
import android.widget.Toast
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import com.uc.caffeine.R
import com.uc.caffeine.data.CaffeineDatabase
import com.uc.caffeine.data.HealthConnectManager
import com.uc.caffeine.data.SettingsRepository
import com.uc.caffeine.data.model.ConsumptionEntry
import com.uc.caffeine.data.model.DEFAULT_CONSUMPTION_DURATION_MINUTES
import com.uc.caffeine.util.resolvedZoneId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class QuickLogAction : ActionCallback {
    companion object {
        val KEY_PRESET_ID = ActionParameters.Key<String>("preset_id")
        val KEY_DRINK_NAME = ActionParameters.Key<String>("drink_name")
        val KEY_CAFFEINE_MG = ActionParameters.Key<Int>("caffeine_mg")
        val KEY_QUANTITY = ActionParameters.Key<Double>("quantity")
        val KEY_UNIT_KEY = ActionParameters.Key<String>("unit_key")
        val KEY_UNIT_CAFFEINE_MG = ActionParameters.Key<Double>("unit_caffeine_mg")
        val KEY_EMOJI = ActionParameters.Key<String>("emoji")
        val KEY_IMAGE_NAME = ActionParameters.Key<String>("image_name")
        val KEY_ABSORPTION_RATE = ActionParameters.Key<Int>("absorption_rate")
        val KEY_DURATION_MINUTES = ActionParameters.Key<Int>("duration_minutes")
    }

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        val entry = ConsumptionEntry(
            drinkName = parameters[KEY_DRINK_NAME] ?: return,
            caffeineMg = parameters[KEY_CAFFEINE_MG] ?: return,
            emoji = parameters[KEY_EMOJI] ?: "☕",
            presetItemId = parameters[KEY_PRESET_ID] ?: "",
            quantity = parameters[KEY_QUANTITY] ?: 1.0,
            unitKey = parameters[KEY_UNIT_KEY] ?: "",
            unitCaffeineMg = parameters[KEY_UNIT_CAFFEINE_MG] ?: 0.0,
            imageName = parameters[KEY_IMAGE_NAME] ?: "",
            absorptionRate = parameters[KEY_ABSORPTION_RATE] ?: 45,
            startedAtMillis = System.currentTimeMillis(),
            durationMinutes = parameters[KEY_DURATION_MINUTES] ?: DEFAULT_CONSUMPTION_DURATION_MINUTES,
        )

        withContext(Dispatchers.IO) {
            val db = CaffeineDatabase.getDatabase(context)
            val newId = db.consumptionLogDao().logDrink(entry)
            val settings = SettingsRepository(context).settingsFlow.first()
            if (settings.healthConnectEnabled) {
                // An HC failure must not crash the Glance action callback
                runCatching {
                    HealthConnectManager(context).writeEntry(entry.copy(id = newId.toInt()), settings.resolvedZoneId())
                }
            }
        }

        withContext(Dispatchers.Main) {
            Toast.makeText(context, context.getString(R.string.widget_quick_log_toast), Toast.LENGTH_SHORT).show()
        }

        // Refresh all widget instances so the new drink is reflected immediately
        CaffeineWidgetUpdater.update(context)
    }
}
