package com.hanzyukukobo.arukikata.domain

import com.hanzyukukobo.arukikata.data.FrameEntity
import com.hanzyukukobo.arukikata.data.JointsAngle
import java.util.*
import java.util.stream.Collectors
import javax.inject.Inject
import kotlin.math.abs

class GetChartDataUseCase @Inject constructor() {
    operator fun invoke(
        resultData: List<FrameEntity>,
        compareData: List<Map<String, Any>>,
        landmark: Int
    ): List<List<Double>> {
        val compareDataList = compareDataToList(compareData, landmark)
        val jointLandmarkData = framesToList(resultData, landmark)
        compareDataList[KEY_INDEXES] = normalizeList(compareDataList[KEY_INDEXES]!!)
        jointLandmarkData[KEY_INDEXES] = normalizeList(jointLandmarkData[KEY_INDEXES]!!)

        var x0 = compareDataList[KEY_INDEXES]!!
        var x1 = jointLandmarkData[KEY_INDEXES]!!
        var y0 = compareDataList[KEY_ANGLES]!!
        var y1 = jointLandmarkData[KEY_ANGLES]!!

        if (compareDataList[KEY_INDEXES]!!.size != jointLandmarkData[KEY_INDEXES]!!.size) {
            if (compareDataList[KEY_INDEXES]!!.size > jointLandmarkData[KEY_INDEXES]!!.size) {
                val convertedData = convertToComputational(
                    compareDataList,
                    jointLandmarkData
                )
                x1 = convertedData[KEY_INDEXES]!!
                y1 = convertedData[KEY_ANGLES]!!
            } else {
                val convertedData = convertToComputational(
                    jointLandmarkData,
                    compareDataList
                )
                x0 = convertedData[KEY_INDEXES]!!
                y0 = convertedData[KEY_ANGLES]!!
            }
        }
        return listOf(x0, y0, x1, y1)
    }

    private fun compareDataToList(compareData: List<Map<String, Any>>, landmark: Int): MutableMap<String, List<Double>> {
        val indexes = mutableListOf<Double>()
        val angles = mutableListOf<Double>()

        for (i in compareData.indices) {
            indexes.add(i.toDouble())
            // 180を下にして0を上にしたいから(-)をつけて逆転させる
            angles.add(
                -(compareData[i][KEY_JOINT_ANGLE] as JointsAngle).getJointLandmark(landmark)!!.angle)
        }
        return mutableMapOf(
            KEY_INDEXES to indexes,
            KEY_ANGLES to angles
        )
    }

    // targetと最も近い要素のIndexを取得する
    private fun getApproximate(indexes: List<Double>, target: Double): IntArray {
        // 最終的に取得したいデータは、targetより多きものと小さいものをひとつずつであるから
        val elements = 2
        var left = 0
        var right: Int = indexes.size-1

        while (right - left >= elements) {
            if (abs(indexes[left] - target) > abs(indexes[right] - target)) {
                left++
            } else {
                right--
            }
        }
        val doubleArray = Arrays.stream(
            indexes.toDoubleArray(), left, right+1).boxed().collect(Collectors.toList())
        // targetと近似するValueが取得できるので、そのValueが存在するIndexを戻り値とする
        return intArrayOf(
            getMatchedIndex(indexes, doubleArray[0]),
            getMatchedIndex(indexes, doubleArray[1]))
    }

    private fun getMatchedIndex(indexes: List<Double>, target: Double): Int {
        for (i in indexes.indices) {
            if (target == indexes[i]) {
                // 一致する要素があればその時点のindexを返す
                return i
            }
        }
        // 一致しなければ-1を返す
        return -1
    }

    // データ量の多いほうをcomparisonDataに入れる
    // dataに入れられたMapのデータを変換して戻り値とする
    private fun convertToComputational(
        compareData: Map<String, List<Double>>,
        data: Map<String, List<Double>>): Map<String, List<Double>> {
        val compareIndexes = compareData[KEY_INDEXES] as List<Double>
        val dataIndexes = data[KEY_INDEXES] as List<Double>
        val dataAngles = data[KEY_ANGLES] as List<Double>
        val completedAngles = mutableListOf<Double>()

        for (i in compareIndexes.indices) {
            val index = compareIndexes[i]
            if (i == 0) {
                // 1つ目のデータの時
                completedAngles.add(dataAngles[0])
            } else if (i == compareIndexes.lastIndex) {
                completedAngles.add(dataAngles[dataIndexes.lastIndex])
            } else {
                val matchedIndex = getMatchedIndex(dataIndexes, index)
                if (matchedIndex != -1) {
                    // 比較元と一致するindexがあった時
                    completedAngles.add(dataAngles[matchedIndex])
                } else {
                    // 線形補完で比較元と同じ位置のデータを取得する
                    val approximations = getApproximate(dataIndexes, index)
                    val min: Int
                    val max: Int
                    if (approximations[0] < approximations[1]) {
                        min = approximations[0]
                        max = approximations[1]
                    } else {
                        min = approximations[1]
                        max = approximations[0]
                    }
                    completedAngles.add(lerp(
                        dataIndexes[min], dataAngles[min],
                        dataIndexes[max], dataAngles[max],
                        index
                    ))
                }
            }
        }
        return mapOf(
            KEY_INDEXES to compareIndexes,
            KEY_ANGLES to completedAngles
        )
    }

    private fun framesToList(frames: List<FrameEntity>, landmark: Int): MutableMap<String, List<Double>> {
        val indexes = mutableListOf<Double>()
        val angles = mutableListOf<Double>()

        for (frame in frames) {
            indexes.add(frame.index.toDouble())
            // 180を下にして0を上にしたいから(-)をつけて逆転させる
            angles.add(-frame.jointsAngle.getJointLandmark(landmark)!!.angle)
        }
        return mutableMapOf(
            KEY_INDEXES to indexes,
            KEY_ANGLES to angles
        )
    }

    // 線形補完
    private fun lerp(x0: Double, y0: Double, x1: Double, y1: Double, x: Double): Double {
        return y0 + (y1 - y0) * (x - x0) / (x1 - x0)
    }

    private fun normalizeList(list: List<Double>): List<Double> {
        val max = list.size-1
        val newList = mutableListOf<Double>()
        for (element in list) {
            newList.add(normalization(element, max))
        }
        return newList
    }

    private fun normalization(value: Double, max: Int) = value / max.toDouble() * 100

    companion object {
        val KEY_INDEXES = "indexes"
        val KEY_ANGLES = "angles"
        val KEY_JOINT_ANGLE = "jointsAngle"
    }
}