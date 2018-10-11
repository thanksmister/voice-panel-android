/*
 * Copyright (c) 2018 ThanksMister LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.thanksmister.iot.voicepanel.utils

import android.content.Context
import timber.log.Timber
import java.io.*
import java.util.zip.ZipInputStream

/**
 * Created by Michael Ritchie on 9/6/18.
 */
class FileUtils {

    companion object {

        fun getAssistantDirectory(appContext: Context): File {
            val rootDir: File by lazy { appContext.filesDir }
            val assistantDir by lazy { File(rootDir, "assistant") }
            Timber.d("Rood Dir: " + rootDir.path)
            Timber.d("Assistant Dir: " + assistantDir.path)
            return assistantDir
        }

        fun unzipAssistantDirectory(appContext: Context, version: String):Boolean {
            val assistantDir = getAssistantDirectory(appContext)
            val versionFile by lazy { File(assistantDir, "android_version_$version") }
            Timber.d("Unzip File Version Name: " + versionFile.name)
            return !versionFile.exists()
        }

        fun doUnzipAssistantDirectory(appContext: Context, version: String): Boolean {
            Timber.d("doUnzipAssistantDirectory")
            val rootDir: File by lazy { appContext.filesDir }
            val assistantDir by lazy { File(rootDir, "assistant") }
            val versionFile by lazy { File(assistantDir, "android_version_$version") }
            try {
                assistantDir.deleteRecursively()
            } catch (e: Exception) {
                Timber.d("could not clean previous assistant dir")
            }
            if (!versionFile.exists()) {
                assistantDir.mkdirs()
                versionFile.createNewFile()
            }
            val done = unzipFile(appContext.assets.open("assistant.zip"), rootDir)
            Timber.d("unzipping done")
            return done
        }

        private fun unzipFile(zipFile: InputStream, targetDirectory: File): Boolean {
            ZipInputStream(BufferedInputStream(zipFile)).use { zis ->
                var ze = zis.nextEntry
                var count: Int
                val buffer = ByteArray(8192)
                while (ze != null) {
                    val file = File(targetDirectory, ze.name)
                    Timber.d("unzipping ${file.absoluteFile}")
                    val dir = if (ze.isDirectory) file else file.parentFile
                    if (!dir.isDirectory && !dir.mkdirs())
                        throw FileNotFoundException("Failed to create directory: " + dir.absolutePath)
                    if (ze.isDirectory) {
                        ze = zis.nextEntry
                        continue
                    }
                    FileOutputStream(file).use { fout ->
                        count = zis.read(buffer)
                        while (count != -1) {
                            fout.write(buffer, 0, count)
                            count = zis.read(buffer)
                        }
                    }
                    ze = zis.nextEntry
                }
                return true;
            }
        }
    }
}