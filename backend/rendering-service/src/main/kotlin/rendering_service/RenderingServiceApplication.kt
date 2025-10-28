package rendering_service

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(
    scanBasePackages = [
        "rendering_service",
        "com.uhk.fim.prototype.common"
    ]
)
class RenderingServiceApplication

fun main(args: Array<String>) {
    runApplication<RenderingServiceApplication>(*args)
}
