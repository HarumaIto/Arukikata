package com.hanzyukukobo.arukikata.ui.log

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.hanzyukukobo.arukikata.R
import com.hanzyukukobo.arukikata.databinding.ActivityAnalysisLogBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AnalysisLogActivity : AppCompatActivity(R.layout.activity_analysis_log), OnItemClickListener {

    private lateinit var binding: ActivityAnalysisLogBinding

    private val viewModel: AnalysisLogViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_analysis_log)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.logsListView.onItemClickListener = this
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshAdapter()
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        Log.d("test", "item: ${parent?.adapter?.getItem(id.toInt())}")
        viewModel.buildCharts((parent?.adapter?.getItem(id.toInt()) as LogListItem).name, binding)
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