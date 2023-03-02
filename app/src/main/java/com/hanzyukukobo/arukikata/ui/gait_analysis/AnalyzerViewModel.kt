package com.hanzyukukobo.arukikata.ui.gait_analysis

import android.view.View
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hanzyukukobo.arukikata.R
import com.hanzyukukobo.arukikata.data.repositories.GaitAnalysisRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

data class AnalyzerUiState (
    val backButtonVisibility: Int = View.INVISIBLE,
    val nextButtonVisibility: Int = View.VISIBLE,
    val isNextButtonEnable: Boolean = true,
    val restartButtonVisibility: Int = View.GONE
)

@HiltViewModel
class AnalyzerViewModel @Inject constructor(
    private val gaitAnalysisRepository: GaitAnalysisRepository
) : ViewModel() {

    private val _uiState = MutableLiveData(AnalyzerUiState())
    val uiState: LiveData<AnalyzerUiState>
        get() = _uiState

    var nowFragment: GaitAnalysisFragments = GaitAnalysisFragments.VideoSelector

    fun changeNavigationButtonVisibility(gaitAnalysisFragments: GaitAnalysisFragments) {
        nowFragment = gaitAnalysisFragments
        _uiState.value = _uiState.value?.let {
            when (gaitAnalysisFragments) {
                GaitAnalysisFragments.VideoSelector -> {
                    it.copy(
                        backButtonVisibility = View.GONE,
                        nextButtonVisibility = View.VISIBLE,
                        restartButtonVisibility = View.GONE,
                        isNextButtonEnable = false
                    )
                }
                GaitAnalysisFragments.GaitAnalyzer -> {
                    it.copy(
                        backButtonVisibility = View.VISIBLE,
                        nextButtonVisibility = View.VISIBLE,
                        isNextButtonEnable = false
                    )
                }
                GaitAnalysisFragments.GaitResult -> {
                    it.copy(
                        backButtonVisibility = View.GONE,
                        nextButtonVisibility = View.GONE,
                        restartButtonVisibility = View.VISIBLE,
                        isNextButtonEnable = false
                    )
                }
                GaitAnalysisFragments.ShootVideo -> {
                    it.copy(
                        backButtonVisibility = View.VISIBLE,
                        nextButtonVisibility = View.GONE,
                        restartButtonVisibility = View.GONE,
                    )
                }
            }
        }
    }

    fun setNextButtonEnable(isEnable: Boolean) {
        _uiState.value = _uiState.value?.copy(
            isNextButtonEnable = isEnable
        )
    }

    fun changeFragmentContainer(
        fragmentTransaction: FragmentTransaction,
        gaitAnalysisFragments: GaitAnalysisFragments,
        onCompleteListener: OnCompleteListener,
        onMoveRecording: OnMoveRecording? = null
    ) {
        fragmentTransaction.also {
            it.replace(
                R.id.fragmentContainer,
                when (gaitAnalysisFragments) {
                    GaitAnalysisFragments.VideoSelector -> VideoSelectorFragment(onCompleteListener, onMoveRecording!!)
                    GaitAnalysisFragments.GaitAnalyzer -> GaitAnalyzerFragment(onCompleteListener)
                    GaitAnalysisFragments.GaitResult -> GaitResultFragment(onCompleteListener)
                    GaitAnalysisFragments.ShootVideo -> ShootVideoFragment(onCompleteListener)
                }
            )
            it.commit()
        }
    }

    fun reset() {
        gaitAnalysisRepository.resetVideoInfo()
    }
}