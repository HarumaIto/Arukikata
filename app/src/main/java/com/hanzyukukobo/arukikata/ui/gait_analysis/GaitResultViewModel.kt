package com.hanzyukukobo.arukikata.ui.gait_analysis

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hanzyukukobo.arukikata.ui.common_widget.DetailResultPreviewFragment
import com.hanzyukukobo.arukikata.ui.common_widget.EasyResultPreviewFragment

data class GaitResultUiState(
    val changeButtonText: String,
    val easyPreview:EasyResultPreviewFragment?,
    val detailPreview: DetailResultPreviewFragment?
)

class GaitResultViewModel : ViewModel() {

    companion object {
        private const val detailText = "詳しくみる"
        private const val easyText = "簡単にみる"
    }

    private val _uiState = MutableLiveData(
        GaitResultUiState(
            detailText,
            null,
            null
        )
    )
    val uiState: LiveData<GaitResultUiState>
        get() = _uiState

    val isEasyPreview: Boolean get() = uiState.value?.easyPreview != null

    fun changeFragment(easyPreview: EasyResultPreviewFragment?, detailPreview: DetailResultPreviewFragment?) {
        _uiState.value = _uiState.value?.copy(
            changeButtonText = if (isEasyPreview) easyText else detailText,
            easyPreview = easyPreview,
            detailPreview = detailPreview
        )
    }
}