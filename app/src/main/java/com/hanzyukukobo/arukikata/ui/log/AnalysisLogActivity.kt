package com.hanzyukukobo.arukikata.ui.log

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.View.OnClickListener
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.hanzyukukobo.arukikata.R
import com.hanzyukukobo.arukikata.databinding.ActivityAnalysisLogBinding
import com.hanzyukukobo.arukikata.ui.common_widget.DetailResultPreviewFragment
import com.hanzyukukobo.arukikata.ui.common_widget.EasyResultPreviewFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AnalysisLogActivity : AppCompatActivity(R.layout.activity_analysis_log), OnItemClickListener, OnClickListener {

    private lateinit var binding: ActivityAnalysisLogBinding

    private val viewModel: AnalysisLogViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_analysis_log)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.logsListView.onItemClickListener = this
        binding.logChangePreviewButton.setOnClickListener(this)

        val easyPreview = EasyResultPreviewFragment(false)
        supportFragmentManager.beginTransaction().apply {
            add(R.id.logFragmentContainer, easyPreview)
            commit()
        }
        viewModel.changeFragment(easyPreview, null)
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshAdapter()
    }

    override fun onClick(v: View?) {
        if (v == null) return

        supportFragmentManager.beginTransaction().also {
            val fragment: Fragment = if (viewModel.isEasyPreview) {
                DetailResultPreviewFragment(false).apply {
                    viewModel.changeFragment(null, this)
                }
            } else {
                EasyResultPreviewFragment(false).apply {
                    viewModel.changeFragment(this, null)
                }
            }

            it.replace(R.id.logFragmentContainer, fragment)
            it.commit()
        }
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        Log.d("test", "item: ${parent?.adapter?.getItem(id.toInt())}")
        viewModel.buildCharts((parent?.adapter?.getItem(id.toInt()) as LogListItem).name)
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