package com.hanzyukukobo.arukikata.ui.gait_analysis

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.snackbar.Snackbar
import com.hanzyukukobo.arukikata.R
import com.hanzyukukobo.arukikata.databinding.FragmentShootVideoBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ShootVideoFragment(
    private val onCompleteListener: OnCompleteListener
) : Fragment(), OnClickListener {

    private var _binding: FragmentShootVideoBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ShootVideoViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShootVideoBinding.inflate(layoutInflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        binding.recordingButton.setOnClickListener(this)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val requestPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                viewModel.startCamera(
                    activity?.baseContext!!,
                    this,
                    binding.previewView.surfaceProvider)
            } else {
                Snackbar.make(
                    binding.previewView,
                    "マイクの権限が許可されていません",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
        requestPermission.launch(Manifest.permission.RECORD_AUDIO)
    }

    override fun onClick(v: View?) {
        if (v == null) return
        when (v.id) {
            R.id.recordingButton -> {
                if (viewModel.uiState.value?.isRecording!!) {
                    viewModel.stopRecording()
                } else {
                    viewModel.startRecording(activity?.baseContext!!, onCompleteListener)
                }
            }
        }
    }
}