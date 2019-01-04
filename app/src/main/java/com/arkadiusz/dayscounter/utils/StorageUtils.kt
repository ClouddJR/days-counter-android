package com.arkadiusz.dayscounter.utils

import android.net.Uri
import android.os.Environment
import java.io.*

/**
 * Created by arkadiusz on 28.03.18
 */

object StorageUtils {

    val BACKUP_PATH = "${Environment.getExternalStorageDirectory()}/DaysCounter_Backup"
    const val EXPORT_FILE_NAME = "dayscounter"
    const val EXPORT_FILE_EXTENSION = "realm"

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

    fun isCorrectFileChosenForImport(uri: Uri): Boolean {
        return uri.toString().isNotEmpty() && uri.toString().contains(".realm")
    }

    fun InputStream.toFile(path: String) {
        File(path).outputStream().use {
            this.copyTo(it)
        }
    }
}