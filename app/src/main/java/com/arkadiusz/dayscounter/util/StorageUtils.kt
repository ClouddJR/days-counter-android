package com.arkadiusz.dayscounter.util

import android.content.Context
import android.net.Uri
import android.os.Environment
import java.io.*

object StorageUtils {

    const val EXPORT_FILE_NAME = "dayscounter"
    const val EXPORT_FILE_EXTENSION = "realm"

    fun saveImage(context: Context, sourceUri: Uri): Uri {
        val folder = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        folder!!.mkdir()

        val source = sourceUri.path
        val destination = "${folder.path}${File.separatorChar}${sourceUri.lastPathSegment}"

        FileInputStream(source).use { inputStream ->
            FileOutputStream(destination).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }

        return Uri.parse(destination)
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