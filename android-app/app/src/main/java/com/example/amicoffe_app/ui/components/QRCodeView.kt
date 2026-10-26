package com.example.amicoffe_app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.amicoffe_app.ui.theme.AmiGreenDark
import kotlin.math.abs

@Composable
fun QRCodeView(
    qrData: String,
    modifier: Modifier = Modifier,
    size: Dp = 180.dp,
    qrColor: Color = AmiGreenDark,
    backgroundColor: Color = Color.White
) {
    Box(
        modifier = modifier
            .size(size)
            .background(backgroundColor, RoundedCornerShape(20.dp))
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
            .padding(14.dp),
        contentAlignment = Alignment.Center
    ) {
        val gridSize = 21

        Canvas(modifier = Modifier.fillMaxSize()) {
            val cellWidth = size.toPx() / gridSize
            val cellHeight = size.toPx() / gridSize

            // Generador pseudo-aleatorio determinista basado en el hash del string
            val seed = qrData.hashCode()

            // 1. Dibuja los patrones de posicionamiento de las 3 esquinas (Top-Left, Top-Right, Bottom-Left)
            fun drawFinderPattern(startCol: Int, startRow: Int) {
                // Marco exterior de 7x7
                drawRect(
                    color = qrColor,
                    topLeft = Offset(startCol * cellWidth, startRow * cellHeight),
                    size = Size(7 * cellWidth, 7 * cellHeight)
                )
                // Hueco interior blanco de 5x5
                drawRect(
                    color = backgroundColor,
                    topLeft = Offset((startCol + 1) * cellWidth, (startRow + 1) * cellHeight),
                    size = Size(5 * cellWidth, 5 * cellHeight)
                )
                // Centro oscuro de 3x3
                drawRect(
                    color = qrColor,
                    topLeft = Offset((startCol + 2) * cellWidth, (startRow + 2) * cellHeight),
                    size = Size(3 * cellWidth, 3 * cellHeight)
                )
            }

            drawFinderPattern(0, 0)
            drawFinderPattern(gridSize - 7, 0)
            drawFinderPattern(0, gridSize - 7)

            // 2. Dibuja los módulos de datos en la cuadrícula
            for (r in 0 until gridSize) {
                for (c in 0 until gridSize) {
                    // Ignora las zonas reservadas de los 3 patrones de búsqueda y centro
                    val isTopLeft = r in 0..7 && c in 0..7
                    val isTopRight = r in 0..7 && c in (gridSize - 8)..<gridSize
                    val isBottomLeft = r in (gridSize - 8)..<gridSize && c in 0..7
                    val isCenterLogo = r in 8..12 && c in 8..12

                    if (!isTopLeft && !isTopRight && !isBottomLeft && !isCenterLogo) {
                        val cellHash = abs((seed + r * 31 + c * 17) xor (r * c))
                        val isDark = cellHash % 3 != 0

                        if (isDark) {
                            drawRect(
                                color = qrColor,
                                topLeft = Offset(c * cellWidth, r * cellHeight),
                                size = Size(cellWidth * 0.92f, cellHeight * 0.92f)
                            )
                        }
                    }
                }
            }
        }

        // Insignia central de AmiCoffee en el centro del QR
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color.White, RoundedCornerShape(10.dp))
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                .padding(3.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AmiGreenDark, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocalCafe,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
