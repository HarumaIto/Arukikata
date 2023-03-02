package com.hanzyukukobo.arukikata.data.sources

import android.content.Context
import android.util.Log
import com.hanzyukukobo.arukikata.data.JointLandmark
import com.hanzyukukobo.arukikata.data.JointsAngle
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.*
import javax.inject.Inject

class CompareDataLocalDataSource @Inject constructor() {
    companion object {
        val FILE_NAME = "comparison_data.csv"
    }

    fun loadDataForAssets(context: Context): List<Map<String, Any>> {
        val result = mutableListOf<Map<String, Any>>()
        try {
            val inputStream = context.assets.open(FILE_NAME)
            BufferedReader(InputStreamReader(inputStream)).use {
                // ヘッダーは使わないから呼び出して破棄
                Log.d("test", "header: ${it.readLine()}")

                val lines = it.readLines()
                for (i in lines.indices) {
                    val line = lines[i]
                    val tokenizer = StringTokenizer(line, ",")

                    val time = tokenizer.nextToken().toFloat()
                    val landmarks = mutableListOf<JointLandmark>()
                    for (j in 0 until tokenizer.countTokens()) {
                        val jointLandmark = JointLandmark(j, tokenizer.nextToken().toDouble())
                        landmarks.add(jointLandmark)
                    }
                    val jointsAngle = JointsAngle(landmarks)
                    result.add(mapOf(
                        "time" to time,
                        "jointsAngle" to jointsAngle
                    ))
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return result
    }
}