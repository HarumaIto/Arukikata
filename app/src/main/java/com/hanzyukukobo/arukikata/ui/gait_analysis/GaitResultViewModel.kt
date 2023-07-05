package com.hanzyukukobo.arukikata.ui.gait_analysis

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hanzyukukobo.arukikata.ui.common_widget.DetailResultPreviewFragment
import com.hanzyukukobo.arukikata.ui.common_widget.EasyResultPreviewFragment

data class GaitResultUiState(
    val easyPreview:EasyResultPreviewFragment?,
    val detailPreview: DetailResultPreviewFragment?
)

class GaitResultViewModel : ViewModel() {
    private val _uiState = MutableLiveData(
        GaitResultUiState(
            null,
            null
        )
    )
    val uiState: LiveData<GaitResultUiState>
        get() = _uiState

    val isEasyPreview: Boolean get() = uiState.value?.easyPreview != null

    fun changeFragment(easyPreview: EasyResultPreviewFragment?, detailPreview: DetailResultPreviewFragment?) {
        _uiState.value = _uiState.value?.copy(
            easyPreview = easyPreview,
            detailPreview = detailPreview
        )
    }

    fun buildCharts(itemName: String) {
        if (uiState.value?.easyPreview != null) {
            uiState.value?.easyPreview!!.buildCharts(itemName)
        } else {
            uiState.value?.detailPreview!!.buildCharts(itemName)
        }
    }
}