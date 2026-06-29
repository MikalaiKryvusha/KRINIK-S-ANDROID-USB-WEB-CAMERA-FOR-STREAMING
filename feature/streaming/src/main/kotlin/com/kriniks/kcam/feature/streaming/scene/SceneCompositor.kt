/**
 * SceneCompositor — адаптер «доменная [Scene] → backend рендера» (Idea 19, interview_005).
 *
 * Backend компоновки оверлеев = штатный стек фильтров RootEncoder (`GlStreamInterface`):
 *   • базовый слой (камера) рисуется самим пайплайном как VideoSource — компоновщик его не трогает;
 *   • каждый видимый слой-картинка → `ImageObjectFilterRender`, добавленный в стек фильтров;
 *   • порядок добавления = z-order (нижний оверлей добавляется первым), как в [Scene.layers].
 *
 * Применяется идемпотентно: на каждый вызов [apply] полностью пересобираем стек фильтров под
 * текущую сцену (clearFilters → addFilter по порядку). Вызовы редкие (старт превью, готовность GL,
 * старт стрима, правка сцены пользователем) — не на каждый кадр, поэтому пересборка дешева и проста
 * (KISS). Если позже потребуется минимизировать churn — кэшировать рендеры по id слоя.
 *
 * Потокобезопасность: add/clearFilters в RootEncoder сами постятся на GL-поток через рендер-хендлер,
 * звать можно с main. Если GL ещё не запущен — просто выходим (повторно применится на следующем хуке).
 */

package com.kriniks.kcam.feature.streaming.scene

import com.kriniks.kcam.core.logging.KLog
import com.pedro.encoder.input.gl.render.filters.`object`.ImageObjectFilterRender
import com.pedro.library.view.GlStreamInterface

private const val TAG = "SceneCompositor"

object SceneCompositor {

    /**
     * Привести стек фильтров [gl] в соответствие со сценой [scene]: убрать все оверлеи и заново
     * добавить видимые слои-картинки в порядке снизу вверх. No-op до запуска GL.
     */
    fun apply(gl: GlStreamInterface?, scene: Scene) {
        gl ?: return
        try {
            // Полная пересборка стека под текущую сцену.
            gl.clearFilters()
            val overlays = scene.visibleImageOverlays()
            overlays.forEach { layer ->
                val render = ImageObjectFilterRender().apply { setImage(layer.bitmap) }
                // addFilter без индекса = добавить на верх стека; идём снизу вверх → корректный z-order.
                gl.addFilter(render)
            }
            KLog.d(TAG, "apply: ${overlays.size} overlay layer(s) → filter stack (total layers=${scene.layers.size})")
        } catch (e: Exception) {
            KLog.e(TAG, "apply: failed to sync overlay filters", e)
        }
    }
}
