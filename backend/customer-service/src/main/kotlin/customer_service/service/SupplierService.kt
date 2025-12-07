package customer_service.service

import com.uhk.fim.prototype.common.exceptions.NotFoundException
import com.uhk.fim.prototype.common.exceptions.WrongDataException
import customer_service.models.Supplier
import customer_service.repo.SupplierRepo
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SupplierService(
    private val supplierRepo: SupplierRepo
) {

    @Transactional
    fun create(supplier: Supplier): Supplier {
        when {
            getSupplierByEmailOrPhoneNumber(supplier.email, supplier.phoneNumber) != null ->
                throw WrongDataException("Supplier already exists!")

            !(supplier.tradeName.isNotBlank()) ->
                throw WrongDataException("Supplier trade name must be filled!")

            supplier.phoneNumber.isBlank() ->
                throw WrongDataException("Supplier phone must be fill!")

            supplier.email.isBlank() ->
                throw WrongDataException("Supplier email must be fill!")
        }

        val savedSupplier = supplierRepo.save(supplier)

        return savedSupplier
    }

    fun update(supplier: Supplier): Supplier =
        getSupplierById(supplier.id!!).apply { updateFrom(supplier) }
            .let { supplierRepo.save(it) }

    fun deleteSupplier(id: Long) =
        supplierRepo.findByIdOrNull(id)?.let {
            supplierRepo.delete(it)
        } ?: throw NotFoundException("Supplier not found!")

    fun getSupplierById(id: Long): Supplier =
        supplierRepo.findByIdOrNull(id)
            ?: throw NotFoundException("Supplier not found!")

    private fun getSupplierByEmailOrPhoneNumber(email: String, phoneNumber: String): Supplier? =
        supplierRepo.findByEmailOrPhoneNumber(email, phoneNumber)

    fun getAllSuppliers(
        pageable: Pageable,
        supplierId: Long? = null,
        supplierIds: List<Long>? = null,
        tradeName: String? = null,
        email: String? = null,
        phoneNumber: String? = null,
        city: String? = null,
        ico: String? = null,
        dic: String? = null,
        country: String? = null,
        currency: String? = null
    ): Page<Supplier> {
        val spec: Specification<Supplier>? = when {
            !supplierIds.isNullOrEmpty() -> Specification { root, _, _ -> root.get<Long>("id").`in`(supplierIds) }
            supplierId != null -> Specification { root, _, cb -> cb.equal(root.get<Long>("id"), supplierId) }
            else -> {
                val filters = mutableListOf<Specification<Supplier>>()
                tradeName?.let { filters.add(Specification { root, _, cb -> cb.like(cb.lower(root.get("tradeName")), "%" + it.lowercase() + "%") }) }
                email?.let { filters.add(Specification { root, _, cb -> cb.like(cb.lower(root.get("email")), "%" + it.lowercase() + "%") }) }
                phoneNumber?.let { filters.add(Specification { root, _, cb -> cb.like(cb.lower(root.get("phoneNumber")), "%" + it.lowercase() + "%") }) }
                city?.let { filters.add(Specification { root, _, cb -> cb.like(cb.lower(root.get("city")), "%" + it.lowercase() + "%") }) }
                ico?.let { filters.add(Specification { root, _, cb -> cb.like(cb.lower(root.get("ico")), "%" + it.lowercase() + "%") }) }
                dic?.let { filters.add(Specification { root, _, cb -> cb.like(cb.lower(root.get("dic")), "%" + it.lowercase() + "%") }) }
                country?.let { filters.add(Specification { root, _, cb -> cb.like(cb.lower(root.get("country")), "%" + it.lowercase() + "%") }) }
                currency?.let { filters.add(Specification { root, _, cb -> cb.like(cb.lower(root.get("currency")), "%" + it.lowercase() + "%") }) }
                filters.reduceOrNull { acc, s -> acc.and(s) }
            }
        }
        return if (spec != null) supplierRepo.findAll(spec, pageable) else supplierRepo.findAll(pageable)
    }
}
