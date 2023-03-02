package com.hanzyukukobo.arukikata.ui.gait_analysis

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.View.OnClickListener
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.media3.common.util.UnstableApi
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.hanzyukukobo.arukikata.R
import com.hanzyukukobo.arukikata.databinding.ActivityAnalyzerBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AnalyzerActivity : AppCompatActivity(),
    OnClickListener, OnCompleteListener, OnMoveRecording {

    private val viewModel: AnalyzerViewModel by viewModels()

    private lateinit var binding: ActivityAnalyzerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAnalyzerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.backPageButton.setOnClickListener(this)
        binding.nextPageButton.setOnClickListener(this)
        binding.restartPageButton.setOnClickListener(this)

        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.add(R.id.fragmentContainer, VideoSelectorFragment(this, this))
        fragmentTransaction.commit()
        viewModel.changeNavigationButtonVisibility(GaitAnalysisFragments.VideoSelector)
    }

    override fun onClick(v: View?) {
        if (v == null) return
        var position = 0
        when (v.id) {
            R.id.backPageButton -> {
                // 1ページ戻る
                viewModel.nowFragment.position.also {
                    position = if (it == GaitAnalysisFragments.ShootVideo.position) {
                        // 撮影ページであれば、VideoSelectorに戻る
                        0
                    } else {
                        it-1
                    }
                }
            }
            R.id.nextPageButton -> {
                // 1ページ進む
                position = viewModel.nowFragment.position+1
                viewModel.setNextButtonEnable(false)
            }
            R.id.restartPageButton -> {
                position = 0
                viewModel.reset()
            }
        }
        val fragment = GaitAnalysisFragments.values()[position]
        viewModel.changeNavigationButtonVisibility(fragment)
        viewModel.changeFragmentContainer(
            supportFragmentManager.beginTransaction(), fragment, this, this)
    }

    override fun onComplete(gaitAnalysisFragments: GaitAnalysisFragments) {
        viewModel.setNextButtonEnable(true)
    }

    override fun startRecording() {
        val fragment = GaitAnalysisFragments.ShootVideo
        viewModel.changeNavigationButtonVisibility(fragment)
        viewModel.changeFragmentContainer(
            supportFragmentManager.beginTransaction(), fragment, this)
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