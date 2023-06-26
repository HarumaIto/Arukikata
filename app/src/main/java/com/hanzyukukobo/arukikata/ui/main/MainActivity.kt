package com.hanzyukukobo.arukikata.ui.main

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.hanzyukukobo.arukikata.R
import com.hanzyukukobo.arukikata.databinding.ActivityMainBinding
import com.hanzyukukobo.arukikata.ui.gait_analysis.AnalyzerActivity
import com.hanzyukukobo.arukikata.ui.log.AnalysisLogActivity
import com.hanzyukukobo.arukikata.ui.realtime_analysis.CameraPreviewActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity(R.layout.activity_main), OnClickNextButton {

    private lateinit var binding: ActivityMainBinding

    private lateinit var listAdapter: CardListAdapter

    private val mainCards = arrayListOf(
        MainCard(
            "リアルタイム検出",
            "カメラを使ってリアルタイムで検出処理をします",
            R.drawable.warrier2_sketch),
        MainCard(
            "歩行分析",
            "端末に保存されている動画を使用して、AIを使った歩行分析をします",
            R.drawable.pose_detection),
        MainCard(
            "分析履歴",
            "保存してある分析結果の関節角度から求められるスコアを表示します",
            R.drawable.text_recognition),
        MainCard(
            "DPP Test",
            "WiFi easy connectのテストボタンです",
            R.drawable.baseline_android_24)
    )

    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult? ->
        if (result?.resultCode == Activity.RESULT_OK) {
            Log.i("ddp result", result.data!!.toString())
        }
        Log.d("dpp result", "nannkarresultkita: ${result.toString()}")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // DataBindingのインスタンスを生成
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        // ライフサイクル所有者を設定
        binding.lifecycleOwner = this

        listAdapter = CardListAdapter(mainCards, this)
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(baseContext)
            adapter = listAdapter
        }
    }

    override fun onNext(mainCard: MainCard) {
        when (mainCard) {
            mainCards[0] -> {
                val intent = Intent(this, CameraPreviewActivity::class.java)
                startActivity(intent)
            }
            mainCards[1] -> {
                val intent = Intent(this, AnalyzerActivity::class.java)
                startActivity(intent)
            }
            mainCards[2] -> {
                val intent = Intent(this, AnalysisLogActivity::class.java)
                startActivity(intent)
            }
            mainCards[3] -> {
                // WifiDPP関連
                val wifiManager: WifiManager = getSystemService(Context.WIFI_SERVICE) as WifiManager

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    if (wifiManager.isEasyConnectSupported) {
                        val intent = Intent("android.settings.WIFI_DPP_ENROLLEE_QR_CODE_SCANNER")
                        startForResult.launch(intent)
                    }
                } else {
                    TODO("VERSION.SDK_INT < Q")
                }
            }
        }
    }
}