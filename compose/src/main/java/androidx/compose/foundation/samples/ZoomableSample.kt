/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.compose.foundation.samples


import androidx.animation.*
import androidx.compose.*
import androidx.ui.animation.asDisposableClock
import androidx.ui.core.*
import androidx.ui.core.gesture.ScaleObserver
import androidx.ui.core.gesture.scaleGestureFilter


import androidx.ui.foundation.*
import androidx.ui.foundation.gestures.zoomable
import androidx.ui.graphics.Color
import androidx.ui.layout.fillMaxSize
import androidx.ui.layout.preferredSize
import androidx.ui.unit.dp
import androidx.ui.unit.sp




/**
 * Controller to control [zoomable] modifier with. Provides smooth scaling capabilities.
 *
 * @param animationClock clock observable to run animation on. Consider querying
 * [AnimationClockAmbient] to get current composition value
 * @param onZoomDelta callback to be invoked when pinch/smooth zooming occurs. The callback
 * receives the delta as the ratio of the new size compared to the old. Callers should update
 * their state and UI in this callback.
 */
class ZoomableController(
    animationClock: AnimationClockObservable,
    val onZoomDelta: (Float) -> Unit
) {

    /**
     * Smooth scale by a ratio of [value] over the current size.
     *
     * @param value ratio over the current size by which to scale
     * @param spec [AnimationSpec] to be used for smoothScale animation
     * @pram [onEnd] callback invoked when the smooth scaling has ended
     */
    fun smoothScaleBy(
        value: Float,
        spec: AnimationSpec<Float> = SpringSpec(stiffness = Spring.StiffnessLow),
        onEnd: ((endReason: AnimationEndReason, finishValue: Float) -> Unit)? = null
    ) {
        val to = animatedFloat.value * value
        animatedFloat.animateTo(
            to,
            onEnd = onEnd,
            anim = spec
        )
    }

    /**
     * Stop any ongoing animation or smooth scaling for this controller
     *
     * Call this to stop receiving scrollable deltas in [onZoomDelta]
     */
    fun stopAnimation() {
        animatedFloat.stop()
    }

    internal fun onScale(scaleFactor: Float) = onZoomDelta(scaleFactor)

    private val animatedFloat = DeltaAnimatedScale(1f, animationClock, ::onScale)
}



private class DeltaAnimatedScale(
    initial: Float,
    clock: AnimationClockObservable,
    private val onDelta: (Float) -> Unit
) : AnimatedFloat(clock) {

    override var value = initial
        set(value) {
            if (isRunning) {
                val delta = value / field
                onDelta(delta)
            }
            field = value
        }
}


/**
 * Create and remember [ZoomableController] with default [AnimationClockObservable].
 *
 * @param onZoomDelta callback to be invoked when pinch/smooth zooming occurs. The callback
 * receives the delta as the ratio of the new size compared to the old. Callers should update
 * their state and UI in this callback.
 */
@Composable
fun rememberZoomableController(onZoomDelta: (Float) -> Unit): ZoomableController {
    val clocks = AnimationClockAmbient.current.asDisposableClock()
    return remember(clocks) { ZoomableController(clocks, onZoomDelta) }
}

@Composable
fun ZoomableSample() {
    Box(
        Modifier.preferredSize(300.dp).clipToBounds(),
        backgroundColor = Color.LightGray
    ) {
        var scale by state(structuralEqualityPolicy()) { 1f }

        val zoomableController = rememberZoomableController { scale *= it }
        Box(
            Modifier
                .zoomable(zoomableController)
                .clickable(
                    indication = null,
                    onDoubleClick = { zoomableController.smoothScaleBy(4f) },
                    onClick = {}
                )
                .fillMaxSize()
                .drawBorder(1.dp, Color.Green),
            gravity = ContentGravity.Center
        ) {
            Text(
                "☠",
                fontSize = 32.sp,
                modifier = Modifier.drawLayer(scaleX = scale, scaleY = scale)
            )
        }
    }
}

/**
 * Enable zooming of the modified UI element.
 *
 * [ZoomableController.onZoomDelta] will be invoked with the change in proportion of the UI element's
 * size at each change in either ratio of the gesture or smooth scaling. Callers should update
 * their state and UI in this callback.
 *
 * @sample androidx.compose.foundation.samples.ZoomableSample
 *
 * @param controller [ZoomableController] object that holds the internal state of this zoomable,
 * and provides smooth scaling capabilities.
 * @param enabled whether zooming by gestures is enabled or not
 * @param onZoomStarted callback to be invoked when zoom has started.
 * @param onZoomStopped callback to be invoked when zoom has stopped.
 */
fun Modifier.zoomable(
    controller: ZoomableController,
    enabled: Boolean = true,
    onZoomStarted: (() -> Unit)? = null,
    onZoomStopped: (() -> Unit)? = null
) = composed {
    onDispose {
        controller.stopAnimation()
    }
    scaleGestureFilter(
        scaleObserver = object : ScaleObserver {
            override fun onScale(scaleFactor: Float) {
                if (enabled) {
                    controller.stopAnimation()
                    controller.onScale(scaleFactor)
                }
            }

            override fun onStop() {
                if (enabled) {
                    onZoomStopped?.invoke()
                }
            }

            override fun onCancel() {
                if (enabled) {
                    onZoomStopped?.invoke()
                }
            }

            override fun onStart() {
                if (enabled) {
                    controller.stopAnimation()
                    onZoomStarted?.invoke()
                }
            }
        }
    )
}