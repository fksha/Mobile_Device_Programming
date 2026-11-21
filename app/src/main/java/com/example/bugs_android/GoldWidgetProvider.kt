
package com.example.bugs_android

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.bugs_android.network.GoldRateRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GoldWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {



            val views = RemoteViews(
                context.packageName,
                R.layout.gold_widget
            )

            val intent = Intent(context, MainActivity::class.java)

            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
            )

            views.setOnClickPendingIntent(
                R.id.gold_rate_text,
                pendingIntent
            )

            views.setTextViewText(
                R.id.gold_rate_text,
                "Золото\nЗагрузка..."
            )

            appWidgetManager.updateAppWidget(
                appWidgetId,
                views
            )

            CoroutineScope(Dispatchers.IO).launch {

                val rate = GoldRateRepository.getGoldRate()

                val text = if (rate != null) {
                    "Золото\n${rate.buy} ₽/г"
                } else {
                    "Золото\nНет данных"
                }

                val updateViews = RemoteViews(
                    context.packageName,
                    R.layout.gold_widget
                )

                updateViews.setOnClickPendingIntent(
                    R.id.gold_rate_text,
                    pendingIntent
                )

                updateViews.setOnClickPendingIntent(
                    R.id.gold_rate_text,
                    pendingIntent
                )

                updateViews.setTextViewText(
                    R.id.gold_rate_text,
                    text
                )

                appWidgetManager.updateAppWidget(
                    appWidgetId,
                    updateViews
                )
            }
        }
    }
}
