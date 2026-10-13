package com.example.calculator

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalNeumorphicColors
import com.example.ui.theme.OutfitFontFamily
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * High-fidelity Neumorphic Surface box.
 * Produces dual directional soft shadows for an extruded physical feel,
 * and an inset recessed look when [isPressed] is true.
 */
@Composable
fun NeumorphicBox(
    modifier: Modifier = Modifier,
    isPressed: Boolean = false,
    shape: Shape = RoundedCornerShape(20.dp),
    cornerRadius: Dp = 20.dp,
    elevation: Dp = 5.dp,
    highlightColor: Color? = null,
    shadowColor: Color? = null,
    surfaceColor: Color? = null,
    content: @Composable () -> Unit
) {
    val colors = LocalNeumorphicColors.current
    val actualHighlight = highlightColor ?: colors.lightShadow
    val actualShadow = shadowColor ?: colors.darkShadow
    val actualSurface = surfaceColor ?: if (isPressed) colors.surfacePressed else colors.surface

    val animElevation by animateDpAsState(
        targetValue = if (isPressed) 0.dp else elevation,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f),
        label = "neo_elevation"
    )

    val animScale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 500f),
        label = "neo_scale"
    )

    Box(
        modifier = modifier
            .scale(animScale)
    ) {
        if (!isPressed && animElevation > 0.dp) {
            // Dark bottom-right shadow layer
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(x = animElevation, y = animElevation)
                    .clip(shape)
                    .background(actualShadow)
            )

            // Light top-left highlight shadow layer
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(x = -animElevation, y = -animElevation)
                    .clip(shape)
                    .background(actualHighlight)
            )
        }

        // Main Surface body
        val bevelBorderBrush = if (isPressed) {
            Brush.linearGradient(
                colors = listOf(
                    if (colors.isDark) Color(0x66000000) else Color(0x33000000),
                    if (colors.isDark) Color(0x22FFFFFF) else Color(0x66FFFFFF)
                )
            )
        } else {
            Brush.linearGradient(
                colors = listOf(
                    if (colors.isDark) Color(0x26FFFFFF) else Color(0xCCFFFFFF),
                    if (colors.isDark) Color(0x55000000) else Color(0x1F000000)
                )
            )
        }

        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(shape)
                .background(actualSurface)
                .border(
                    width = 1.dp,
                    brush = bevelBorderBrush,
                    shape = shape
                )
        )

        // Internal Content
        Box(
            modifier = Modifier.clip(shape),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

/**
 * Sunken/Recessed Neumorphic Well for display or input containers.
 */
@Composable
fun NeumorphicRecessedBox(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(28.dp),
    content: @Composable () -> Unit
) {
    val colors = LocalNeumorphicColors.current

    val recessedBackground = if (colors.isDark) {
        Color(0xFF1B1E26)
    } else {
        Color(0xFFDCE3ED)
    }

    val insetBorderBrush = Brush.verticalGradient(
        colors = listOf(
            if (colors.isDark) Color(0x800D0F14) else Color(0x33000000),
            if (colors.isDark) Color(0x1AFFFFFF) else Color(0x80FFFFFF)
        )
    )

    Box(
        modifier = modifier
            .clip(shape)
            .background(recessedBackground)
            .border(width = 1.5.dp, brush = insetBorderBrush, shape = shape)
    ) {
        content()
    }
}

enum class ButtonStyleType {
    NUMBER,
    OPERATOR,
    SPECIAL,
    EQUALS
}

@Composable
fun NeumorphicCalculatorButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    styleType: ButtonStyleType = ButtonStyleType.NUMBER,
    isSelectedOperator: Boolean = false,
    enabledHaptics: Boolean = true,
    testTag: String = "btn_$text"
) {
    val colors = LocalNeumorphicColors.current
    val view = LocalView.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val textColor = when {
        isSelectedOperator -> Color.White
        styleType == ButtonStyleType.EQUALS -> Color.White
        styleType == ButtonStyleType.OPERATOR -> colors.accent
        styleType == ButtonStyleType.SPECIAL -> colors.accentSecondary
        else -> colors.textPrimary
    }

    val surfaceColor = when {
        isSelectedOperator -> colors.accent
        styleType == ButtonStyleType.EQUALS -> colors.equalsColor
        isPressed -> colors.surfacePressed
        else -> colors.surface
    }

    val buttonShape = RoundedCornerShape(22.dp)

    Box(
        modifier = modifier
            .testTag(testTag)
            .pointerInput(enabledHaptics) {
                detectTapGestures(
                    onPress = {
                        if (enabledHaptics) {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        }
                    },
                    onTap = {
                        onClick()
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        NeumorphicBox(
            modifier = Modifier.fillMaxWidth().height(68.dp),
            isPressed = isPressed || isSelectedOperator,
            shape = buttonShape,
            surfaceColor = surfaceColor,
            elevation = 5.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = text,
                    color = textColor,
                    fontSize = if (text.length > 2) 20.sp else 26.sp,
                    fontFamily = OutfitFontFamily,
                    fontWeight = if (styleType == ButtonStyleType.EQUALS || isSelectedOperator) {
                        FontWeight.Bold
                    } else {
                        FontWeight.Medium
                    },
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun NeumorphicDisplay(
    expressionPreview: String,
    displayValue: String,
    modifier: Modifier = Modifier,
    onCopy: () -> Unit = {}
) {
    val colors = LocalNeumorphicColors.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showCopiedBadge by remember { mutableStateOf(false) }

    NeumorphicRecessedBox(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Calculator Result", displayValue)
                clipboard.setPrimaryClip(clip)
                onCopy()
                coroutineScope.launch {
                    showCopiedBadge = true
                    delay(1500)
                    showCopiedBadge = false
                }
            }
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnimatedVisibility(
                    visible = showCopiedBadge,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.accent.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Copiado!",
                            color = colors.accent,
                            fontSize = 12.sp,
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                if (!showCopiedBadge) {
                    Spacer(modifier = Modifier.width(4.dp))
                }

                // Previous expression preview
                Text(
                    text = expressionPreview.ifBlank { " " },
                    color = colors.textSecondary,
                    fontSize = 17.sp,
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main Display value
            val fontSize = when {
                displayValue.length > 13 -> 30.sp
                displayValue.length > 9 -> 38.sp
                displayValue.length > 6 -> 46.sp
                else -> 52.sp
            }

            Text(
                text = displayValue,
                color = colors.textPrimary,
                fontSize = fontSize,
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.End,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("calculator_display")
            )
        }
    }
}

@Composable
fun NeumorphicIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isActive: Boolean = false,
    size: Dp = 46.dp,
    testTag: String = "icon_btn"
) {
    val colors = LocalNeumorphicColors.current
    val view = LocalView.current
    val tint = if (isActive) colors.accent else colors.textSecondary

    Box(
        modifier = modifier
            .size(size)
            .testTag(testTag)
            .clickable {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        NeumorphicBox(
            modifier = Modifier.size(size),
            isPressed = isActive,
            shape = CircleShape,
            elevation = 3.dp
        ) {
            Box(
                modifier = Modifier.size(size),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    tint = tint,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
