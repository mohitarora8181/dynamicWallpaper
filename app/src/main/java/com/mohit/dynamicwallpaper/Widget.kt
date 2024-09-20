package com.mohit.dynamicwallpaper

import android.app.WallpaperManager
import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.glance.Button
import androidx.glance.ButtonColors
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
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

object Widget :GlanceAppWidget() {

    @Composable
    override fun Content() {
        Column (modifier = GlanceModifier
            .fillMaxSize()
            .background(Color.Black),
            verticalAlignment = Alignment.Vertical.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                text = "Change Wallpaper",
                onClick = actionRunCallback(ChangeWallpaper::class.java),
                modifier = GlanceModifier
                    .fillMaxWidth()
                ,
                colors = ButtonColors(
                    backgroundColor = ColorProvider(Color.Black),
                    contentColor = ColorProvider(Color.White)
                ),
                style = TextStyle(
                    fontWeight = FontWeight.Medium,
                    color = ColorProvider(Color.White),
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
            )
        }
    }
}

class WidgetReceiver:GlanceAppWidgetReceiver(){
    override val glanceAppWidget: GlanceAppWidget
        get() = Widget
}

class ChangeWallpaper:ActionCallback{
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val scope = CoroutineScope(Dispatchers.IO)
        val client = HttpClient(CIO){
            install(ContentNegotiation){
                json(Json {
                    ignoreUnknownKeys=true
                    isLenient=true
                })
            }
        }

        var imageUrl = ""

        fun setWallpaper(imageUrl: String,context: Context){
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

        suspend fun getRandomImage(context: Context) {
            val clientID = "D1AkKB3tOoieW8xl11h12D3uMSnGmeVaMTsCgoJ9qcM"
            try {
                val data: HttpResponse =
                    client.get("https://api.unsplash.com/photos/random?count=1&orientation=portrait&client_id=$clientID")
                imageUrl = data.body<List<UnsplashApiSchema>>()[0].urls.raw
                setWallpaper(imageUrl, context = context);
                println("From Widget")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        getRandomImage(context)
    }
}