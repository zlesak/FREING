package rendering_service.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository
import rendering_service.model.Pdf

@Repository
interface PdfRepository : JpaRepository<Pdf, Long>, JpaSpecificationExecutor<Pdf> {}

