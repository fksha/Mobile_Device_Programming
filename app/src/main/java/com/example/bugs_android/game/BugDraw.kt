package com.example.bugs_android.game

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.example.bugs_android.R
import com.example.bugs_android.model.Bug

private const val LOGICAL_WIDTH = 1000f
private const val LOGICAL_HEIGHT = 1000f

fun DrawScope.drawBugs(bugs: List<Bug>, normalImage: ImageBitmap, fastImage: ImageBitmap, rareImage: ImageBitmap) {
    for (bug in bugs) {
        val image = when (bug.imageResource) {
            R.drawable.bug_normal -> normalImage
            R.drawable.bug_fast -> fastImage
            R.drawable.bug_rare -> rareImage
            else -> continue
        }
        val scaleX = size.width / LOGICAL_WIDTH
        val scaleY = size.height / LOGICAL_HEIGHT

        val screenX = (bug.x * scaleX).toInt()
        val screenY = (bug.y * scaleY).toInt()

        val screenSize =
            (bug.size * minOf(scaleX, scaleY)).toInt()

        rotate(
            degrees = bug.angle,
            pivot = Offset(
                x = screenX + screenSize / 2f,
                y = screenY + screenSize / 2f
            )
        ) {
            drawImage(
                image = image,
                dstOffset = IntOffset(screenX, screenY),
                dstSize = IntSize(screenSize, screenSize)
            )
        }
    }
}
