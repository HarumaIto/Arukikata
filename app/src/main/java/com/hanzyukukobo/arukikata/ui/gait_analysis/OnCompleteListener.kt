package com.hanzyukukobo.arukikata.ui.gait_analysis

enum class GaitAnalysisFragments(val position: Int) {
    VideoSelector(0),
    GaitAnalyzer(1),
    GaitResult(2),
    ShootVideo(3)
}

interface OnCompleteListener {
    fun onComplete(gaitAnalysisFragments: GaitAnalysisFragments)
}