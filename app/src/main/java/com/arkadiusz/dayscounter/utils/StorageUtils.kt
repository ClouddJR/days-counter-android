package com.arkadiusz.dayscounter.utils

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import java.io.*

/**
 * Created by arkadiusz on 28.03.18
 */

object StorageUtils {

    fun saveFile(sourceUri: Uri): Uri {

        val folder = File(Environment.getExternalStorageDirectory().toString() + "/croppedImages")
        folder.mkdir()

        val sourceFilename = sourceUri.path
        val destinationFilename = Environment.getExternalStorageDirectory().path + File.separatorChar + "croppedImages/" + sourceUri.lastPathSegment

        var bis: BufferedInputStream? = null
        var bos: BufferedOutputStream? = null

        try {
            bis = BufferedInputStream(FileInputStream(sourceFilename))
            bos = BufferedOutputStream(FileOutputStream(destinationFilename, false))
            val buf = ByteArray(1024)
            bis.read(buf)
            do {
                bos.write(buf)
            } while (bis.read(buf) != -1)
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            bis?.close()
            bos?.close()
            return Uri.parse(destinationFilename)
        }
    }

    fun getRealPathFromURI(contentURI: Uri, context: Context): String {
        val result: String
        val cursor = context.contentResolver
                .query(contentURI, null, null, null, null) //security exception
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.path
        } else {
            cursor.moveToFirst()
            val idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
            result = cursor.getString(idx)
            cursor.close()
        }
        return result
    }
}