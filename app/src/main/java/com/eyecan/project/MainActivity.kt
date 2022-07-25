package com.eyecan.project

import android.Manifest
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.Environment
import android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
import android.speech.tts.TextToSpeech
import android.text.Html
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import nl.siegmann.epublib.domain.Book
import nl.siegmann.epublib.domain.Resource
import nl.siegmann.epublib.domain.Spine
import nl.siegmann.epublib.epub.EpubReader
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() ,TextToSpeech.OnInitListener{


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
            ir.setType("application/*")
//                ir.setType("text/plain")
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


                    val uri_book =data?.data;




                    string_extracted = readTextFromUri(uri_book!!)
                    Log.e("onactivity result" ,string_extracted)
                    select.text=string_extracted






//                    readPdfFile(tt)

                }
            }
        }
    }




    @Throws(IOException::class)
    fun readTextFromUri(uri: Uri): String {
        val stringBuilder = StringBuilder()
       lateinit var sff:String
        contentResolver.openInputStream(uri)?.use { inputStream ->
//            BufferedReader(InputStreamReader(inputStream)).use { reader ->
//                var line: String? = reader.readLine()
//                while (line != null) {
//                    stringBuilder.append(line)
//                   line = reader.readLine()
//                }
//            }}
//
//        return stringBuilder.toString()


            val book = EpubReader().readEpub(inputStream)


            stringBuilder.append(book.title)

           stringBuilder.append(" by"+ book.metadata.authors)
            val bookstring = listofArray(book)
            val s:String= bookstring.get(2)
stringBuilder.append(s)


           }


//            while (line != null) {
//                stringBuilder.append(line)
//                line = reader.readLine()
//            }

    return stringBuilder.toString()
    }



    fun listofArray(book: Book):ArrayList<String>{

        val spine: Spine = book.spine
        var res: Resource
        val spineList = spine.spineReferences
        val listOfPages: ArrayList<String> = ArrayList()
        val count = spineList.size
        var start = 0
        val string = StringBuilder()
        res = spine.getResource(0);
        while ( start < count) {

            res = spine.getResource(start);
//            val iss: InputStream = res.inputStream
            val reader = BufferedReader(InputStreamReader(res.inputStream))
            var line: String? = reader.readLine()
            while(line!=null){
            if (line!!.contains("<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"no\"?>")) {
                string.delete(0, string.length);
            }
            string.append(Html.fromHtml(formatLine(line)));

            if (line.contains("</html>")) {
                listOfPages.add(string.toString());
            }
                line= reader.readLine()
            }

            start++
        }

        return listOfPages;
    }

    fun formatLine (  line : String):String{

         var temps:String=line

        if (line.contains("http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd")) {
            temps = line.substring(line.indexOf(">") + 1, line.length);
        }
        if ((line.contains("{") && line.contains("}"))
            || ((line.contains("/*")) && line.contains("*/"))
            || (line.contains("<!--") && line.contains("-->"))) {
            temps = line.substring(line.length);
        }

  return temps  }


//        val stringBuilder = StringBuilder()
//        contentResolver.openInputStream(uri)?.use { inputStream ->
//            val zis = ZipInputStream(contentResolver.openInputStream(uri))
//            var entry = zis.nextEntry
//
//            zis.read( bd:byte[],  off:int, int len)
//            val out: java.lang.StringBuilder = getTxtFiles()






    override fun onInit( status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts!!.setLanguage(Locale.UK)
            Log.e("TTS","The Language is selcted")
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS","The Language not supported!")
            } else {
                read!!.isEnabled = true

            }
        }
    }

}