package br.com.diegomb.covid19infosdm

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewParent
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import br.com.diegomb.covid19infosdm.viewmodel.Covid19ViewModel
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: Covid19ViewModel
    private lateinit var countryAdapter: ArrayAdapter<String>

    // Classe para os serviços que serão acessados
    private enum class Information(val type: String){
        DAY_ONE("Day one"),
        BY_COUNTRY("By country")
    }

    // Classe para o status que será buscado no serviço
    private enum class Status(val type: String){
        CONFIRMED("Confirmed"),
        RECOVERED("Recovered"),
        DEATHS("Deaths")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = Covid19ViewModel(this)

        countryAdapterInit()

        informationAdapterInit()

        statusAdapterInit()
    }

    fun onRetrieveClick(view: View) {

    }

    private fun countryAdapterInit() {
        //Preenchido por Web Service
        countryAdapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1)
        countrySp.adapter = countryAdapter
        viewModel.fetchCountries().observe(
            this,
            Observer { countryList ->
                countryList.forEach{ countryListItem ->
                    if(countryListItem.country.isNotEmpty()) { countryAdapter.add(countryListItem.country)}
                }
            }
        )
    }

    private fun informationAdapterInit() {
        val informationList = arrayListOf<String>()
        Information.values().forEach { informationList.add(it.type) }

        infoSp.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, informationList)
        infoSp.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) { }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when(position) {
                    Information.DAY_ONE.ordinal -> {
                        viewModeTv.visibility = View.GONE
                        viewModeRg.visibility = View.GONE
                    }
                    Information.BY_COUNTRY.ordinal -> {
                        viewModeTv.visibility = View.VISIBLE
                        viewModeRg.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun statusAdapterInit() {
        val statusList = arrayListOf<String>()
        Status.values().forEach { statusList.add(it.type) }

        statusSp.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, statusList)
    }
}
