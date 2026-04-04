package com.example.myapplication.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val ClujShapes = Shapes(
    small        = RoundedCornerShape(12.dp),   // pills, chips
    medium       = RoundedCornerShape(20.dp),   // buttons
    large        = RoundedCornerShape(24.dp),   // cards (rounded-3xl)
    extraLarge   = RoundedCornerShape(32.dp),   // hero blob, large containers
)
