package rendering_service.components

import org.jfree.chart.ChartFactory
import org.jfree.chart.JFreeChart
import org.jfree.chart.plot.PiePlot
import org.jfree.data.general.DefaultPieDataset
import java.awt.Color
import java.awt.Font
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import java.util.Base64

object ReportStatusPieChartGenerator {
    fun generateStatusPieChartBase64(statusCounts: Map<String, Int>): String {
        val statusTranslations = mapOf(
            "DRAFT" to "Koncept",
            "SENT" to "Odesláno",
            "PENDING" to "Čeká na platbu",
            "PAID" to "Zaplaceno",
            "CANCELLED" to "Stornováno",
            "OVERDUE" to "Po splatnosti",
            "PAID_OVERDUE" to "Zaplaceno po splatnosti"
        )
        val pieDataset = DefaultPieDataset<String>()
        for ((status, count) in statusCounts) {
            val translated = statusTranslations[status] ?: status
            pieDataset.setValue(translated, count)
        }
        val chart: JFreeChart = ChartFactory.createPieChart(
            "Podíl stavů faktur",
            pieDataset,
            true,
            false,
            false
        )
        chart.backgroundPaint = Color.WHITE
        val plot = chart.plot as PiePlot<String>
        plot.backgroundPaint = Color.WHITE
        plot.setOutlineVisible(false)
        plot.setLabelFont(Font("DejaVu Sans", Font.PLAIN, 12))
        plot.setLabelGenerator(null)
        val statuses = statusCounts.keys.map { statusTranslations[it] ?: it }
        for (i in statuses.indices) {
            val hue = i.toFloat() / statuses.size
            val rgb = Color.getHSBColor(hue, 0.6f, 0.85f)
            plot.setSectionPaint(statuses[i], rgb)
        }
        plot.setInteriorGap(0.05)
        val image: BufferedImage = chart.createBufferedImage(300, 300)
        val baos = ByteArrayOutputStream()
        ImageIO.write(image, "png", baos)
        return Base64.getEncoder().encodeToString(baos.toByteArray())
    }
}
