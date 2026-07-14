package com.lernhero.home.dnd

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/** A registered drop zone. [bounds] are in root coordinates. */
internal data class DropTargetInfo<T>(
    val key: Any,
    val bounds: Rect,
    val canAccept: (T?) -> Boolean,
    val onDrop: (T) -> Unit
)

/**
 * Holds the state of a single in-app drag-and-drop interaction (one drag at a time).
 *
 * The dragged "ghost" preview is rendered by [DragAndDropContainer] at [previewTopLeft];
 * sources register via [Modifier.dragSource] and zones via [Modifier.dropTarget].
 */
class DragAndDropState<T> internal constructor(
    private val scope: CoroutineScope
) {
    var draggedItem by mutableStateOf<T?>(null)
        private set

    /** True from lift until the pointer is released. Sources use this to fade themselves. */
    var isDragging by mutableStateOf(false)
        private set

    /** True while the ghost preview should be drawn (includes the snap-back animation). */
    var isPreviewVisible by mutableStateOf(false)
        private set

    /** Key of the drop zone currently under the pointer (that also accepts the item), or null. */
    var hoveredTargetKey by mutableStateOf<Any?>(null)
        private set

    internal var dragPreview by mutableStateOf<(@Composable () -> Unit)?>(null)
        private set

    internal var itemSize by mutableStateOf(IntSize.Zero)
        private set

    /** Top-left of the ghost preview in root coordinates (animated on snap-back). */
    internal val previewTopLeft = Animatable(Offset.Zero, Offset.VectorConverter)

    /**
     * Root-space position of the [DragAndDropContainer]. Hit-testing happens entirely in root
     * coordinates; the container subtracts this when rendering the ghost so it lands under the
     * pointer even when the container itself is offset (e.g. below a top app bar).
     */
    internal var containerOffset: Offset = Offset.Zero

    private var origin = Offset.Zero
    private var pointerOffset = Offset.Zero
    private var accumulated = Offset.Zero

    private val targets = mutableMapOf<Any, DropTargetInfo<T>>()

    internal fun registerTarget(target: DropTargetInfo<T>) {
        targets[target.key] = target
    }

    internal fun unregisterTarget(key: Any) {
        targets.remove(key)
        if (hoveredTargetKey == key) hoveredTargetKey = null
    }

    internal fun startDrag(
        item: T,
        preview: @Composable () -> Unit,
        originTopLeft: Offset,
        pointerOffsetInItem: Offset,
        size: IntSize
    ) {
        draggedItem = item
        dragPreview = preview
        origin = originTopLeft
        pointerOffset = pointerOffsetInItem
        itemSize = size
        accumulated = Offset.Zero
        hoveredTargetKey = null
        isDragging = true
        isPreviewVisible = true
        scope.launch { previewTopLeft.snapTo(originTopLeft) }
    }

    internal fun onDrag(delta: Offset) {
        accumulated += delta
        val newTopLeft = origin + accumulated
        scope.launch { previewTopLeft.snapTo(newTopLeft) }
        hoveredTargetKey = targetAt(newTopLeft + pointerOffset)?.key
    }

    internal fun endDrag() {
        isDragging = false
        val pointer = previewTopLeft.value + pointerOffset
        val target = targetAt(pointer)
        val item = draggedItem
        if (target != null && item != null) {
            target.onDrop(item)
            clear()
        } else {
            snapBack()
        }
    }

    internal fun cancelDrag() {
        isDragging = false
        snapBack()
    }

    private fun snapBack() {
        scope.launch {
            previewTopLeft.animateTo(
                targetValue = origin,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
            clear()
        }
    }

    private fun clear() {
        isPreviewVisible = false
        draggedItem = null
        dragPreview = null
        hoveredTargetKey = null
    }

    private fun targetAt(pointer: Offset): DropTargetInfo<T>? =
        targets.values.firstOrNull { target ->
            target.bounds.contains(pointer) && target.canAccept(draggedItem)
        }
}

@Composable
fun <T> rememberDragAndDropState(): DragAndDropState<T> {
    val scope = rememberCoroutineScope()
    return remember { DragAndDropState(scope) }
}
