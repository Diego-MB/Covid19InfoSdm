package br.com.diegomb.covid19infosdm.model

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import br.com.diegomb.covid19infosdm.R
import br.com.diegomb.covid19infosdm.model.Covid19Api.BASE_URL
import br.com.diegomb.covid19infosdm.model.Covid19Api.COUNTRIES_ENDPOINT
import br.com.diegomb.covid19infosdm.model.dataclass.CaseList
import br.com.diegomb.covid19infosdm.model.dataclass.CountryList
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class Covid19Service(val context: Context) {
    private val requestQueue = Volley.newRequestQueue(context)
    private val gson = Gson()

    // Acessando a Web Service usando Volley
    fun callGetCountries(): MutableLiveData<CountryList> {
        val url = "${BASE_URL}${COUNTRIES_ENDPOINT}"

        val countriesListLd = MutableLiveData<CountryList>()

        val request = JsonArrayRequest(
            Request.Method.GET,
            url,
            null,
            { countriesList ->
                countriesListLd.value = gson.fromJson(countriesList.toString(), CountryList::class.java)
            },
            { error ->  Log.e(context.getString(R.string.app_name), "${error.message}")}
        )

        requestQueue.add(request)

        return countriesListLd
    }

    //Cria um implementação da interface usando um objeto retrofit
    private val retrofitServices = with(Retrofit.Builder()){
        baseUrl(BASE_URL)
        addConverterFactory(GsonConverterFactory.create())
        build()
    }.create(Covid19Api.RetrofitServices::class.java)

    /*Acesso a Web Service usando Retrofit. Como os serviços retornam o mesmo tipo de resposta
    * foram aglutionados numa mesm função*/
    fun callService(countryName: String, status: String, service: String): MutableLiveData<CaseList> {
        val caseList: MutableLiveData<CaseList> = MutableLiveData()

        //Callback usando pelos serviços que retornam o mesmo tipo de JSON
        val callback = object: Callback<CaseList> {
            override fun onResponse(call: Call<CaseList>, response: Response<CaseList>) {
                if (response.isSuccessful){
                    caseList.value = response.body()
                }
            }

            override fun onFailure(call: Call<CaseList>, t: Throwable) {
                Log.e(context.getString(R.string.app_name), "")
            }
        }

        return caseList
    }
}