package com.example.prm391_project.ui.checkout

import TokenManager
import androidx.lifecycle.ViewModel
import com.example.prm391_project.api.PaymentService
import com.example.prm391_project.response.AddressDto
import kotlinx.coroutines.flow.MutableStateFlow

data class AddressForm(
    val id: Int,
    val name: String,
    val phone: String,
    val city: String,
    val district: String,
    val ward: String,
    val street: String,
    val addressLine: String,
    val isDefault: Boolean
)

fun AddressDto.toForm() = AddressForm(
    id = id ?: 0,
    name = name.orEmpty(),
    phone = phone.orEmpty(),
    city = city.orEmpty(),
    district = district.orEmpty(),
    ward = ward.orEmpty(),
    street = street.orEmpty(),
    addressLine = addressLine.orEmpty(),
    isDefault = isDefault == true
)

data class Voucher(
    val code: String,
    val title: String,
    val discountPercent: Int,
    val description: String
)

