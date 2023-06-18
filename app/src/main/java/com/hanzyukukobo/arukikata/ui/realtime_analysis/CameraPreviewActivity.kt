package com.hanzyukukobo.arukikata.ui.realtime_analysis

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.hanzyukukobo.arukikata.R
import com.hanzyukukobo.arukikata.databinding.ActivityCameraPreviewBinding
import com.hanzyukukobo.arukikata.data.PartialAngle

class CameraPreviewActivity : AppCompatActivity(), View.OnClickListener {

    private val viewModel: CameraPreviewViewModel by viewModels()

    private lateinit var binding: ActivityCameraPreviewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraPreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.overlayRecordButton.setOnClickListener(this)
        binding.showAngleButton.setOnClickListener(this)
        binding.showAngleButton.setOnLongClickListener {
            showMenu(it)
            true
        }
        binding.changeDetectionButton.setOnClickListener(this)

        if (!isPermissionGranted()) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.CAMERA), REQUEST_CODE_PERMISSIONS
            )
        } else {
            viewModel.startCamera(this, binding)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.release()
    }

    override fun onClick(v: View?) {
        if (v == null) return
        when (v.id) {
            binding.overlayRecordButton.id -> {
                if (viewModel.isRecording) {
                    viewModel.stopRecording()
                } else {
                    binding.previewView.also {
                        viewModel.startRecording(it.width, it.height, v)
                    }
                }
            }
            binding.showAngleButton.id -> {
                viewModel.changeAngleVisibility(v)
                viewModel.remakePoseProcessor()
            }
            binding.changeDetectionButton.id -> viewModel.changeDetection(v)
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

    private fun showMenu(v: View) {
        val popup = PopupMenu(this, v).apply {
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.upperAngle -> {
                        it.isChecked = viewModel.changePartialAngleVisibility(PartialAngle.UPPER_HALF_BODY, v)
                        viewModel.remakePoseProcessor()
                        true
                    }
                    R.id.lowerAngle -> {
                        it.isChecked = viewModel.changePartialAngleVisibility(PartialAngle.LOWER_HALF_BODY, v)
                        viewModel.remakePoseProcessor()
                        true
                    }
                    else -> false
                }
            }
        }
        val inflater: MenuInflater = popup.menuInflater
        inflater.inflate(R.menu.show_angle_popup_menu, popup.menu)
        // apply内で実装すると、まだmenuがセットされていないためindexOutOfBoundsではじかれてしまう
        // だからinflateでセットした後に実装する
        popup.menu.getItem(0).isChecked = viewModel.uiState.value!!.upperAngleVisible
        popup.menu.getItem(1).isChecked = viewModel.uiState.value!!.lowerAngleVisible
        popup.show()
    }

    private fun isPermissionGranted() =
        ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (!isPermissionGranted()) {
                Snackbar.make(
                    binding.previewView,
                    "カメラの権限が許可されていません",
                    Snackbar.LENGTH_LONG
                ).show()
            } else {
                viewModel.startCamera(this, binding)
            }
        }
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 1000
    }
}