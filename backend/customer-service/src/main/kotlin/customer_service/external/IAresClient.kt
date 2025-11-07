package customer_service.external

import customer_service.models.ares.Subjekt

interface IAresClient {
    fun getSubjectByIcoARES(ico: String): Subjekt?
}