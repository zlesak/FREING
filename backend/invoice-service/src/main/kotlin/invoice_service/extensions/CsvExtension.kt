package invoice_service.extensions

fun String.csvEscape(): String =
    if (this.any { it == ',' || it == '"' || it == '\n' || it == '\r' })
        '"' + replace("\"", "\"\"") + '"'
    else this
