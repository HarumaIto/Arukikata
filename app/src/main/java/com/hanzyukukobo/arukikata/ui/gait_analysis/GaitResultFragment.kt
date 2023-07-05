package com.hanzyukukobo.arukikata.ui.gait_analysis

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.media3.common.util.UnstableApi
import com.hanzyukukobo.arukikata.R
import com.hanzyukukobo.arukikata.databinding.FragmentGaitResultBinding
import com.hanzyukukobo.arukikata.ui.common_widget.DetailResultPreviewFragment
import com.hanzyukukobo.arukikata.ui.common_widget.EasyResultPreviewFragment
import com.hanzyukukobo.arukikata.ui.common_widget.ScorePreview
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class GaitResultFragment constructor(
    private val onCompleteListener: OnCompleteListener
) : Fragment(), OnClickListener {

    private var _binding: FragmentGaitResultBinding? = null
    private val binding get() = _binding!!

    private val viewModel: GaitResultViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGaitResultBinding.inflate(layoutInflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        binding.resultChangePreviewButton.setOnClickListener(this)

        val easyPreview = EasyResultPreviewFragment(true)
        childFragmentManager.beginTransaction().apply {
            add(R.id.resultFragmentContainer, easyPreview)
            commit()
        }
        viewModel.changeFragment(easyPreview, null)
        return binding.root
    }

    @UnstableApi
    override fun onResume() {
        super.onResume()
        onCompleteListener.onComplete(GaitAnalysisFragments.GaitResult)
    }

    override fun onClick(v: View?) {
        if (v == null) return

        val fragment = if (viewModel.isEasyPreview)
            DetailResultPreviewFragment(true) else EasyResultPreviewFragment(true)
        childFragmentManager
            .beginTransaction()
            .replace(R.id.resultFragmentContainer, fragment)
            .commit()
        if (viewModel.isEasyPreview) {
            viewModel.changeFragment(null, fragment as DetailResultPreviewFragment)
        } else {
            viewModel.changeFragment(fragment as EasyResultPreviewFragment, null)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}