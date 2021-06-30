package com.myapp

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.concurrent.Executors

class NodeService: Service() {

    private val executor = Executors.newCachedThreadPool()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        executor.execute(Runner(intent))
        return START_NOT_STICKY
    }

    private fun getOutput(process: Process): Int {
        val out = BufferedReader(InputStreamReader(process.inputStream))

        var output = ""
        while(out.readLine()?.also { output = it } != null) {
            Log.d("NODE", output)
        }

        process.waitFor()
        return process.exitValue()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    inner class Runner(private val intent: Intent?): Runnable {
        override fun run() {
            val libraryPath = intent?.getStringExtra(INTENT_EXTRAS_LIBRARY_PATH)
            val nodeAppPath = intent?.getStringExtra(INTENT_EXTRAS_NODE_APP_PATH)

            val command = arrayOf(
                "./libnode.so",
                "$nodeAppPath/index.js"
            )

            val processBuilder = ProcessBuilder(*command)
            processBuilder.environment()["LD_LIBRARY_PATH"] = libraryPath

            val nativeLibraryDir = File(applicationContext.applicationInfo.nativeLibraryDir)

            val process = processBuilder
                .directory(nativeLibraryDir)
                .start()

            getOutput(process)
        }
    }

}
