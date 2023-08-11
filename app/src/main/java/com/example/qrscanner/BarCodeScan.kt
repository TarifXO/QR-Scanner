package com.example.qrscanner

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.SurfaceHolder
import android.widget.Toast
import com.example.qrscanner.databinding.ActivityBarCodeScanBinding
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL


class BarCodeScan : AppCompatActivity() {

    private lateinit var binding: ActivityBarCodeScanBinding
    private lateinit var barcodeDetector: BarcodeDetector
    private lateinit var cameraSource: CameraSource
    var intentData = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBarCodeScanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnAction.setOnClickListener {
            if (intentData.isNotEmpty() && isUrl(intentData)) {
                val websiteUrl = intentData
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(websiteUrl))
                startActivity(intent)
            }else {
                Toast.makeText(this, "No Website Found!", Toast.LENGTH_LONG).show()
            }
    }
}

    private fun initBarcode(){
        barcodeDetector = BarcodeDetector.Builder(this)
            .setBarcodeFormats(Barcode.ALL_FORMATS).build()
        cameraSource = CameraSource.Builder(this, barcodeDetector)
            .setRequestedPreviewSize(1920, 1080)
            .setAutoFocusEnabled(true)
            .build()
        binding.surfaceview.holder.addCallback(object : SurfaceHolder.Callback{
            @SuppressLint("MissingPermission")
            override fun surfaceCreated(holder: SurfaceHolder) {
                try {
                    cameraSource.start(binding.surfaceview.holder)
                }catch (e:IOException){
                    e.printStackTrace()
                }
            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
                // not needed
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                cameraSource.stop()
            }
        })

        barcodeDetector.setProcessor(object : Detector.Processor<Barcode>{
            override fun release() {
                Toast.makeText(applicationContext, "Scanner Stopped!", Toast.LENGTH_SHORT).show()
            }

            override fun receiveDetections(detections: Detector.Detections<Barcode>) {
                val barcodes = detections.detectedItems
                if (barcodes.size() != 0){
                    intentData = barcodes.valueAt(0).displayValue
                    binding.BarCodeValue.post{
                        binding.BarCodeValue.text = intentData
                    }
                }
            }
        })
    }

    private fun isUrl(text: String): Boolean {
        return try {
            URL(text)
            true
        } catch (e: MalformedURLException) {
            false
        }
    }

    override fun onPause(){
        super.onPause()
        cameraSource.release()
    }

    override fun onResume(){
        super.onResume()
        initBarcode()
    }
}