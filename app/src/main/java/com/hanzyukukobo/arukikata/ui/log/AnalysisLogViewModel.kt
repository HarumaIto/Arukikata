package com.hanzyukukobo.arukikata.ui.log

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.hanzyukukobo.arukikata.R
import com.hanzyukukobo.arukikata.data.AnalysisLogEntity
import com.hanzyukukobo.arukikata.data.repositories.GaitAnalysisRepository
import com.hanzyukukobo.arukikata.databinding.ActivityAnalysisLogBinding
import com.hanzyukukobo.arukikata.domain.GetLogChartDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class AnalysisLogUiState(
    val adapter: LogListAdapter
)

@HiltViewModel
class AnalysisLogViewModel @Inject constructor(
    application: Application,
    private val gaitAnalysisRepository: GaitAnalysisRepository,
    private val getLogChartDataUseCase: GetLogChartDataUseCase
) : AndroidViewModel(application) {

    private val _uiState = MutableLiveData(
        AnalysisLogUiState(
            LogListAdapter(
                getApplication(), R.layout.log_list_item, arrayListOf())))
    val uiState: LiveData<AnalysisLogUiState>
        get() = _uiState

    fun refreshAdapter() {
        viewModelScope.launch(Dispatchers.Main) {
            val logs: List<AnalysisLogEntity>? = try {
                // データがないとエラーを吐くので回収する
                gaitAnalysisRepository.getAllLogs()
            } catch (e: java.lang.IllegalStateException) {
                e.printStackTrace()
                null
            }

            val listItems: MutableList<LogListItem> = mutableListOf()
            if (logs != null && logs.isNotEmpty()) {
                for (log in logs) {
                    val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.JAPAN)
                    dateFormat.timeZone = TimeZone.getTimeZone("Asia/Tokyo")

                    listItems.add(LogListItem(log.name, dateFormat.format(log.time)))
                }
            } else {
                listItems.add(LogListItem("履歴がありません", ""))
            }

            _uiState.value?.adapter?.addAll(listItems)
        }
    }

    fun buildCharts(itemName: String, binding: ActivityAnalysisLogBinding) {
        viewModelScope.launch {
            binding.scoreChartsPreview.buildCharts(
                itemName, getFramesChartDataUseCase = null, getLogChartDataUseCase)
        }
    }
}