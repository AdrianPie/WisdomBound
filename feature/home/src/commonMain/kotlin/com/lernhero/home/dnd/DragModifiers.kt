package com.lernhero.home.dnd

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.IntSize

/**
 * Makes this composable a drag source. Long-press to lift, then drag the ghost preview.
 * While lifted, the original is faded out. [dataProvider] supplies the payload at lift time,
 * and [preview] is the ghost drawn under the pointer.
 */
fun <T> Modifier.dragSource(
    state: DragAndDropState<T>,
    dataProvider: () -> T,
    preview: @Composable () -> Unit
): Modifier = composed {
    var rootPosition by remember { mutableStateOf(Offset.Zero) }
    var itemSize by remember { mutableStateOf(IntSize.Zero) }
    var selfDragging by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(if (selfDragging) 0f else 1f)

    this
        .onGloballyPositioned { coordinates ->
            rootPosition = coordinates.positionInRoot()
            itemSize = coordinates.size
        }
        .graphicsLayer { this.alpha = alpha }
        .pointerInput(state) {
            detectDragGesturesAfterLongPress(
                onDragStart = { localOffset ->
                    selfDragging = true
                    state.startDrag(
                        item = dataProvider(),
                        preview = preview,
                        originTopLeft = rootPosition,
                        pointerOffsetInItem = localOffset,
                        size = itemSize
                    )
                },
                onDrag = { change, dragAmount ->
                    change.consume()
                    state.onDrag(dragAmount)
                },
                onDragEnd = {
                    selfDragging = false
                    state.endDrag()
                },
                onDragCancel = {
                    selfDragging = false
                    state.cancelDrag()
                }
            )
        }
}

/**
 * Marks this composable as a drop zone identified by [key]. [canAccept] decides whether the
 * currently dragged payload is valid here (drives hover highlight + drop acceptance).
 * Query [DragAndDropState.hoveredTargetKey] == key for highlighting.
 */
fun <T> Modifier.dropTarget(
    state: DragAndDropState<T>,
    key: Any,
    canAccept: (T?) -> Boolean = { true },
    onDrop: (T) -> Unit
): Modifier = composed {
    DisposableEffect(key) {
        onDispose { state.unregisterTarget(key) }
    }
    this.onGloballyPositioned { coordinates ->
        state.registerTarget(
            DropTargetInfo(
                key = key,
                bounds = coordinates.boundsInRoot(),
                canAccept = canAccept,
                onDrop = onDrop
            )
        )
    }
}
