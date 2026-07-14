package com.lernhero.home.dnd

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.zIndex
import kotlin.math.roundToInt

/**
 * Root wrapper for a drag-and-drop area. Lays out [content] and, on top, draws the floating
 * ghost preview following the pointer with a subtle lift (scale/elevation) and snap-back.
 * All [Modifier.dragSource] / [Modifier.dropTarget] users must live inside this container.
 */
@Composable
fun <T> DragAndDropContainer(
    state: DragAndDropState<T>,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier.onGloballyPositioned { state.containerOffset = it.positionInRoot() }
    ) {
        content()

        val preview = state.dragPreview
        if (state.isPreviewVisible && preview != null) {
            val density = LocalDensity.current
            val lift by animateFloatAsState(if (state.isDragging) 1.08f else 1f)
            val size = state.itemSize

            Box(
                modifier = Modifier
                    .zIndex(1000f)
                    .offset {
                        val topLeft = state.previewTopLeft.value - state.containerOffset
                        IntOffset(topLeft.x.roundToInt(), topLeft.y.roundToInt())
                    }
                    .then(
                        if (size != IntSize.Zero) {
                            with(density) {
                                Modifier.size(size.width.toDp(), size.height.toDp())
                            }
                        } else {
                            Modifier
                        }
                    )
                    .graphicsLayer {
                        scaleX = lift
                        scaleY = lift
                        shadowElevation = 16f
                        alpha = 0.96f
                    }
            ) {
                preview()
            }
        }
    }
}
