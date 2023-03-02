package com.hanzyukukobo.arukikata.data

import android.net.Uri
import java.io.File

data class VideoInfo(
    val uri: Uri,
    val resultVideoFile: File,
    val width: Int,
    val height: Int,
    val rotation: Int,
    val duration: Int,
    val frameCount: Int,
)
