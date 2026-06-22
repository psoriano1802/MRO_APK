package com.example.lacteos_flores.models

import com.google.gson.annotations.SerializedName

data class ResponseCatTmc(
    @SerializedName("ResponseCat_TMC") val responseCatTmc: List<Map<String, String>>
)
