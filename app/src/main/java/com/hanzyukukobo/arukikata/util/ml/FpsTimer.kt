package com.hanzyukukobo.arukikata.util.ml

import java.util.*

class FpsTimer {

    var latestFps: Int = 0

    private var fpsTimer = Timer()
    private var numRuns = 0
    private var frameProcessedInOneSecondInterval = 0
    private var framesPerSecond = 0

    fun setTimer() {
        fpsTimer.scheduleAtFixedRate(
            object : TimerTask() {
                override fun run() {
                    framesPerSecond = frameProcessedInOneSecondInterval
                    frameProcessedInOneSecondInterval = 0
                }
            },
            0,
            1000
        )
    }

    fun calculateFps(): Int? {
        if (numRuns >= 500) {
            resetFpsStatus()
        }
        numRuns++
        frameProcessedInOneSecondInterval++
        if (frameProcessedInOneSecondInterval == 1) {
            latestFps = framesPerSecond
            return framesPerSecond
        }
        return null
    }

    fun resetFpsStatus() {
        numRuns = 0
        fpsTimer.cancel()
    }
}