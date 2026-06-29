/**
 * StreamLayersOverlay — модальная панель «Слои/Сцена» (Idea 19, Q3=A).
 *
 * Показывает слои текущей сцены СВЕРХУ ВНИЗ (верхний в списке = поверх остальных в кадре, как в OBS).
 * По каждому слою: «глаз» (вкл/выкл видимость), стрелки порядка (z-order), удаление (для оверлеев —
 * камеру в первом заходе не удаляем). Внизу — кнопка добавить тестовый PNG-оверлей (первый заход
 * доказывает пайплайн без файлов/SAF; выбор реального PNG из файла — следующий шаг).
 *
 * Вьюфайндер под панелью показывает результат компоновки вживую (превью = тот же GL, что и энкодер).
 *
 * Related: StreamViewModel, Scene/Layer (scene package), SceneCompositor
 */

package com.kriniks.kcam.feature.streaming.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kriniks.kcam.feature.streaming.scene.Layer
import com.kriniks.kcam.feature.streaming.scene.Scene

private val AcidPink = Color(0xFFFF1A8C)
private val DarkSurface = Color(0xFF1A1A1A)
private val CardSurface = Color(0xFF232323)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StreamLayersOverlay(
    scene: Scene,
    onDismiss: () -> Unit,
    onAddTestOverlay: () -> Unit,
    onToggleVisible: (String) -> Unit,
    onRemove: (String) -> Unit,
    onMoveUp: (String) -> Unit,
    onMoveDown: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
        ) {
            // Заголовок панели.
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Layers, contentDescription = null, tint = AcidPink)
                Spacer(Modifier.width(10.dp))
                Text("Scene layers", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(4.dp))
            Text(
                "Top of the list is drawn on top of the frame",
                color = Color(0xFF999999), fontSize = 12.sp,
            )
            Spacer(Modifier.height(16.dp))

            // Список слоёв СВЕРХУ ВНИЗ: scene.layers идёт снизу вверх по z-order → разворачиваем.
            val topToBottom = scene.layers.asReversed()
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.heightIn(max = 360.dp),
            ) {
                itemsIndexed(topToBottom, key = { _, layer -> layer.id }) { index, layer ->
                    LayerRow(
                        layer = layer,
                        // index в развёрнутом списке: 0 = самый верхний. «Вверх» по экрану = выше z-order.
                        isTop = index == 0,
                        isBottom = index == topToBottom.lastIndex,
                        onToggleVisible = { onToggleVisible(layer.id) },
                        onRemove = { onRemove(layer.id) },
                        onMoveUp = { onMoveUp(layer.id) },
                        onMoveDown = { onMoveDown(layer.id) },
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            // Добавить тестовый оверлей (первый заход).
            Button(
                onClick = onAddTestOverlay,
                colors = ButtonDefaults.buttonColors(containerColor = AcidPink),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Add image overlay (test)")
            }
        }
    }
}

/** Одна строка слоя: имя/тип, «глаз», стрелки порядка, удаление (оверлеи). */
@Composable
private fun LayerRow(
    layer: Layer,
    isTop: Boolean,
    isBottom: Boolean,
    onToggleVisible: () -> Unit,
    onRemove: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
) {
    val isCamera = layer is Layer.Camera
    Surface(color = CardSurface, shape = RoundedCornerShape(12.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            // Иконка типа слоя.
            Icon(
                imageVector = if (isCamera) Icons.Default.Videocam else Icons.Default.Image,
                contentDescription = null,
                tint = if (layer.visible) Color.White else Color(0xFF666666),
            )
            Spacer(Modifier.width(12.dp))
            Text(
                layer.name,
                color = if (layer.visible) Color.White else Color(0xFF888888),
                fontSize = 15.sp,
                modifier = Modifier.weight(1f),
            )

            // Порядок (z-order): вверх/вниз. Камера-низ обычно не двигается, но не блокируем явно —
            // стрелки сами гаснут на границах.
            IconButton(onClick = onMoveUp, enabled = !isTop) {
                Icon(Icons.Default.KeyboardArrowUp, "Move up",
                    tint = if (isTop) Color(0xFF555555) else Color.White)
            }
            IconButton(onClick = onMoveDown, enabled = !isBottom) {
                Icon(Icons.Default.KeyboardArrowDown, "Move down",
                    tint = if (isBottom) Color(0xFF555555) else Color.White)
            }
            // Видимость.
            IconButton(onClick = onToggleVisible) {
                Icon(
                    if (layer.visible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = "Toggle visibility",
                    tint = if (layer.visible) AcidPink else Color(0xFF888888),
                )
            }
            // Удаление — только для НЕ-камеры (камеру в первом заходе не убираем).
            if (!isCamera) {
                IconButton(onClick = onRemove) {
                    Icon(Icons.Default.Delete, "Remove", tint = Color(0xFFCC5555))
                }
            }
        }
    }
}
