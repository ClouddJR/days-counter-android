package com.arkadiusz.dayscounter.util

import android.content.Context
import android.net.Uri
import android.os.Environment
import java.io.*
import java.util.*

object StorageUtils {

    const val EXPORT_FILE_NAME = "dayscounter"
    const val EXPORT_FILE_EXTENSION = "realm"

    fun saveImage(context: Context, sourcePath: String): Uri {
        val folder = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        folder!!.mkdir()

        val destination = "${folder.path}${File.separatorChar}${UUID.randomUUID()}" +
                ".${sourcePath.substringAfterLast(".")}"

        FileInputStream(sourcePath).use { inputStream ->
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