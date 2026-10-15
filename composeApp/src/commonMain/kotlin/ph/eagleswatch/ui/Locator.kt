package ph.eagleswatch.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import ph.eagleswatch.content.Coordinates

/*
 * A schematic silhouette of the Philippines, in latitude/longitude pairs, simplified to the point where it
 * still reads as the archipelago at 48dp wide. It is a locator, not a map: it shows where in the country a
 * project sits, and nothing else. Marker placement is a plain equirectangular projection.
 */

private const val LON_MIN = 116.6
private const val LON_MAX = 126.9
private const val LAT_MIN = 4.6
private const val LAT_MAX = 19.6

/** Width to height of the projected bounding box. */
val LOCATOR_ASPECT: Float = ((LON_MAX - LON_MIN) / (LAT_MAX - LAT_MIN)).toFloat()

private val Islands: List<List<Pair<Double, Double>>> = listOf(
    // Luzon
    listOf(
        18.55 to 120.80, 18.62 to 121.30, 18.40 to 122.20, 17.20 to 122.45, 16.30 to 122.10, 15.45 to 121.45,
        14.70 to 121.70, 14.15 to 122.00, 14.10 to 122.95, 13.85 to 123.85, 13.35 to 123.75, 13.15 to 123.75,
        12.95 to 124.00, 12.58 to 124.08, 12.65 to 123.75, 13.10 to 123.45, 13.60 to 122.90, 13.30 to 122.60,
        13.90 to 122.35, 13.90 to 121.60, 13.55 to 121.15, 13.78 to 120.62, 14.05 to 120.60, 14.42 to 120.52,
        15.00 to 119.90, 15.85 to 119.80, 16.35 to 119.95, 16.60 to 120.30, 17.60 to 120.35, 18.20 to 120.55,
    ),
    // Mindoro
    listOf(13.52 to 120.90, 13.30 to 121.30, 12.70 to 121.50, 12.25 to 121.20, 12.40 to 120.75, 12.90 to 120.50, 13.40 to 120.60),
    // Palawan
    listOf(11.50 to 119.40, 10.70 to 119.70, 9.50 to 118.90, 8.50 to 117.60, 8.40 to 117.20, 9.20 to 117.90, 10.30 to 118.60, 11.10 to 119.10),
    // Panay
    listOf(12.00 to 121.95, 11.65 to 122.75, 11.45 to 123.10, 10.70 to 122.60, 10.45 to 122.00, 10.90 to 121.90, 11.60 to 121.95),
    // Negros
    listOf(10.95 to 123.35, 10.50 to 123.55, 9.80 to 123.25, 9.05 to 122.95, 9.40 to 122.50, 10.10 to 122.80, 10.70 to 123.00),
    // Cebu
    listOf(11.25 to 124.00, 10.60 to 124.00, 9.95 to 123.60, 9.45 to 123.30, 9.60 to 123.25, 10.30 to 123.65, 11.00 to 123.90),
    // Bohol
    listOf(10.15 to 123.85, 10.10 to 124.60, 9.60 to 124.50, 9.60 to 123.85),
    // Samar
    listOf(12.60 to 124.35, 12.60 to 125.10, 12.00 to 125.60, 11.00 to 125.65, 11.05 to 125.00, 11.50 to 124.90, 12.00 to 124.35),
    // Leyte
    listOf(11.55 to 124.35, 11.50 to 125.00, 10.90 to 125.10, 10.00 to 125.05, 10.00 to 124.75, 10.60 to 124.60, 11.20 to 124.40),
    // Masbate
    listOf(12.60 to 123.30, 12.35 to 124.00, 11.95 to 123.65, 12.00 to 123.15, 12.45 to 123.10),
    // Catanduanes
    listOf(14.10 to 124.20, 13.95 to 124.40, 13.55 to 124.30, 13.75 to 124.05),
    // Mindanao
    listOf(
        9.80 to 125.50, 9.30 to 125.95, 8.50 to 126.30, 7.60 to 126.60, 6.60 to 126.40, 6.30 to 126.20,
        6.95 to 125.70, 7.30 to 125.65, 6.50 to 125.50, 5.60 to 125.30, 6.00 to 124.60, 6.45 to 124.00,
        7.40 to 124.05, 7.55 to 123.60, 7.25 to 122.85, 6.90 to 122.05, 7.85 to 122.10, 8.20 to 122.90,
        8.55 to 123.50, 8.20 to 124.20, 8.50 to 124.65, 8.80 to 125.10, 9.40 to 125.40,
    ),
    // Basilan
    listOf(6.75 to 121.85, 6.70 to 122.30, 6.45 to 122.25, 6.45 to 121.90),
)

/**
 * Where a project sits in the country. [point] is drawn as a ring on the silhouette; null draws the silhouette
 * alone, for records with no coordinates on file.
 */
@Composable
fun Locator(
    point: Coordinates?,
    modifier: Modifier = Modifier,
    land: Color = EagleColors.SurfaceStrong,
    outline: Color = EagleColors.LineStrong,
    marker: Color = EagleColors.Blue,
    description: String? = null,
) {
    val label = description ?: if (point != null) "Location marked on a map of the Philippines" else "Map of the Philippines, no location marked"
    Canvas(modifier.aspectRatio(LOCATOR_ASPECT).semantics { contentDescription = label }) {
        drawIslands(land, outline)
        if (point != null) drawMarker(point, marker)
    }
}

private fun DrawScope.project(lat: Double, lon: Double): Offset = Offset(
    x = ((lon - LON_MIN) / (LON_MAX - LON_MIN) * size.width).toFloat(),
    y = ((LAT_MAX - lat) / (LAT_MAX - LAT_MIN) * size.height).toFloat(),
)

private fun DrawScope.drawIslands(land: Color, outline: Color) {
    val hairline = Stroke(width = (size.width / 160f).coerceAtLeast(1f))
    Islands.forEach { island ->
        val path = Path()
        island.forEachIndexed { i, (lat, lon) ->
            val p = project(lat, lon)
            if (i == 0) path.moveTo(p.x, p.y) else path.lineTo(p.x, p.y)
        }
        path.close()
        drawPath(path, land)
        drawPath(path, outline, style = hairline)
    }
}

private fun DrawScope.drawMarker(point: Coordinates, marker: Color) {
    val lat = point.lat.coerceIn(LAT_MIN, LAT_MAX)
    val lon = point.lon.coerceIn(LON_MIN, LON_MAX)
    val centre = project(lat, lon)
    val r = (size.width / 14f).coerceAtLeast(4.dp.toPx())
    drawCircle(Color.White, r + 2.dp.toPx() * 0.5f, centre)
    drawCircle(marker, r, centre, style = Stroke(width = (r * 0.5f).coerceAtLeast(1.5.dp.toPx())))
    drawCircle(marker, r * 0.28f, centre)
}
