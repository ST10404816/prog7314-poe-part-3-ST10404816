/**
 * Animation Utilities for Modern UI Interactions
 * 
 * This file provides a comprehensive collection of reusable animation components
 * and modifiers that enhance user experience through meaningful motion design.
 * Following Material Design motion principles and Android animation best practices.
 * 
 * Animation Categories:
 * 1. Interaction Animations (bounceClick, hoverElevation)
 * 2. Attention Animations (pulseAnimation, floatingAnimation)
 * 3. Transition Animations (slideIn/Out variants, expandCollapse)
 * 4. List Animations (staggered item appearance)
 * 5. Mathematical Animations (wave motion, custom curves)
 * 
 * Key Concepts Demonstrated:
 * - Jetpack Compose animation APIs
 * - Custom animation specifications and easing curves
 * - Interaction source monitoring for responsive UI
 * - Infinite transitions for continuous animations
 * - Coordinated animations for complex sequences
 * - Performance optimization through remember and derivedStateOf
 * 
 * Material Design Motion Principles Applied:
 * - Informative: Animations convey hierarchy and spatial relationships
 * - Focused: Motion guides attention without overwhelming
 * - Expressive: Animations reflect brand personality and delight users
 * 
 * Performance Considerations:
 * - Animations are GPU-accelerated where possible
 * - Proper lifecycle management prevents memory leaks
 * - Efficient recomposition through strategic state management
 * - Reasonable duration and easing for smooth 60fps performance
 * 
 * @author Third Year Computer Science Student
 * @version 1.0
 * @see androidx.compose.animation for core animation APIs
 * @see Material Design Motion Guidelines
 */
package com.thriftly.app.ui.animations

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.semantics.Role
import kotlin.math.PI
import kotlin.math.sin

/**
 * Common animation specifications used throughout the app
 * 
 * Provides consistent timing and easing for a cohesive user experience.
 * All animations follow Material Design motion guidelines.
 */
object AnimationSpecs {
    /**
     * Fast animation for quick feedback (e.g., button presses)
     * Duration: 150ms with standard easing
     */
    val Fast = tween<Float>(
        durationMillis = 150,
        easing = FastOutSlowInEasing
    )
    
    /**
     * Standard animation for most UI transitions
     * Duration: 300ms with smooth easing curve
     */
    val Standard = tween<Float>(
        durationMillis = 300,
        easing = FastOutSlowInEasing
    )
    
    /**
     * Slow animation for complex transitions
     * Duration: 450ms with emphasized easing
     */
    val Emphasized = tween<Float>(
        durationMillis = 450,
        easing = EaseInOutCubic
    )
    
    /**
     * Spring animation for bouncy, natural feeling motion
     * Uses medium damping ratio for balanced bounce
     */
    val Spring = spring<Float>(
        dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
        stiffness = androidx.compose.animation.core.Spring.StiffnessMedium
    )
    
    /**
     * Gentle spring for subtle movements
     * Lower bounce for more refined animations
     */
    val SpringGentle = spring<Float>(
        dampingRatio = androidx.compose.animation.core.Spring.DampingRatioLowBouncy,
        stiffness = androidx.compose.animation.core.Spring.StiffnessLow
    )
}

/**
 * Modifier that adds a bounce animation when the component is pressed
 * 
 * Creates engaging feedback for interactive elements by slightly scaling
 * down when pressed and bouncing back when released.
 * 
 * @param onClick Callback for click events
 * @param enabled Whether the click interaction is enabled
 * @param role Semantic role for accessibility
 */
fun Modifier.bounceClick(
    onClick: () -> Unit,
    enabled: Boolean = true,
    role: Role? = null
) = composed(
    inspectorInfo = debugInspectorInfo {
        name = "bounceClick"
        properties["enabled"] = enabled
        properties["role"] = role
    }
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = AnimationSpecs.SpringGentle,
        label = "bounce_scale"
    )
    
    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            interactionSource = interactionSource,
            indication = null, // We provide custom visual feedback
            enabled = enabled,
            role = role,
            onClick = onClick
        )
}

/**
 * Modifier that adds a subtle hover elevation effect
 * 
 * Provides visual feedback by slightly lifting the element when hovered
 * or pressed, creating depth perception.
 * 
 * @param pressedElevation Elevation when pressed (default 2dp)
 * @param hoveredElevation Elevation when hovered (default 4dp)
 */
fun Modifier.hoverElevation(
    pressedElevation: Float = 2f,
    hoveredElevation: Float = 4f
) = composed(
    inspectorInfo = debugInspectorInfo {
        name = "hoverElevation"
        properties["pressedElevation"] = pressedElevation
        properties["hoveredElevation"] = hoveredElevation
    }
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()
    
    val elevation by animateFloatAsState(
        targetValue = when {
            isPressed -> pressedElevation
            isHovered -> hoveredElevation
            else -> 0f
        },
        animationSpec = AnimationSpecs.Fast,
        label = "hover_elevation"
    )
    
    this.graphicsLayer {
        shadowElevation = elevation
    }
}

