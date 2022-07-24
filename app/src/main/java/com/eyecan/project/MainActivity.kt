package com.eyecan.project

import android.Manifest
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.Environment
import android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import com.github.barteksc.pdfviewer.PDFView
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.parser.PdfTextExtractor
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.net.URI
import java.net.URL
import java.util.*


class MainActivity : AppCompatActivity() ,TextToSpeech.OnInitListener{

  lateinit var urifile:URI
    lateinit var string_extracted:String
  lateinit var select:TextView
    lateinit var read:Button
    private var tts: TextToSpeech? = null

    private val LOCAL_STORAGE = "/storage/self/primary/"
    private val REQUEST_EXTERNAL_STORAGE = 1
    private val PERMISSION_STORAGE = arrayOf<String>(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

       select = findViewById(R.id.text_select) as TextView
        read= findViewById(R.id.button1)

        verifystoragepermission(this);
        tts = TextToSpeech(this, this)

        select.setOnClickListener{
            Toast.makeText(this@MainActivity,"selection Clicked",Toast.LENGTH_SHORT).show()
            val ir = Intent(Intent.ACTION_OPEN_DOCUMENT)
            ir.addCategory(Intent.CATEGORY_OPENABLE)
//            ir.setType("application/*")
            ir.setType("text/plain")
            startActivityForResult(ir,100)

            val contentResolver = applicationContext.contentResolver


        }


        read!!.isEnabled = false
        read.setOnClickListener{speakOut()}


    }

    private fun speakOut() {
        val text = select.text
        tts!!.speak(text, TextToSpeech.QUEUE_FLUSH, null,"")
    }

    private fun openpdf() {

//
//        val file = File(Environment.getExternalStorageDirectory(), "Download/TRENDOCEANS.pdf")
//        Log.d("pdfFIle", "" + file)
//
//        val uriPdfPath =
//            FileProvider.getUriForFile(this, applicationContext.packageName + ".provider", file)
//        Log.d("pdfPath", "" + uriPdfPath)
//        val pdfOpenIntent = Intent(Intent.ACTION_VIEW)
//        pdfOpenIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
//        pdfOpenIntent.clipData = ClipData.newRawUri("", uriPdfPath)
//       pdfOpenIntent.setDataAndType(uriPdfPath, "application/pdf")
//        pdfOpenIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
//
//
//        val pdff= findViewById<PDFView>(R.id.web);
//        pdff.fromUri(uriPdfPath).load()





//
//            startActivity(pdfOpenIntent)







//        } catch (activityNotFoundException: ActivityNotFoundException) {
//            Toast.makeText(this, "There is no app to load corresponding PDF", Toast.LENGTH_LONG)
//                .show()
//        }


    }

    private fun verifystoragepermission(activity: MainActivity) {
        val permission = ActivityCompat.checkSelfPermission(activity, WRITE_EXTERNAL_STORAGE);

        // Surrounded with if statement for Android R to get access of complete file.
        if (SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager() && permission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    activity,
                    PERMISSION_STORAGE,
                    REQUEST_EXTERNAL_STORAGE);

                // Abruptly we will ask for permission once the application is launched for sake demo.
                 intent =  Intent();
                intent.setAction(ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                val uri = Uri.fromParts("package", this.getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            }
    }}


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Check which request we're responding to
        if (requestCode == 100) {
            // Make sure the request was successful
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {


                    Log.e("onactivity result" , data?.data.toString())
//                    val webb = findViewById<ImageView>(R.id.web)

//                    webb.setImageURI(data?.data)


                    val uriPdfPath =data?.data;
                    val pdff= findViewById<PDFView>(R.id.web);
//                    pdff.fromUri(uriPdfPath).load()


                    string_extracted = readTextFromUri(uriPdfPath!!)
                    Log.e("onactivity result" ,string_extracted)
                    select.text=string_extracted
                    speak(string_extracted)

                    //                Toast.makeText(this, uri.getPath(), Toast.LENGTH_SHORT).show();




//                    readPdfFile(tt)

                }
            }
        }
    }

    private fun speak(text: String) {



//             fun onInit(status: Int) {
//                if (status == TextToSpeech.SUCCESS) {
//                    val result = tts!!.setLanguage(Locale.US)
//
//                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
//                        Log.e("TTS","The Language not supported!")
//                    } else {
//                        btnSpeak!!.isEnabled = true
//                    }
//                }


    }








    @Throws(IOException::class)
    fun readTextFromUri(uri: Uri): String {
        val stringBuilder = StringBuilder()
        contentResolver.openInputStream(uri)?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                var line: String? = reader.readLine()
                while (line != null) {
                    stringBuilder.append(line)
                    line = reader.readLine()
                }
            }
        }
        return stringBuilder.toString()




}

    override fun onInit( status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts!!.setLanguage(Locale.US)
            Log.e("TTS","The Language is selcted")
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS","The Language not supported!")
            } else {
                read!!.isEnabled = true

            }
        }
    }

}