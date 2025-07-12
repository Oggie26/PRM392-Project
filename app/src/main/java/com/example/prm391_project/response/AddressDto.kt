package com.example.prm391_project.response

import com.google.gson.annotations.SerializedName

data class AddressDto(
    @SerializedName("id")          val id: Int?,
    @SerializedName("name")        val name: String?,
    @SerializedName("phone")       val phone: String?,
    @SerializedName("city")        val city: String?,
    @SerializedName("district")    val district: String?,
    @SerializedName("ward")        val ward: String?,
    @SerializedName("street")      val street: String?,
    @SerializedName("addressLine") val addressLine: String?,
    @SerializedName("isDefault")   val isDefault: Boolean?
)