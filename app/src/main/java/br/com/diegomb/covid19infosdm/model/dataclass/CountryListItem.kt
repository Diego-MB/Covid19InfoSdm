package br.com.diegomb.covid19infosdm.model.dataclass

import com.google.gson.annotations.SerializedName

data class CountryListItem(
    @SerializedName("Country")
    val country: String,
    @SerializedName("Provinces")
    val provinces: List<String>,
    @SerializedName("Slug")
    val slug: String
)