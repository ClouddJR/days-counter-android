package com.arkadiusz.dayscounter.util

import android.graphics.*
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.bumptech.glide.util.Util
import java.nio.ByteBuffer
import java.security.MessageDigest

class DimTransformation(private val dimValue: Int) : BitmapTransformation() {

    override fun transform(
        pool: BitmapPool,
        toTransform: Bitmap,
        outWidth: Int,
        outHeight: Int
    ): Bitmap {
        val bitmap = toTransform.copy(Bitmap.Config.ARGB_8888, true)
        val paint = Paint()
        val stringHex = Integer.toHexString(255 - (255 / 17 * dimValue))
        paint.colorFilter = PorterDuffColorFilter(
            "FF$stringHex$stringHex$stringHex".toLong(16).toInt(),
            PorterDuff.Mode.MULTIPLY
        )
        val canvas = Canvas(bitmap)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        return bitmap
    }

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update(ID_BYTES)
        messageDigest.update(ByteBuffer.allocate(4).putInt(dimValue).array())
    }

    override fun equals(other: Any?): Boolean {
        if (other is DimTransformation) {
            return dimValue == other.dimValue
        }
        return false
    }

    override fun hashCode(): Int {
        return Util.hashCode(ID.hashCode(), Util.hashCode(dimValue))
    }

    private companion object {
        const val ID = "com.arkadiusz.dayscounter.util.DimTransformation"
        val ID_BYTES = ID.toByteArray()
    }
}