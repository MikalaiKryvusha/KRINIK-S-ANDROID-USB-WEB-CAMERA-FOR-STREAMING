/**
 * OverlayTestImage — программно рисует тестовый PNG-оверлей для первого захода мульти-источников
 * (Idea 19, Q1=A). Нужен, чтобы доказать пайплайн компоновки БЕЗ файлов/SAF и БЕЗ рук Криника:
 * добавляем такой слой-картинку поверх (виртуальной) камеры и через ADB-скриншот убеждаемся, что
 * оверлей реально лёг в кадр (и в превью, и в энкодер). Реальный выбор PNG из файла (SAF) — следующий
 * шаг; здесь самодостаточный битмап.
 *
 * Рисуем на ПРОЗРАЧНОМ полотне 16:9 яркий бренд-бейдж (#FF1A8C) с текстом в левом-верхнем углу и
 * тонкую рамку по периметру — так оверлей однозначно виден поверх тест-паттерна виртуалки.
 */

package com.kriniks.kcam.feature.streaming.scene

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF

object OverlayTestImage {

    // Цвет бренда (acid pink) — см. стиль кода в AGENT_GUIDE.
    private const val BRAND = 0xFFFF1A8C.toInt()
    private const val W = 1920
    private const val H = 1080

    /** Полнокадровый прозрачный битмап с бренд-бейджем и рамкой — тестовый оверлей. */
    fun render(): Bitmap {
        val bmp = Bitmap.createBitmap(W, H, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        // Прозрачный фон — видна камера под оверлеем.
        canvas.drawColor(Color.TRANSPARENT)

        // Рамка по периметру кадра (чтобы было видно, что оверлей растянут на весь кадр).
        val border = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 10f
            color = BRAND
        }
        canvas.drawRect(20f, 20f, W - 20f, H - 20f, border)

        // Бейдж в левом-верхнем углу.
        val badge = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = BRAND }
        val badgeRect = RectF(60f, 60f, 720f, 220f)
        canvas.drawRoundRect(badgeRect, 28f, 28f, badge)

        // Текст бейджа.
        val text = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 84f
            isFakeBoldText = true
        }
        canvas.drawText("KrinikCam · overlay", 96f, 168f, text)
        return bmp
    }
}
