package rendering_service.components

import org.jfree.chart.ChartFactory
import org.jfree.chart.JFreeChart
import org.jfree.chart.plot.PiePlot
import org.jfree.data.general.DefaultPieDataset
import java.awt.Color
import java.awt.Font
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.util.*
import javax.imageio.ImageIO

object ReportPieChartGenerator {
    fun generatePieChartBase64(customerNames: List<String>, invoiceCounts: List<Double>): String {
        val pieDataset = DefaultPieDataset<String>()
        for (i in customerNames.indices) {
            pieDataset.setValue(customerNames[i], invoiceCounts[i])
        }
        val chart: JFreeChart = ChartFactory.createPieChart(
            "Podíl počtu faktur podle zákazníka",
            pieDataset,
            true,
            false,
            false
        )
        chart.backgroundPaint = Color.WHITE
        val plot = chart.plot as PiePlot<String>
        plot.backgroundPaint = Color.WHITE
        plot.setOutlineVisible(false)
        plot.labelFont = Font("DejaVu Sans", Font.PLAIN, 12)
        plot.labelGenerator = null

        for (i in customerNames.indices) {
            val hue = i.toFloat() / customerNames.size
            val rgb = Color.getHSBColor(hue, 0.6f, 0.85f)
            plot.setSectionPaint(customerNames[i], rgb)
        }
        plot.labelBackgroundPaint = Color.WHITE
        plot.labelOutlinePaint = Color.WHITE
        plot.labelShadowPaint = Color.WHITE
        plot.shadowPaint = Color.WHITE
        plot.simpleLabels = true

        plot.interiorGap = 0.05
        val image: BufferedImage = chart.createBufferedImage(300, 300)
        val baos = ByteArrayOutputStream()
        ImageIO.write(image, "png", baos)
        return Base64.getEncoder().encodeToString(baos.toByteArray())
    }
}
