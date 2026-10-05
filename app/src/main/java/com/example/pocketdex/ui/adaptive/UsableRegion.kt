package com.example.pocketdex.ui.adaptive

data class PixelRect(val left: Int, val top: Int, val right: Int, val bottom: Int) {
    val width: Int get() = (right - left).coerceAtLeast(0)
    val height: Int get() = (bottom - top).coerceAtLeast(0)
    val area: Long get() = width.toLong() * height
}

data class WindowObstruction(val bounds: PixelRect, val vertical: Boolean)

/** Conservative single-pane policy: choose the largest safe rectangle, top/left on ties. */
fun usableRegion(width: Int, height: Int, obstructions: List<WindowObstruction>): PixelRect {
    require(width >= 0 && height >= 0)
    var regions = listOf(PixelRect(0, 0, width, height))
    for (obstruction in obstructions) {
        regions = regions.flatMap { region ->
            val bounds = obstruction.bounds
            if (obstruction.vertical && bounds.bottom > region.top && bounds.top < region.bottom &&
                bounds.right > region.left && bounds.left < region.right
            ) {
                listOf(
                    region.copy(right = bounds.left.coerceIn(region.left, region.right)),
                    region.copy(left = bounds.right.coerceIn(region.left, region.right)),
                )
            } else if (!obstruction.vertical && bounds.right > region.left && bounds.left < region.right &&
                bounds.bottom > region.top && bounds.top < region.bottom
            ) {
                listOf(
                    region.copy(bottom = bounds.top.coerceIn(region.top, region.bottom)),
                    region.copy(top = bounds.bottom.coerceIn(region.top, region.bottom)),
                )
            } else listOf(region)
        }
    }
    return regions.maxBy { it.area }
}
