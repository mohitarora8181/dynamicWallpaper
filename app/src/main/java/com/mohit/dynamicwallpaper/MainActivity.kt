package com.mohit.dynamicwallpaper

import android.app.WallpaperManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import coil.compose.AsyncImage
import com.mohit.dynamicwallpaper.ui.theme.DynamicWallpaperTheme
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.net.URL
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DynamicWallpaperTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Home(modifier = Modifier.padding(innerPadding))
                    val periodicWork = PeriodicWorkRequestBuilder<Worker>(10,TimeUnit.SECONDS).build()
                    WorkManager.getInstance().enqueueUniquePeriodicWork(
                        "123456789098765432",
                        ExistingPeriodicWorkPolicy.KEEP,
                        periodicWork
                    )
                }
            }
        }
    }
}

@Composable
fun Home(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val client = HttpClient(CIO){
        install(ContentNegotiation){
            json(Json {
                ignoreUnknownKeys=true
                isLenient=true
            })
        }
    }

    var imageUrl by remember {
        mutableStateOf("")
    }

    suspend fun getRandomImage():List<UnsplashApiSchema>{
        val clientID = "xtW_knyL7_P0HlkCKaCFXMyNIzHvp8qhkJn1JTJHhlQ"
        try {
            val data:HttpResponse = client.get("https://api.unsplash.com/photos/random?count=1&orientation=portrait&client_id=$clientID")
            imageUrl = data.body<List<UnsplashApiSchema>>()[0].urls.raw
            return data.body()
        }catch(e:Exception){
            e.printStackTrace()
            return emptyList()
        }
    }

    var trigger by remember {
        mutableIntStateOf(0)
    }

    var isLoading by remember {
        mutableStateOf(true)
    }

    val data = produceState<List<UnsplashApiSchema>>(initialValue = emptyList(),trigger) {
        value =  getRandomImage()
    }
    if(data.value.isNotEmpty()){
        AsyncImage(
            modifier = modifier
                .fillMaxWidth()
                .padding(10.dp),
            model = imageUrl,
            contentDescription = null,
            onLoading = {
                isLoading = true
            },
            onSuccess = {
                isLoading = false
            }
        )
    }
    if(isLoading){
        Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier.fillMaxSize()) {
            CircularProgressIndicator(modifier = Modifier.size(50.dp))
        }
    }
    Row (modifier = modifier
        .fillMaxSize()
        .padding(bottom = 20.dp), horizontalArrangement = Arrangement.SpaceAround, verticalAlignment = Alignment.Bottom){
        Button(onClick = { trigger++ }) {
            Text(text = "Get Random Image")
        }
        Button(onClick = {
            val wallpaperManager = WallpaperManager.getInstance(context)
            try{
                coroutineScope.launch {
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
        }) {
            Text(text = "Set Wallpaper")
        }
    }
}