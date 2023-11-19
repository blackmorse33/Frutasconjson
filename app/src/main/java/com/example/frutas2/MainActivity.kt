package com.example.frutas2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.frutas2.ui.theme.Frutas2Theme
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Frutas2Theme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ListaDeFrutas()
                }
            }
        }
    }
}

data class Nutritions(
    val calories: Double,
    val fat: Double,
    val sugar: Double,
    val carbohydrates: Double,
    val protein: Double
)

data class FruitItem(
    val name: String,
    val id: Int,
    val family: String,
    val order: String,
    val genus: String,
    val nutritions: Nutritions
)

interface ApiService {
    @GET("api/fruit/all")
    suspend fun obtenerDatos(): List<FruitItem>
}

val retrofit = Retrofit.Builder()
    .baseUrl("https://www.fruityvice.com/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()

val servicio = retrofit.create(ApiService::class.java)

@Composable
fun ListaDeFrutas() {
    var frutas by remember { mutableStateOf<List<FruitItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(true) {
        try {
            val response = servicio.obtenerDatos()
            frutas = response
        } catch (e: Exception) {
            // Manejar errores
        } finally {
            isLoading = false
        }
    }

    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        SearchBar(searchQuery = searchQuery) {
            searchQuery = it
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(50.dp)
                    .align(Alignment.CenterHorizontally)
            )
        } else if (frutas.isNotEmpty()) {
            LazyColumn {
                items(frutas.filter { it.name.contains(searchQuery, ignoreCase = true) }) { fruta ->
                    FrutaItem(fruta = fruta)
                    Divider()
                }
            }
        } else {
            Text(
                text = "Error al cargar los datos",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SearchBar(
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit
) {
    var isSearching by remember { mutableStateOf(false) }
    var keyboardController by remember { mutableStateOf<SoftwareKeyboardController?>(null) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BasicTextField(
            value = searchQuery,
            onValueChange = {
                onSearchQueryChanged(it)
                isSearching = it.isNotEmpty()
            },
            textStyle = LocalTextStyle.current.copy(color = Color.Black),
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(8.dp)
                .clip(MaterialTheme.shapes.medium)
                .onGloballyPositioned {
                    val fieldWidth = it.size.width / 7
                    if (fieldWidth > 600) {
                        keyboardController?.show()
                    } else {
                        keyboardController?.hide()
                    }
                }
        )
        IconButton(
            onClick = {
                isSearching = !isSearching
                if (!isSearching) {
                    onSearchQueryChanged("")
                    keyboardController?.hide()
                } else {
                    keyboardController?.show()
                }
            }
        ) {
            Icon(
                imageVector = if (isSearching) Icons.Default.Clear else Icons.Default.Search,
                contentDescription = "Search"
            )
        }
    }

}

@Composable
fun FrutaItem(fruta: FruitItem) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(text = "Nombre: ${fruta.name}", fontWeight = FontWeight.Bold)
        Text(text = "Familia: ${fruta.family}")
        Text(text = "Orden: ${fruta.order}")
        Text(text = "Género: ${fruta.genus}")
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Información nutricional:")
        Text(text = "Calorías: ${fruta.nutritions.calories}")
        Text(text = "Grasa: ${fruta.nutritions.fat}")
        Text(text = "Azúcar: ${fruta.nutritions.sugar}")
        Text(text = "Carbohidratos: ${fruta.nutritions.carbohydrates}")
        Text(text = "Proteína: ${fruta.nutritions.protein}")
    }
}

@Preview
@Composable
fun ListaDeFrutasPreview() {
    Frutas2Theme {
        ListaDeFrutas()
    }
}