/**
 * Modifier that creates a gentle pulsing animation
 * 
 * Useful for drawing attention to important elements or indicating
 * loading states with a subtle breathing effect.
 * 
 * @param minAlpha Minimum opacity during pulse
 * @param maxAlpha Maximum opacity during pulse
 * @param durationMillis Duration of one pulse cycle
 */
fun Modifier.pulseAnimation(
    minAlpha: Float = 0.3f,
    maxAlpha: Float = 1f,
    durationMillis: Int = 1000
) = composed(
    inspectorInfo = debugInspectorInfo {
        name = "pulseAnimation"
        properties["minAlpha"] = minAlpha
        properties["maxAlpha"] = maxAlpha
        properties["durationMillis"] = durationMillis
    }
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = maxAlpha,
        targetValue = minAlpha,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )
    
    this.graphicsLayer { this.alpha = alpha }
}

/**
 * Modifier that creates a floating animation effect
 * 
 * Adds a gentle up-and-down motion that makes elements feel lighter
 * and more dynamic. Perfect for floating action buttons or hero elements.
 * 
 * @param amplitude How far the element moves (in pixels)
 * @param durationMillis Duration of one complete float cycle
 */
fun Modifier.floatingAnimation(
    amplitude: Float = 10f,
    durationMillis: Int = 2000
) = composed(
    inspectorInfo = debugInspectorInfo {
        name = "floatingAnimation"
        properties["amplitude"] = amplitude
        properties["durationMillis"] = durationMillis
    }
) {
    val infiniteTransition = rememberInfiniteTransition(label = "floating")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = amplitude,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float_offset"
    )
    
    this.graphicsLayer {
        translationY = offsetY
    }
}

/**
 * Animated visibility with slide in from bottom
 * 
 * Common pattern for showing new content that slides up from the bottom
 * of the screen with a fade in effect.
 */
@Composable
fun SlideInFromBottom(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = slideInVertically(
            initialOffsetY = { fullHeight -> fullHeight },
            animationSpec = tween(durationMillis = 450, easing = EaseInOutCubic)
        ) + fadeIn(animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)),
        exit = slideOutVertically(
            targetOffsetY = { fullHeight -> fullHeight },
            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
        ) + fadeOut(animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing))
    ) {
        content()
    }
}

/**
 * Animated visibility with slide in from right (for navigation)
 * 
 * Provides a natural transition for forward navigation where new
 * content slides in from the right side.
 */
@Composable
fun SlideInFromRight(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = slideInHorizontally(
            initialOffsetX = { fullWidth -> fullWidth },
            animationSpec = tween(durationMillis = 450, easing = EaseInOutCubic)
        ) + fadeIn(animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)),
        exit = slideOutHorizontally(
            targetOffsetX = { fullWidth -> -fullWidth },
            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
        ) + fadeOut(animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing))
    ) {
        content()
    }
}

/**
 * Animated visibility with expand/collapse
 * 
 * Smooth expand and collapse animation for showing/hiding content
 * vertically. Ideal for accordion-style interfaces.
 */
@Composable
fun ExpandCollapse(
    expanded: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = expanded,
        modifier = modifier,
        enter = expandVertically(
            animationSpec = tween(durationMillis = 450, easing = EaseInOutCubic)
        ) + fadeIn(animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)),
        exit = shrinkVertically(
            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
        ) + fadeOut(animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing))
    ) {
        content()
    }
}

/**
 * Wave animation using sine function for organic motion
 * 
 * Creates a smooth sine wave animation useful for water-like effects
 * or organic loading animations.
 * 
 * @param amplitude Wave height
 * @param frequency Wave frequency (higher = more waves)
 * @param speed Animation speed multiplier
 */
@Composable
fun rememberWaveAnimation(
    amplitude: Float = 20f,
    frequency: Float = 2f,
    speed: Float = 1f
): State<Float> {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (2000 / speed).toInt(),
                easing = LinearEasing
            )
        ),
        label = "wave_time"
    )
    
    return derivedStateOf { 
        (sin(time * frequency) * amplitude).toFloat()
    }
}

/**
 * Stagger animation for list items
 * 
 * Creates a cascading animation effect where list items appear
 * one after another with a slight delay.
 * 
 * @param index Item index in the list
 * @param visible Whether the item should be visible
 * @param delayMillis Delay between each item animation
 */
@Composable
fun StaggeredListItem(
    index: Int,
    visible: Boolean,
    delayMillis: Int = 50,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val delay = remember { index * delayMillis }
    var localVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(visible) {
        if (visible) {
            kotlinx.coroutines.delay(delay.toLong())
            localVisible = true
        } else {
            localVisible = false
        }
    }
    
    AnimatedVisibility(
        visible = localVisible,
        modifier = modifier,
        enter = slideInVertically(
            initialOffsetY = { it / 3 },
            animationSpec = tween(durationMillis = 450, easing = EaseInOutCubic)
        ) + fadeIn(animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)),
        exit = fadeOut(animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing))
    ) {
        content()
    }
}

/* References
Google. (n.d.). Compose UI. https://developer.android.com/jetpack/compose
*/