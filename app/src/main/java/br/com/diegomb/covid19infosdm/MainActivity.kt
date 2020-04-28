package br.com.diegomb.covid19infosdm

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewParent
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import br.com.diegomb.covid19infosdm.model.dataclass.CaseList
import br.com.diegomb.covid19infosdm.viewmodel.Covid19ViewModel
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: Covid19ViewModel
    private lateinit var countryAdapter: ArrayAdapter<String>
    private lateinit var countryNameSlugMap: MutableMap<String, String>

    /* Classe para os serviços que serão acessados */
    private enum class Information(val type: String){
        DAY_ONE("Day one"),
        BY_COUNTRY("By country")
    }

    /* Classe para o status que será buscado no serviço */
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
        when (infoSp.selectedItem.toString()) {
            Information.DAY_ONE.type -> { fetchDayOne() }
            Information.BY_COUNTRY.type -> { fetchByCountry() }
        }
    }

    private fun countryAdapterInit() {
        /* Preenchido por Web Service */
        countryAdapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1)
        countryNameSlugMap = mutableMapOf()
        countrySp.adapter = countryAdapter
        viewModel.fetchCountries().observe(
            this,
            Observer { countryList ->
                countryList.sortedBy { it.country }.forEach { countryListItem ->
                    if ( countryListItem.country.isNotEmpty()) {
                        countryAdapter.add(countryListItem.country)
                        countryNameSlugMap[countryListItem.country] = countryListItem.slug
                    }
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

            // A nova versão dos serviços alterou a forma como dispomos os dados
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when (position) {
                    Information.DAY_ONE.ordinal -> {
                        viewModeTv.visibility = View.VISIBLE
                        viewModeRg.visibility = View.VISIBLE
                    }
                    Information.BY_COUNTRY.ordinal -> {
                        viewModeTv.visibility = View.GONE
                        viewModeRg.visibility = View.GONE
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

    private fun fetchDayOne() {
        val countrySlug = countryNameSlugMap[countrySp.selectedItem.toString()]!!

        viewModel.fetchDayOne(countrySlug, statusSp.selectedItem.toString()).observe(
            this,
            Observer { casesList ->
                if (viewModeTextRb.isChecked) {
                    /* Modo texto */
                    modoGrafico(ligado = false)
                    resultTv.text = casesListToString(casesList)
                }
                else {
                    /* Modo gráfico */
                    modoGrafico(ligado = true)
                    resultGv.removeAllSeries()
                    resultGv.gridLabelRenderer.resetStyles()

                    /* Preparando pontos */
                    val pointsArrayList = arrayListOf<DataPoint>()
                    casesList.forEach {
                        val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(it.date.substring(0,10))
                        val point = DataPoint(date, it.cases.toDouble())
                        pointsArrayList.add(point)
                    }
                    val pointsSeries = LineGraphSeries(pointsArrayList.toTypedArray())
                    resultGv.addSeries(pointsSeries)

                    /* Formatando gráfico */
                    resultGv.gridLabelRenderer.setHumanRounding(false)
                    resultGv.gridLabelRenderer.labelFormatter = DateAsXAxisLabelFormatter(this)

                    resultGv.gridLabelRenderer.numHorizontalLabels = 4
                    val primeiraData = Date(pointsArrayList.first().x.toLong())
                    val ultimaData = Date(pointsArrayList.last().x.toLong())
                    resultGv.viewport.setMinX(primeiraData.time.toDouble())
                    resultGv.viewport.setMaxX(ultimaData.time.toDouble())
                    resultGv.viewport.isXAxisBoundsManual = true

                    resultGv.gridLabelRenderer.numVerticalLabels = 4
                    resultGv.viewport.setMinY(pointsArrayList.first().y)
                    resultGv.viewport.setMaxY(pointsArrayList.last().y)
                    resultGv.viewport.isYAxisBoundsManual = true
                }
            }
        )
    }

    private fun fetchByCountry() {
        val countrySlug = countryNameSlugMap[countrySp.selectedItem.toString()]!!

        modoGrafico(ligado = false)
        viewModel.fetchByCountry(countrySlug, statusSp.selectedItem.toString()).observe(
            this,
            Observer { casesList ->
                resultTv.text = casesListToString(casesList)
            }
        )
    }

    private fun modoGrafico(ligado: Boolean) {
        if (ligado) {
            resultTv.visibility = View.GONE
            resultGv.visibility = View.VISIBLE
        }
        else {
            resultTv.visibility = View.VISIBLE
            resultGv.visibility = View.GONE
        }
    }

    private fun casesListToString(caseList: CaseList): String {
        val resultSb = StringBuffer()
        caseList.forEach {
            resultSb.append("Nome: ${it.country}\n")
            if (it.province.isNotEmpty()){
                resultSb.append("Estado/região: ${it.province}\n")
            }
            resultSb.append("Data: ${it.date.substring(0,10)}\n")
            resultSb.append("Casos: ${it.cases}\n\n")
        }
        return resultSb.toString()
    }
}