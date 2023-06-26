package com.hanzyukukobo.arukikata.ui.gait_analysis

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.media3.common.util.UnstableApi
import com.google.android.material.snackbar.Snackbar
import com.hanzyukukobo.arukikata.R
import com.hanzyukukobo.arukikata.databinding.FragmentVideoSelectorBinding
import com.hanzyukukobo.arukikata.ui.log.AnalysisLogActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VideoSelectorFragment constructor(
    private val onCompleteListener: OnCompleteListener
) : Fragment(), OnClickListener {

    private var _binding: FragmentVideoSelectorBinding? = null
    private val binding get() = _binding!!

    private val viewModel: VideoSelectorViewModel by viewModels()

    // onCreateなどのライフサイクルが呼ばれる前にインスタンスを作成しないとエラーが発生する
    @UnstableApi
    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            val uri = it.data?.data!!
            Log.d("test", uri.toString())
            viewModel.pickedVideoFromGallery(activity?.baseContext!!, uri, binding)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVideoSelectorBinding.inflate(layoutInflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        viewModel.onCompleteListener = onCompleteListener

        return binding.root
    }

    @UnstableApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.shootVideoButton.setOnClickListener(this)
        binding.selectVideoButton.setOnClickListener(this)

        binding.aspectRationFrameLayout.setAspectRatio(16f/9f)

        if (!isPermissionGranted()) {
            val requestPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
                if (!it)  {
                    Snackbar.make(
                        binding.textView,
                        "ストレージにアクセスする権限が許可されていません",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
            requestPermission.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    override fun onResume() {
        super.onResume()
        // 動画がすでにピックされているかどうかで、メソッドの内部のifで判断するため、
        // if (!isPermissionGranted()) のelseには配置しない
        viewModel.buildPlayer(activity?.baseContext!!, binding.videoView)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.releasePlayer()
        _binding = null
    }

    @UnstableApi
    override fun onClick(v: View?) {
        if (v == null) return
        when (v.id) {
            R.id.shootVideoButton -> {
                val intent = Intent(activity!!.baseContext, ShootVideoActivity::class.java)
                startActivity(intent)
            }
            R.id.selectVideoButton -> {
                viewModel.pickVideoFromGallery(launcher)
            }
        }
    }

    private fun isPermissionGranted(): Boolean {
        var result = true
        if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            if (ContextCompat.checkSelfPermission(activity?.baseContext!!, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                result = false
            }
        }
        return result
    }
}