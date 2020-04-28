package br.com.diegomb.covid19infosdm.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import br.com.diegomb.covid19infosdm.model.Covid19Api
import br.com.diegomb.covid19infosdm.model.Covid19Service
import br.com.diegomb.covid19infosdm.model.dataclass.ByCountryResponseList
import br.com.diegomb.covid19infosdm.model.dataclass.DayOneResponseList
import java.util.*

class Covid19ViewModel(context: Context): ViewModel() {
    private val model = Covid19Service(context)

    fun fetchCountries() = model.callGetCountries()

    fun fetchDayOne(countryName: String, status: String) = model.callService(countryName,
        status.toLowerCase(Locale.getDefault()),
        DayOneResponseList::class.java
    )

    fun fetchByCountry(countryName: String, status: String) = model.callService(countryName,
        status.toLowerCase(Locale.getDefault()),
        ByCountryResponseList::class.java
    )
}