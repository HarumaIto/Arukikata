package com.hanzyukukobo.arukikata.ui.log

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.hanzyukukobo.arukikata.R
import com.hanzyukukobo.arukikata.data.AnalysisLogEntity
import com.hanzyukukobo.arukikata.data.repositories.GaitAnalysisRepository
import com.hanzyukukobo.arukikata.ui.common_widget.DetailResultPreviewFragment
import com.hanzyukukobo.arukikata.ui.common_widget.EasyResultPreviewFragment
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class AnalysisLogUiState(
    val adapter: LogListAdapter,
    val changeButtonText: String,
    val easyPreview:EasyResultPreviewFragment?,
    val detailPreview: DetailResultPreviewFragment?
)

@HiltViewModel
class AnalysisLogViewModel @Inject constructor(
    application: Application,
    private val gaitAnalysisRepository: GaitAnalysisRepository
) : AndroidViewModel(application) {

    companion object {
        private const val detailText = "詳しくみる"
        private const val easyText = "簡単にみる"
    }

    private val _uiState = MutableLiveData(
        AnalysisLogUiState(
            LogListAdapter(getApplication(), R.layout.log_list_item, arrayListOf()),
            detailText,
            null,
            null
        ))
    val uiState: LiveData<AnalysisLogUiState>
        get() = _uiState

    val isEasyPreview: Boolean get() = uiState.value?.easyPreview != null

    fun changeFragment(easyPreview:EasyResultPreviewFragment?, detailPreview: DetailResultPreviewFragment?) {
        _uiState.value = _uiState.value?.copy(
            changeButtonText = if (isEasyPreview) easyText else detailText,
            easyPreview = easyPreview,
            detailPreview = detailPreview
        )
    }

    fun buildCharts(itemName: String) {
        if (isEasyPreview) {
            uiState.value?.easyPreview!!.buildCharts(itemName)
        } else {
            uiState.value?.detailPreview!!.buildCharts(itemName)
        }
    }

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
}