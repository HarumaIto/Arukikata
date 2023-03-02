package com.hanzyukukobo.arukikata.ui.gait_analysis

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.hanzyukukobo.arukikata.R
import com.hanzyukukobo.arukikata.databinding.FragmentGaitAnalyzerBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GaitAnalyzerFragment constructor(
    private val onCompleteListener: OnCompleteListener
) : Fragment(), OnClickListener {

    private val viewModel: GaitAnalyzerViewModel by viewModels()

    private var _binding: FragmentGaitAnalyzerBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGaitAnalyzerBinding.inflate(layoutInflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.startMlButton.setOnClickListener(this)
    }

    override fun onPause() {
        super.onPause()
        // 分析の実行中にUIから離れるときの処理を追加する
        viewModel.resetPrivateValues()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onClick(v: View?) {
        if (v == null) return
        when (v.id) {
            R.id.startMlButton -> {
                viewModel.processVideoFrames(activity?.baseContext!!, v, onCompleteListener)
            }
        }
    }
}