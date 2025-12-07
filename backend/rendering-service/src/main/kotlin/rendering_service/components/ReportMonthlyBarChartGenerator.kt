package rendering_service.components

import org.jfree.chart.ChartFactory
import org.jfree.chart.JFreeChart
import org.jfree.data.category.DefaultCategoryDataset
import java.awt.Color
import java.awt.Font
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.util.*
import javax.imageio.ImageIO

object ReportMonthlyBarChartGenerator {
    fun generateMonthlyBarChartBase64(monthCounts: Map<String, Int>): String {
        val dataset = DefaultCategoryDataset()
        for ((month, count) in monthCounts) {
            dataset.addValue(count, "Faktury", month)
        }
        val chart: JFreeChart = ChartFactory.createBarChart(
            "Počet vystavených faktur v měsících",
            "Měsíc",
            "Počet faktur",
            dataset
        )
        chart.backgroundPaint = Color.WHITE
        val plot = chart.categoryPlot
        plot.backgroundPaint = Color.WHITE
        plot.setOutlineVisible(false)
        plot.rangeGridlinePaint = Color(220, 220, 220)
        val renderer = plot.renderer
        renderer.setSeriesPaint(0, Color(74, 144, 226))
        plot.domainAxis.labelFont = Font("DejaVu Sans", Font.BOLD, 14)
        plot.domainAxis.tickLabelFont = Font("DejaVu Sans", Font.PLAIN, 12)
        plot.rangeAxis.labelFont = Font("DejaVu Sans", Font.BOLD, 14)
        plot.rangeAxis.tickLabelFont = Font("DejaVu Sans", Font.PLAIN, 12)
        val image: BufferedImage = chart.createBufferedImage(300, 300)
        val baos = ByteArrayOutputStream()
        ImageIO.write(image, "png", baos)
        return Base64.getEncoder().encodeToString(baos.toByteArray())
    }
}

