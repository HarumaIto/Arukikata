package com.hanzyukukobo.arukikata.ui.gait_analysis

import android.Manifest
import android.os.Bundle
import android.view.*
import android.view.View.OnClickListener
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.hanzyukukobo.arukikata.R
import com.hanzyukukobo.arukikata.databinding.ActivityShootVideoBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ShootVideoActivity: AppCompatActivity(), OnClickListener {

    private lateinit var binding: ActivityShootVideoBinding

    private val viewModel: ShootVideoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShootVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.recordingButton.setOnClickListener(this)

        val requestPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                viewModel.startCamera(this, this, binding.previewView.surfaceProvider)
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
                    viewModel.startRecording(this)
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        var returnVal = true
        if (item.itemId == android.R.id.home) {
            finish()
        } else {
            returnVal = super.onOptionsItemSelected(item)
        }
        return returnVal
    }
}