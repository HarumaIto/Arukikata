package com.hanzyukukobo.arukikata.ui.common_widget

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.hanzyukukobo.arukikata.data.JointLandmark
import com.hanzyukukobo.arukikata.data.Score
import com.hanzyukukobo.arukikata.data.VideoInfo
import com.hanzyukukobo.arukikata.data.repositories.GaitAnalysisRepository
import com.hanzyukukobo.arukikata.databinding.FragmentEasyResultPreviewBinding
import com.hanzyukukobo.arukikata.domain.BuildVideoPlayerUseCase
import com.hanzyukukobo.arukikata.domain.GetFramesChartDataUseCase
import com.hanzyukukobo.arukikata.domain.GetLogChartDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class EasyResultPreviewViewModel @Inject constructor(
    private val gaitAnalysisRepository: GaitAnalysisRepository,
    private val buildVideoPlayerUseCase: BuildVideoPlayerUseCase,
    private val getFramesChartDataUseCase: GetFramesChartDataUseCase,
    private val getLogChartDataUseCase: GetLogChartDataUseCase
) : ViewModel(), ScorePreview {

    private lateinit var binding: FragmentEasyResultPreviewBinding

    private var player: ExoPlayer? = null

    fun setBinding(_binding: FragmentEasyResultPreviewBinding) {
        binding = _binding
    }

    fun buildPlayer(context: Context, playerView: PlayerView) {
        player = buildVideoPlayerUseCase(context, playerView, true)
    }

    fun releasePlayer() {
        player?.release()
        player = null
    }

    fun canBuildResult(): VideoInfo? {
        return gaitAnalysisRepository.getVideoInfo()
    }

    override fun buildCharts(
        context: Context,
        itemName: String,
    ) {
        viewModelScope.launch {
            // UseCaseからデータを取ってくる内部関数
            suspend fun getChartData(isRefresh: Boolean, landmark: Int) = if (itemName.isEmpty()) {
                getFramesChartDataUseCase(context, isRefresh, landmark)
            } else {
                getLogChartDataUseCase(context, itemName, isRefresh, landmark)
            }

            val scores = mutableListOf<Score>()

            for (landmark in JointLandmark.LEFT_HIP .. JointLandmark.RIGHT_ANKLE) {
                val chartForData = getChartData(landmark == JointLandmark.LEFT_HIP, landmark)

                // 1と3は比較データのÝ軸データと生データのÝ軸データ
                val differences = calculateDifference(chartForData[1], chartForData[3])
                scores.add(calculateScore(differences))
                // 左と右のどちらのデータもそろったらバーを表示させる
                val size = scores.size
                if (size % 2 == 0) {
                    buildScoreBar(landmark, scores[size-2], scores[size-1])
                }
            }
            // ひじのデータはうまく連番では取り出せないので別で取り出す
            val rElbowData = getChartData(false, JointLandmark.RIGHT_ELBOW)
            val lElbowData = getChartData(false, JointLandmark.LEFT_ELBOW)
            buildOtherScoreBar(JointLandmark.RIGHT_ELBOW, rElbowData, lElbowData)
            val rHandData = getChartData(false, JointLandmark.RIGHT_WRIST)
            val lHandData = getChartData(false, JointLandmark.LEFT_WRIST)
            buildOtherScoreBar(JointLandmark.RIGHT_WRIST, rHandData, lHandData)

            setAllScore(scores)
        }
    }

    private suspend fun buildOtherScoreBar(landmark: Int, rData: List<List<Double>>, lData: List<List<Double>>) {
        val rDifferences = calculateDifference(rData[1], rData[3])
        val lDifferences = calculateDifference(lData[1], lData[3])
        buildScoreBar(landmark, calculateScore(rDifferences), calculateScore(lDifferences))
    }

    private suspend fun buildScoreBar(landmark: Int, leftScore: Score, rightScore: Score) {
        withContext(Dispatchers.Main) {
            val sum = leftScore.value + rightScore.value
            val average = sum/2

            val scoreBar = getScoreBar(landmark)
            scoreBar.setProgressValue(average.roundToInt())
        }
    }

    private fun getScoreBar(landmark: Int): HorizontalScoreBar {
        return when(landmark) {
            JointLandmark.RIGHT_HIP -> binding.hipScoreBar
            JointLandmark.RIGHT_KNEE -> binding.kneeScoreBar
            JointLandmark.RIGHT_ANKLE -> binding.ankleScoreBar
            JointLandmark.RIGHT_ELBOW -> binding.elbowScoreBar
            JointLandmark.RIGHT_WRIST -> binding.handScoreBar
            else -> binding.hipScoreBar
        }
    }

    private fun setAllScore(scores: MutableList<Score>) {
        val allScore = scores.let {
            val values = mutableListOf<Double>()
            scores.forEach { values.add(it.value) }
            calculateScore(values, false)
        }
        binding.scoreValueText.text = getSumScore(allScore)
    }
}