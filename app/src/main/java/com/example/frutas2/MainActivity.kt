package com.example.frutas2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

enum class SortingOrder {
    CALORIES,
    FAT,
    SUGAR,
    CARBOHYDRATES,
    PROTEIN
}



@Composable
fun ListaDeFrutas() {
    var currentSortingOrder by remember { mutableStateOf(SortingOrder.CALORIES) }
    var frutas by remember { mutableStateOf<List<FruitItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }


    LaunchedEffect(true) {
        try {
            val response = servicio.obtenerDatos()

            frutas = when (currentSortingOrder) {
                SortingOrder.CALORIES -> response.sortedByDescending { it.nutritions.calories }
                SortingOrder.FAT -> response.sortedByDescending { it.nutritions.fat }
                SortingOrder.SUGAR -> response.sortedByDescending { it.nutritions.sugar }
                SortingOrder.CARBOHYDRATES -> response.sortedByDescending { it.nutritions.carbohydrates }
                SortingOrder.PROTEIN -> response.sortedByDescending { it.nutritions.protein }
            }
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
        AppContent()

        SearchBar(searchQuery = searchQuery) {
            searchQuery = it
        }
        Spacer(modifier = Modifier.height(30.dp))
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(70.dp)
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
        IconButton(
            onClick = {
                isSearching = !isSearching
                if (isSearching) {
                    onSearchQueryChanged("")
                    keyboardController?.show()
                } else {
                    keyboardController?.hide()
                }
            }
        ) {
            Icon(
                imageVector = if (isSearching) Icons.Default.Close else Icons.Default.Search,
                contentDescription = "Search",
                modifier = Modifier.size(24.dp)
            )
        }

        if (isSearching) {
            BasicTextField(
                value = searchQuery,
                onValueChange = {
                    onSearchQueryChanged(it)
                },
                textStyle = LocalTextStyle.current.copy(Color.Green,fontSize = 16.sp),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(8.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .onGloballyPositioned {
                        val fieldWidth = it.size.width / 7
                        if (fieldWidth > 700) {
                            keyboardController?.show()
                        } else {
                            keyboardController?.hide()
                        }
                    }
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


@Composable
fun AppContent(/**
    fruits: List<FruitItem>,
    onSortByCalories: (SortingOrder) -> Unit,
    onSortByFat: (SortingOrder) -> Unit,
    onSortBySugar: (SortingOrder) -> Unit,
    onSortByCarbohydrates: (SortingOrder) -> Unit,
    onSortByProtein: (SortingOrder) -> Unit**/

) {
    var expanded by remember { mutableStateOf(false) }
    var currentSortingOrder by remember { mutableStateOf(SortingOrder.CALORIES) }

    Column {
        Row {
            IconButton(onClick = { expanded = !expanded }) {
                Icon(imageVector = Icons.Default.Menu, contentDescription = null)
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            androidx.compose.material.DropdownMenuItem(onClick = { currentSortingOrder = SortingOrder.CALORIES }) {
                Text(text = "Calorias")

            }
            androidx.compose.material.DropdownMenuItem(onClick = { currentSortingOrder = SortingOrder.FAT }) {
                Text(text = "Grasa")

            }
            androidx.compose.material.DropdownMenuItem(onClick = { currentSortingOrder = SortingOrder.SUGAR }) {
                Text(text = "Azucar")

            }
            androidx.compose.material.DropdownMenuItem(onClick = { currentSortingOrder = SortingOrder.CARBOHYDRATES }) {
                Text(text = "Carbohidratos")

            }
            androidx.compose.material.DropdownMenuItem(onClick = { currentSortingOrder = SortingOrder.PROTEIN }) {
                Text(text = "Proteina")

            }
            SortingOrder.values().forEach { order ->
                androidx.compose.material.DropdownMenuItem(onClick = {
                    currentSortingOrder = order
                    expanded = false
                }) {
                    Text(text = order.name.lowercase().capitalize())
                }
            }

        }
    }
}



/**
@Composable
fun AppContent2(List<FruitItem>) {
    // State variables and logic for sorting
    var sortByCalories by remember { mutableStateOf(SortingOrder.ASCENDING) }
    var sortByFat by remember { mutableStateOf(SortingOrder.ASCENDING) }
    var sortBySugar by remember { mutableStateOf(SortingOrder.ASCENDING) }
    var sortByCarbohydrates by remember { mutableStateOf(SortingOrder.ASCENDING) }
    var sortByProtein by remember { mutableStateOf(SortingOrder.ASCENDING) }

    val sortedFruits = remember {
        fruits.sortedWith(compareByDescending<FruitItem> {
            when (it) {
                SortingOrder.ASCENDING -> 0.0
                SortingOrder.DESCENDING -> 1.0
                else -> {0}
            }
        }.thenByDescending {
            it.nutritions.calories
        })
    }

    FruitList(
        fruits = sortedFruits,
        onSortByCalories = {
            sortByCalories = it
        },
        onSortByFat = {
            sortByFat = it
        },
        onSortBySugar = {
            sortBySugar = it
        },
        onSortByCarbohydrates = {
            sortByCarbohydrates = it
        },
        onSortByProtein = {
            sortByProtein = it
        }
    )
}
**/