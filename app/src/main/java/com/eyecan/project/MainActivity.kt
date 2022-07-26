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
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
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
     var currentchap:Int=1
     lateinit var select:TextView
    lateinit var chapter_number:TextView
    lateinit var next:Button
     lateinit var read:Button
    lateinit var pause_button:Button

     lateinit var bookchapters:ArrayList<String>
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
        supportActionBar!!.hide()

        select = findViewById(R.id.text_select) as TextView
        chapter_number= findViewById(R.id.text_chapter)
        read= findViewById(R.id.button1)
        pause_button= findViewById(R.id.pause_but)
        verifystoragepermission(this);
        tts = TextToSpeech(this, this)
         currentchap= 1


        chapter_number.visibility= View.INVISIBLE

        next= findViewById<Button>(R.id.nextbutton)


//     setting button can be clicked or not
        next.isEnabled=false
        pause_button.isEnabled=false
        read.isEnabled = false




        read.setOnClickListener {
            pause_button.isEnabled=true
            speakOut()
        }


        pause_button.setOnClickListener {

            if (tts != null) {
                if(tts!!.isSpeaking) {
                   pause_button.isEnabled=false
                    tts!!.stop()
                }
            }
        }


        select.setOnClickListener{
            Toast.makeText(this@MainActivity,"Select Book",Toast.LENGTH_SHORT).show()
            val ir = Intent(Intent.ACTION_OPEN_DOCUMENT)
            ir.addCategory(Intent.CATEGORY_OPENABLE)
            ir.setType("application/epub+zip")
            startActivityForResult(ir,100)
        }


        next.setOnClickListener {
            chapter_number.isVisible=true
            if(currentchap<bookchapters.size){
            chapter_number.text= "Chapter "+ currentchap.toString()+"/"+(bookchapters.size-1)
            select.text=""
            val chaptertext=  bookchapters.get(currentchap)
            select.text=chaptertext
            currentchap++
        }
        }

    }

    private fun speakOut() {

        val text = select.text
        tts!!.stop()
        tts!!.speak(text, TextToSpeech.QUEUE_FLUSH, null,"")
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
                    val uri_book =data?.data;

                    string_extracted = readTextFromUri(uri_book!!)
                    Log.e("onactivity result" ,string_extracted)
                    select.text=string_extracted
                    chapter_number.text=" "
                    next.isEnabled=true
                    currentchap=1

                }
            }
        }
    }




    @Throws(IOException::class)
    fun readTextFromUri(uri: Uri): String {
        val stringBuilder = StringBuilder()
       lateinit var sff:String
        contentResolver.openInputStream(uri)?.use { inputStream ->

            val book = EpubReader().readEpub(inputStream)

            stringBuilder.append(book.title)

           stringBuilder.append(" by"+ book.metadata.authors)
           bookchapters = ChaptersArray(book)
            val s:String= bookchapters.get(0)
            stringBuilder.append(s)


           }

    return stringBuilder.toString()
    }


//   Get String Arraylist with Chapters text
      fun ChaptersArray(book: Book):ArrayList<String>{

        val spine: Spine = book.spine
        var res: Resource
        val spineList = spine.spineReferences
        val listOfPages: ArrayList<String> = ArrayList()
        val count = spineList.size
        var start = 0

        res = spine.getResource(0);
        while ( start < count) {
            val string = StringBuilder()
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

// Formatting html tags
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

  return temps
    }




// intializing text to speech
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


    //    stopping text to speech if activity is paused
    override fun onPause() {
        if (tts != null) {
            tts!!.stop()
        }
        super.onPause()
    }

    public override fun onDestroy() {
        // Shutdown TTS when
        // activity is destroyed
        if (tts != null) {
            tts!!.stop()
            tts!!.shutdown()
        }
        super.onDestroy()
    }
}