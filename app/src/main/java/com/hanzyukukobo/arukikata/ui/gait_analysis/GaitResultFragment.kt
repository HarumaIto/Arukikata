package com.hanzyukukobo.arukikata.ui.gait_analysis

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.media3.common.util.UnstableApi
import com.hanzyukukobo.arukikata.R
import com.hanzyukukobo.arukikata.databinding.FragmentGaitResultBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class GaitResultFragment @Inject constructor(
    private val onCompleteListener: OnCompleteListener
): Fragment(), OnClickListener {

    private var _binding: FragmentGaitResultBinding? = null
    private val binding get() = _binding!!

    private val viewModel: GaitResultViewModel by viewModels()

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            viewModel.exportCSVFile(it.data?.data!!, activity?.baseContext!!, requireView())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGaitResultBinding.inflate(layoutInflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        binding.saveLogButton.setOnClickListener(this)
        binding.csvExportButton.setOnClickListener(this)
        binding.videoExportButton.setOnClickListener(this)

        return binding.root
    }

    @UnstableApi
    override fun onResume() {
        super.onResume()
        viewModel.canBuildResult()?.let {
            // このページにたどり着くとき、基本的にはVideoInfoにデータは入っているので
            // VideoSelectorFragmentのようにUri.parse("")で初期化はしない
            binding.aspectRationFrameLayout.setAspectRatio(
                (it.width.toFloat()/it.height.toFloat())
            )

            val context = activity?.baseContext!!
            viewModel.buildPlayer(context, binding.videoView)
            viewModel.buildCharts(binding)

            onCompleteListener.onComplete(GaitAnalysisFragments.GaitResult)
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.releasePlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onClick(v: View?) {
        if (v == null) return
        when (v.id) {
            R.id.saveLogButton -> viewModel.saveLocalDatabase()
            R.id.csvExportButton -> viewModel.createDocumentFile(launcher)
            R.id.videoExportButton -> viewModel.copyResultVideo()
        }
    }
}