package com.mohit.dynamicwallpaper

import android.app.WallpaperManager
import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.ui.platform.LocalContext
import androidx.work.Worker
import androidx.work.WorkerParameters
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.net.URL

class Worker(context:Context , params:WorkerParameters):Worker(context,params) {
    val client = HttpClient(CIO){
        install(ContentNegotiation){
            json(Json {
                ignoreUnknownKeys=true
                isLenient=true
            })
        }
    }

    private val scope = CoroutineScope(Dispatchers.IO)
    private val localcontext = context

    override fun doWork(): Result{
        Log.d("worker","working")
        var imageUrl = ""
        suspend fun getRandomImage(){
            val clientID = "D1AkKB3tOoieW8xl11h12D3uMSnGmeVaMTsCgoJ9qcM"
            try {
                val data:HttpResponse = client.get("https://api.unsplash.com/photos/random?count=1&orientation=portrait&client_id=$clientID")
                imageUrl = data.body<List<UnsplashApiSchema>>()[0].urls.raw
                setWallpaper(imageUrl, context = localcontext);
            }catch(e:Exception){
                e.printStackTrace()
            }
        }
        scope.launch {
            getRandomImage()
        }
        return Result.success()
    }

    private fun setWallpaper(imageUrl: String,context: Context){
        val wallpaperManager = WallpaperManager.getInstance(context)
        try{
            scope.launch {
                val task = async(Dispatchers.IO) {
                    BitmapFactory.decodeStream(
                        URL(imageUrl).openConnection().getInputStream()
                    )
                }
                val bitmap = task.await()
                wallpaperManager.setBitmap(bitmap)
            }
        }catch(e:Exception){
            e.printStackTrace()
        }
    }
}