package com.example.bugs_android

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.example.bugs_android.fragments.setTextView
import com.example.bugs_android.network.GoldRateRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class GoldWidgetProvider : AppWidgetProvider() {

    override fun  onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray){
        for (appWidgetId in appWidgetIds){
            val views = RemoteViews(
                context.packageName,
                R.layout.gold_widget
            )

            views.setTextViewText(
                R.id.gold_rate_text,
                "Золото\nЗагрузка..."
            )

            appWidgetManager.updateAppWidget(appWidgetId, views)

            CoroutineScope(Dispatchers.IO).launch {
                val rate = GoldRateRepository.getGoldRate()
                val text = if(rate != null){
                    "Золото\n${rate.buy} ₽/г"
                }
                else{
                    "Золото\nНет данных"
                }

                val updateViews = RemoteViews(
                    context.packageName,
                    R.layout.gold_widget
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