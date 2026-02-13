package io.diasjakupov.mindtag

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import io.diasjakupov.mindtag.core.database.DatabaseDriverFactory
import io.diasjakupov.mindtag.core.di.initKoin
import io.diasjakupov.mindtag.core.network.TokenStorage
import org.koin.dsl.module

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        initKoin(module {
            single { DatabaseDriverFactory(this@MainActivity) }
            single { TokenStorage(this@MainActivity) }
        })

        setContent {
            App()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
