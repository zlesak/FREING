package com.uhk.fim.prototype.common.exceptions.customer

import com.uhk.fim.prototype.common.exceptions.NotFoundException

class CustomerNotFoundException(target: String) : NotFoundException(target) {
}