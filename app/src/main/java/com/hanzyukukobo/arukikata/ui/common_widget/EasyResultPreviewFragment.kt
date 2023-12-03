package com.hanzyukukobo.arukikata.ui.common_widget

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import com.hanzyukukobo.arukikata.ArukikataApplication
import com.hanzyukukobo.arukikata.databinding.FragmentEasyResultPreviewBinding
import com.hanzyukukobo.arukikata.databinding.FragmentGaitResultBinding
import com.hanzyukukobo.arukikata.ui.gait_analysis.GaitAnalysisFragments
import dagger.hilt.android.AndroidEntryPoint
import hilt_aggregated_deps._com_hanzyukukobo_arukikata_ArukikataApplication_GeneratedInjector
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EasyResultPreviewFragment constructor(
    private val isBuildChart: Boolean
) : Fragment() {

    private var _binding: FragmentEasyResultPreviewBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EasyResultPreviewViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEasyResultPreviewBinding.inflate(layoutInflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        binding.hipScoreBar.setMinMaxValue("0%", "100%")
        binding.kneeScoreBar.setMinMaxValue("0%", "100%")
        binding.ankleScoreBar.setMinMaxValue("0%", "100%")
        binding.elbowScoreBar.setMinMaxValue("0%", "100%")
        binding.handScoreBar.setMinMaxValue("小さい", "大きい")

        viewModel.setBinding(binding)

        return binding.root
    }

    @UnstableApi
    override fun onResume() {
        super.onResume()
        viewModel.canBuildResult()?.also {
            // このページにたどり着くとき、基本的にはVideoInfoにデータは入っているので
            // VideoSelectorFragmentのようにUri.parse("")で初期化はしない
            binding.aspectRationFrameLayout.setAspectRatio(
                (it.width.toFloat()/it.height.toFloat())
            )

            val context = activity?.baseContext!!
            viewModel.buildPlayer(context, binding.videoView)
            if (isBuildChart) {
                buildCharts("")
            }
        }
    }

    fun buildCharts(itemName: String) {
        viewModel.buildCharts(activity!!.baseContext!!, itemName)
    }

    override fun onPause() {
        super.onPause()
        viewModel.releasePlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}