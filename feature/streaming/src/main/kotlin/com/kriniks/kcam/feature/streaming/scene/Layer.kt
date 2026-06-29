/**
 * Layer — один источник в рабочей области сцены (фундамент мульти-источников, Idea 19).
 *
 * Аналог слоя в OBS: сцена держит УПОРЯДОЧЕННЫЙ список слоёв, нижний (index 0) рисуется первым,
 * верхние — поверх. Видимость переключается «глазом». Это доменная модель — она НЕ зависит от
 * backend рендера (см. interview_005): сейчас слои-оверлеи кладутся на стек фильтров RootEncoder
 * (`SceneCompositor`), но модель/UI от этого не зависят и переживут смену backend.
 *
 * Первый заход (минимальный вертикальный срез, Q1=A): два типа — [Camera] (базовый слой, сама
 * USB-камера = VideoSource энкодера) и [Image] (PNG-оверлей поверх). Остальные типы из Q2
 * (видео-оверлей, текст, 2-я камера, анимации, заглушка) добавляются по этой же модели позже.
 */

package com.kriniks.kcam.feature.streaming.scene

import android.graphics.Bitmap

sealed interface Layer {
    /** Стабильный идентификатор слоя (для reorder/toggle/remove и будущего сериализованного профиля). */
    val id: String

    /** Человекочитаемое имя для панели «Слои». */
    val name: String

    /** Виден ли слой в компоновке (выключенный «глаз» = слой есть в сцене, но не рисуется). */
    val visible: Boolean

    /**
     * Базовый слой — сама камера (USB UVC). Это НЕ фильтр-оверлей, а нижний слой = VideoSource
     * энкодера; компоновщик его не трогает (камера и так рисуется пайплайном). В списке слоёв
     * присутствует, чтобы стример видел всю сцену целиком. Тоггл/удаление камеры — будущая фаза.
     */
    data class Camera(
        override val id: String = "camera",
        override val name: String = "Camera",
        override val visible: Boolean = true,
    ) : Layer

    /**
     * Слой-картинка (PNG с альфой) поверх камеры — логотип, рамка, «сейчас вернусь». Держит готовый
     * [bitmap]; компоновщик мапит его на `ImageObjectFilterRender`. Пока во весь кадр (трансформа —
     * следующая фаза, она уже поддержана BaseObjectFilterRender.setScale/Position).
     */
    data class Image(
        override val id: String,
        override val name: String,
        override val visible: Boolean = true,
        val bitmap: Bitmap,
    ) : Layer
}
