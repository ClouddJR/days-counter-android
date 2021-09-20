package com.arkadiusz.dayscounter.utils

import android.content.Context
import android.net.Uri
import android.os.Environment
import java.io.*

/**
 * Created by arkadiusz on 28.03.18
 */

object StorageUtils {

    const val EXPORT_FILE_NAME = "dayscounter"
    const val EXPORT_FILE_EXTENSION = "realm"

    fun saveFile(context: Context, sourceUri: Uri): Uri {
        val folder = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        folder!!.mkdir()

        val sourceFilename = sourceUri.path
        val destinationFilename = folder.path + File.separatorChar + sourceUri.lastPathSegment

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

    fun getBackupPath(context: Context): String {
        return context.getExternalFilesDir(null)!!.toString() + "/Backup"
    }

    fun isCorrectFileChosenForImport(uri: Uri): Boolean {
        return uri.toString().isNotEmpty() && uri.toString().contains(".realm")
    }

    fun InputStream.toFile(path: String) {
        File(path).outputStream().use {
            this.copyTo(it)
        }
    }
}