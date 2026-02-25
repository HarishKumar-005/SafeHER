package com.phantomcrowd.utils

import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polygon
import android.graphics.Color
import com.phantomcrowd.data.AnchorData
import com.phantomcrowd.data.RiskScoring
import android.util.Log
import org.osmdroid.util.GeoPoint
import kotlin.math.pow

/**
 * Heatmap renderer for the Women Safety Risk Map.
 *
 * Colors are based on aggregated risk score per grid cell,
 * incorporating severity, upvotes, and time-decay.
 */
class HeatmapRenderer(private val mapView: MapView) {
    
    companion object {
        private const val TAG = "HeatmapRenderer"
        private const val GRID_SIZE_DEGREES = 0.01  // ~1 km grid cell
        const val HIGH_RISK_THRESHOLD = 5  // density threshold for "high risk" cells
    }
    
    private val heatmapCells = mutableListOf<Polygon>()
    
    /**
     * Render heatmap based on risk-weighted anchor density.
     * Uses time-decay and severity weighting.
     */
    fun renderHeatmap(anchors: List<AnchorData>) {
        // Clear previous cells
        heatmapCells.forEach { mapView.overlays.remove(it) }
        heatmapCells.clear()
        
        if (anchors.isEmpty()) {
            mapView.invalidate()
            return
        }
        
        // Group anchors into grid cells
        val grid = mutableMapOf<String, MutableList<AnchorData>>()
        
        anchors.forEach { anchor ->
            val cellKey = getCellKey(anchor.latitude, anchor.longitude)
            grid.getOrPut(cellKey) { mutableListOf() }.add(anchor)
        }
        
        // Create polygon for each grid cell
        grid.forEach { (cellKey, cellAnchors) ->
            val (baseLat, baseLon) = parseCellKey(cellKey)
            
            // Compute weighted risk for the cell: sum of individual risk scores
            val cellRisk = computeCellRisk(cellAnchors)
            val (color, alpha) = getColorForRisk(cellRisk, cellAnchors.size)
            
            // Create polygon bounds
            val north = baseLat + GRID_SIZE_DEGREES
            val south = baseLat
            val east = baseLon + GRID_SIZE_DEGREES
            val west = baseLon
            
            val polygon = Polygon(mapView).apply {
                setPoints(listOf(
                    GeoPoint(north, west),
                    GeoPoint(north, east),
                    GeoPoint(south, east),
                    GeoPoint(south, west)
                ))
                fillPaint.color = color
                fillPaint.alpha = alpha
                outlinePaint.color = darkenColor(color)
                outlinePaint.strokeWidth = 2f
            }
            
            mapView.overlays.add(polygon)
            heatmapCells.add(polygon)
        }
        
        Log.d(TAG, "Rendered risk heatmap with ${heatmapCells.size} cells")
        mapView.invalidate()
    }
    
    /**
     * Compute aggregate risk for a grid cell.
     * Each anchor contributes a time-decayed risk score.
     */
    private fun computeCellRisk(cellAnchors: List<AnchorData>): Double {
        val now = System.currentTimeMillis()
        return cellAnchors.sumOf { anchor ->
            val ageDays = (now - anchor.timestamp).toDouble() / (1000 * 60 * 60 * 24)
            RiskScoring.computeRiskScore(
                severity = anchor.severity,
                upvotes = anchor.upvotes,
                ageDays = ageDays,
                distanceMeters = 0.0  // Cell-level, no user distance needed
            )
        }
    }
    
    /**
     * Get grid cell key for lat/lon
     */
    private fun getCellKey(lat: Double, lon: Double): String {
        val cellLat = kotlin.math.floor(lat / GRID_SIZE_DEGREES) * GRID_SIZE_DEGREES
        val cellLon = kotlin.math.floor(lon / GRID_SIZE_DEGREES) * GRID_SIZE_DEGREES
        return "$cellLat,$cellLon"
    }
    
    /**
     * Parse grid cell key back to coordinates
     */
    private fun parseCellKey(key: String): Pair<Double, Double> {
        val (lat, lon) = key.split(",")
        return Pair(lat.toDouble(), lon.toDouble())
    }
    
    /**
     * Color based on aggregated risk + density.
     * Uses density as fallback when risk data is sparse.
     */
    private fun getColorForRisk(cellRisk: Double, density: Int): Pair<Int, Int> {
        return when {
            cellRisk >= 2.0 || density >= 5 -> Pair(Color.parseColor("#EF4444"), 90)  // High Risk: Red
            cellRisk >= 0.8 || density >= 2 -> Pair(Color.parseColor("#FBBF24"), 80)  // Medium Risk: Amber
            else -> Pair(Color.parseColor("#22C55E"), 70)                              // Low Risk: Green
        }
    }
    
    /**
     * Darken color for outline
     */
    private fun darkenColor(color: Int): Int {
        val r = Color.red(color) / 2
        val g = Color.green(color) / 2
        val b = Color.blue(color) / 2
        return Color.rgb(r, g, b)
    }
    
    /**
     * Clear all heatmap cells
     */
    fun clearHeatmap() {
        heatmapCells.forEach { mapView.overlays.remove(it) }
        heatmapCells.clear()
        mapView.invalidate()
        Log.d(TAG, "Heatmap cleared")
    }
    
    /**
     * Check if a grid cell is high risk — used by SafeRoute to avoid danger zones.
     */
    fun isHighRiskCell(lat: Double, lon: Double, anchors: List<AnchorData>): Boolean {
        val cellKey = getCellKey(lat, lon)
        val cellAnchors = anchors.filter { getCellKey(it.latitude, it.longitude) == cellKey }
        if (cellAnchors.isEmpty()) return false
        val risk = computeCellRisk(cellAnchors)
        return risk >= 2.0 || cellAnchors.size >= HIGH_RISK_THRESHOLD
    }
}
