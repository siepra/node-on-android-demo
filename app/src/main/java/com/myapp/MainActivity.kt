package com.myapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import java.io.File

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val libs = prepareAsset("libs")
        val nodeApp = prepareAsset("nodeapp")

        val service = Intent(this, NodeService::class.java)
        service.putExtra(INTENT_EXTRAS_LIBRARY_PATH, libs)
        service.putExtra(INTENT_EXTRAS_NODE_APP_PATH, nodeApp)
        startService(service)
    }

    private fun prepareAsset(asset: String): String {
        val assetsInstaller = AssetsInstaller(
            context = this,
            assetPath = "assets/arm64-v8a/$asset.zip"
        )

        val file = File(filesDir, asset)
        assetsInstaller.installAssets(file)

        return file.absolutePath
    }

}
