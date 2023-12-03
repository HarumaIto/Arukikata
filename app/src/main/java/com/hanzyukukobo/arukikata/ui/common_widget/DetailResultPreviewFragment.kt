package com.hanzyukukobo.arukikata.ui.common_widget

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.hanzyukukobo.arukikata.ArukikataApplication
import com.hanzyukukobo.arukikata.R
import com.hanzyukukobo.arukikata.databinding.FragmentDetailResultPreviewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DetailResultPreviewFragment constructor(
    private val isBuildChart: Boolean
) : Fragment(), OnClickListener {
    private var _binding: FragmentDetailResultPreviewBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DetailResultPreviewViewModel by viewModels()

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
        _binding = FragmentDetailResultPreviewBinding.inflate(layoutInflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        binding.saveLogButton.setOnClickListener(this)
        binding.csvExportButton.setOnClickListener(this)
        binding.videoExportButton.setOnClickListener(this)

        viewModel.setBinding(binding)

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        buildCharts("")
    }

    fun buildCharts(itemName: String) {
        viewModel.buildCharts(activity!!.baseContext!!, itemName)
    }

    override fun onClick(v: View?) {
        if (v == null) return
        when (v.id) {
            R.id.saveLogButton -> viewModel.saveLocalDatabase()
            R.id.csvExportButton -> viewModel.createDocumentFile(launcher)
            R.id.videoExportButton -> viewModel.copyResultVideo()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}