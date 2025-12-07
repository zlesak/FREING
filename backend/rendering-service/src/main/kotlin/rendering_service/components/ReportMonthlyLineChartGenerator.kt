package rendering_service.components

import org.jfree.chart.ChartFactory
import org.jfree.chart.JFreeChart
import org.jfree.chart.plot.PlotOrientation
import org.jfree.data.category.DefaultCategoryDataset
import java.awt.Color
import java.awt.Font
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import java.util.Base64

object ReportMonthlyLineChartGenerator {
    fun generateMonthlyLineChartBase64(monthIssued: Map<String, Int>, monthPaid: Map<String, Int>): String {
        val dataset = DefaultCategoryDataset()
        for ((month, count) in monthIssued) {
            dataset.addValue(count, "Vydané faktury", month)
        }
        for ((month, count) in monthPaid) {
            dataset.addValue(count, "Zaplacené faktury", month)
        }
        val chart: JFreeChart = ChartFactory.createLineChart(
            "Vydané vs. zaplacené faktury dle měsíců",
            "Měsíc",
            "Počet faktur",
            dataset,
            PlotOrientation.VERTICAL,
            true,
            false,
            false
        )
        chart.backgroundPaint = Color.WHITE
        val plot = chart.categoryPlot
        plot.backgroundPaint = Color.WHITE
        plot.setOutlineVisible(false)
        plot.rangeGridlinePaint = Color(220, 220, 220)
        val renderer = plot.renderer
        renderer.setSeriesPaint(0, Color(74, 144, 226))
        renderer.setSeriesPaint(1, Color(46, 204, 113))
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

