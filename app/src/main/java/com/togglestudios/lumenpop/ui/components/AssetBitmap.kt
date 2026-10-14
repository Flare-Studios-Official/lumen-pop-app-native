package com.togglestudios.lumenpop.ui.components

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import java.util.concurrent.ConcurrentHashMap

object AssetCache {
    private val cache = ConcurrentHashMap<String, ImageBitmap>()

    fun getOrLoad(context: Context, filename: String): ImageBitmap? {
        cache[filename]?.let { return it }
        return try {
            context.assets.open(filename).use { stream ->
                val bitmap = BitmapFactory.decodeStream(stream)
                val imageBitmap = bitmap.asImageBitmap()
                cache[filename] = imageBitmap
                imageBitmap
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

@Composable
fun rememberAssetImage(filename: String): ImageBitmap? {
    val context = LocalContext.current
    return remember(filename) {
        AssetCache.getOrLoad(context, filename)
    }
}
