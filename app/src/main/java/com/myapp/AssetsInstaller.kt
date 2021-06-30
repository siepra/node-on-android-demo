package com.myapp

import android.content.Context
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

class AssetsInstaller(private val context: Context, private val assetPath: String) {

    fun installAssets(assetInstallDirectory: File) {
        // Prepare installation folder
        assetInstallDirectory.mkdirs()

        val apk = ZipFile(context.applicationInfo.sourceDir)

        apk.entries()
            .toList()
            .filter {
                // Get all entries from matching directory
                val dirs = it.name.split("/")
                val path = assetPath.split("/")

                var match = true
                path.mapIndexed { i, p ->
                    try {
                        if(p != dirs[i]) match = false
                    } catch (e: IndexOutOfBoundsException) {
                        // Entry path is shorter than desired file location
                        match = false
                    }

                }
                match
            }
            .map {
                // Copy file to accessible destination
                val entry = apk.getEntry(it.name)
                    ?: throw Exception("Unable to find file in apk:${it.name}")

                val tempFile = File.createTempFile("tempFile", "zip")
                val tempOut = FileOutputStream(tempFile)

                IOUtils.copy(
                    apk.getInputStream(entry),
                    tempOut
                )

                val archive = ZipFile(tempFile)

                archive.use { archive ->
                    archive.entries()
                        .toList()
                        .map { childEntry ->
                            val target = File(assetInstallDirectory, childEntry.name)

                            if(!target.exists()) {
                                // Keep original archive hierarchy
                                target.parentFile?.mkdirs()

                                if(!childEntry.isDirectory) {
                                    writeEntryFile(
                                        archive = archive,
                                        childEntry = childEntry,
                                        target = target
                                    )
                                }
                            }
                        }
                }
            }
    }

    private fun writeEntryFile(archive: ZipFile, childEntry: ZipEntry, target: File) {
        val stream = archive.getInputStream(childEntry)
        val out: OutputStream = FileOutputStream(target)
        val buf = ByteArray(4096)
        var len: Int

        while (stream.read(buf).also { len = it } > 0) {
            Thread.yield()
            out.write(buf, 0, len)
        }

        out.close()
        target.setReadable(true)
        stream?.close()
    }

    fun setFilePermissions(file: File) {
        file.setReadable(true)
        // file.setExecutable(true)
        // file.setWritable(true)
    }

}