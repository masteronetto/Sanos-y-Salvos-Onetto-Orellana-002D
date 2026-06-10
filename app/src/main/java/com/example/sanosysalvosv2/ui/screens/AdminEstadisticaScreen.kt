package com.example.sanosysalvosv2.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.sanosysalvosv2.ui.theme.TextSecondary

private val DarkGreen = Color(0xFF0E5B3D)
private val GrayText = Color(0xFF7A7A7A)
private val BorderColor = Color(0xFFD7E5E3)
private val KPIBackground = Color.White
private val Teal = Color(0xFF0F8A8A)
private val TealDark = Color(0xFF0F5B5B)
private val TealMid = Color(0xFF167E7E)
private val TealLight = Color(0xFF6ED8D1)
private val DarkGray = Color(0xFF424242)
private val BarDarkest = Color(0xFF0B6F70)
private val LineEncontrados = Teal
private val LinePerdidos = DarkGray

@Composable
fun AdminEstadisticaScreen(
    onLogout: () -> Unit,
) {
    val comunaValues = remember {
        listOf(
            "Maipú" to 120,
            "Providencia" to 98,
            "La Florida" to 85,
            "Ñuñoa" to 65,
        )
    }

    val months = remember { listOf("Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio") }
    val foundSeries = remember { listOf(32f, 40f, 48f, 58f, 66f, 74f) }
    val lostSeries = remember { listOf(72f, 66f, 58f, 44f, 34f, 24f) }

    androidx.compose.material3.Scaffold(
        topBar = { AdminTopBar(onLogout = onLogout) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Entidades colaboradoras",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = DarkGreen,
            )
            Text(
                text = "Análisis y estadísticas de la plataforma",
                style = MaterialTheme.typography.bodyLarge,
                color = GrayText,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                KpiCard(
                    title = "Tasa de recuperación",
                    value = "62%",
                    modifier = Modifier.weight(1f),
                )
                KpiCard(
                    title = "Tiempo promedio de reencuentro",
                    value = "3,4 días",
                    modifier = Modifier.weight(1f),
                )
            }

            SectionCard(
                title = "Reportes por comuna",
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    val maxValue = comunaValues.maxOf { it.second }.coerceAtLeast(1)
                    comunaValues.forEach { (label, value) ->
                        ComunaBarRow(
                            label = label,
                            value = value,
                            maxValue = maxValue,
                            barColor = when (label) {
                                "Maipú" -> BarDarkest
                                "Providencia" -> TealDark
                                "La Florida" -> TealMid
                                else -> TealLight
                            },
                        )
                    }
                }
            }

            SectionCard(title = "Reportes por tipo (Últimos 6 meses)") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        LegendItem(color = LineEncontrados, label = "Encontrados")
                        LegendItem(color = LinePerdidos, label = "Perdidos")
                    }

                    LineChart(
                        months = months,
                        foundSeries = foundSeries,
                        lostSeries = lostSeries,
                    )
                }
            }
        }
    }
}

@Composable
private fun KpiCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = KPIBackground),
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(text = title, color = GrayText, fontWeight = FontWeight.Medium)
            Text(text = value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = DarkGreen)
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(text = title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.Black)
            content()
        }
    }
}

@Composable
private fun ComunaBarRow(
    label: String,
    value: Int,
    maxValue: Int,
    barColor: Color,
) {
    val fraction = (value.toFloat() / maxValue.toFloat()).coerceIn(0f, 1f)

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(text = label, color = GrayText, fontWeight = FontWeight.Medium)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .background(Color(0xFFF3F7F6), RoundedCornerShape(10.dp))
                .border(1.dp, BorderColor, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.CenterStart,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .height(28.dp)
                    .background(barColor, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.CenterStart,
            ) {
                Text(
                    text = "$value",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 12.dp),
                )
            }
        }
    }
}

@Composable
private fun LegendItem(
    color: Color,
    label: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, RoundedCornerShape(99.dp)),
        )
        Text(text = label, color = GrayText, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun LineChart(
    months: List<String>,
    foundSeries: List<Float>,
    lostSeries: List<Float>,
) {
    val minValue = 0f
    val maxValue = 80f

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .fillMaxWidth(),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val leftPad = 28f
                        val rightPad = 8f
                        val topPad = 18f
                        val bottomPad = 28f
                        val chartWidth = size.width - leftPad - rightPad
                        val chartHeight = size.height - topPad - bottomPad

                        val ySteps = listOf(0f, 20f, 40f, 60f, 80f)
                        ySteps.forEach { value ->
                            val y = topPad + chartHeight - ((value - minValue) / (maxValue - minValue)) * chartHeight
                            drawLine(
                                color = Color(0xFFE8ECEB),
                                start = androidx.compose.ui.geometry.Offset(leftPad, y),
                                end = androidx.compose.ui.geometry.Offset(size.width - rightPad, y),
                                strokeWidth = 2f,
                            )
                        }

                        val xStep = if (months.size > 1) chartWidth / (months.size - 1) else chartWidth

                        fun points(series: List<Float>): List<Offset> {
                            return series.mapIndexed { index, value ->
                                val x = leftPad + xStep * index
                                val normalized = ((value - minValue) / (maxValue - minValue)).coerceIn(0f, 1f)
                                val y = topPad + chartHeight - normalized * chartHeight
                                Offset(x, y)
                            }
                        }

                        val foundPoints = points(foundSeries)
                        val lostPoints = points(lostSeries)

                        fun drawSeries(points: List<Offset>, color: Color) {
                            if (points.isEmpty()) return

                            val path = Path().apply {
                                moveTo(points.first().x, points.first().y)
                                for (i in 1 until points.size) {
                                    lineTo(points[i].x, points[i].y)
                                }
                            }
                            drawPath(
                                path = path,
                                color = color,
                                style = Stroke(width = 5f, cap = StrokeCap.Round),
                            )

                            points.forEach { point ->
                                drawCircle(color = color, radius = 7f, center = point)
                                drawCircle(color = Color.White, radius = 3f, center = point)
                            }
                        }

                        drawSeries(lostPoints, LinePerdidos)
                        drawSeries(foundPoints, LineEncontrados)

                        ySteps.forEach { value ->
                            val y = topPad + chartHeight - ((value - minValue) / (maxValue - minValue)) * chartHeight
                            drawCircle(color = Color(0xFFBFC7C5), radius = 2f, center = androidx.compose.ui.geometry.Offset(leftPad, y))
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    listOf("0", "20", "40", "60", "80").forEach { label ->
                        Text(text = label, color = GrayText, fontWeight = FontWeight.Medium)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    months.forEach { month ->
                        Text(
                            text = month,
                            color = GrayText,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminTopBar(onLogout: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(modifier = Modifier.height(1.dp))
        androidx.compose.material3.TextButton(onClick = onLogout) {
            Text(text = "Salir")
        }
    }
}