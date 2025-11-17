package invoice_service.extensions

import java.math.BigDecimal
import java.math.RoundingMode

fun BigDecimal.roundTo(scale: Int): BigDecimal =
    this.setScale(scale, RoundingMode.HALF_UP)

fun BigDecimal.roundAmount(): BigDecimal = roundTo(4)

fun BigDecimal.roundRate(): BigDecimal = roundTo(8)

