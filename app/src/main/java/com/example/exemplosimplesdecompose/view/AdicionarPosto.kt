package com.example.exemplosimplesdecompose.view

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.example.exemplosimplesdecompose.R
import com.example.exemplosimplesdecompose.data.Coordenadas
import com.example.exemplosimplesdecompose.data.Posto
import com.example.exemplosimplesdecompose.repos.PostoRepository
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

private fun getLastKnownLocation(
    fusedLocationClient: FusedLocationProviderClient,
    onLocationReceived: (Double, Double) -> Unit
) {
    try {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    onLocationReceived(it.latitude, it.longitude)
                    Log.d("TAG", "Latitude: ${it.latitude}, Longitude: ${it.longitude}")
                } ?: Log.e("TAG", "Localização nula")
            }
            .addOnFailureListener { e ->
                Log.e("TAG", "Erro ao obter localização", e)
            }
    } catch (e: SecurityException) {
        Log.e("TAG", "Permissão de localização não concedida", e)
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun AdicionarPosto(navController: NavHostController, posto: Posto? = null) {
    val context = LocalContext.current
    val repository = remember { PostoRepository(context) }

    var nome by remember { mutableStateOf("") }
    var alcool by remember { mutableStateOf("") }
    var gasolina by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }
    var permissaoConcedida by remember { mutableStateOf(false) }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val estadoPermissao = rememberPermissionState(permission = Manifest.permission.ACCESS_FINE_LOCATION)

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            getLastKnownLocation(fusedLocationClient) { lat, lon ->
                latitude = lat
                longitude = lon
            }
        }
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getLastKnownLocation(fusedLocationClient) { lat, lon ->
                latitude = lat
                longitude = lon
            }
        } else {
            launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (posto == null) context.getString(R.string.adicionar_posto) else context.getString(R.string.editar_posto)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, "Voltar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = nome,
                onValueChange = { nome = it },
                label = { Text(context.getString(R.string.nome_posto)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = alcool,
                onValueChange = { alcool = it },
                label = { Text(context.getString(R.string.preco_alcool)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )

            OutlinedTextField(
                value = gasolina,
                onValueChange = { gasolina = it },
                label = { Text(context.getString(R.string.preco_gasolina)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )

            if (latitude != null && longitude != null) {
                Text("Latitude: $latitude")
                Text("Longitude: $longitude")
            }

            errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Botão de salvar
            Button(
                onClick = {
                    if (nome.isBlank()) {
                        errorMessage = "Por favor, insira o nome do posto."
                        return@Button
                    }

                    val alcoholPrice = alcool.replace(",", ".").toDoubleOrNull()
                    val gasPrice = gasolina.replace(",", ".").toDoubleOrNull()

                    if (alcoholPrice == null || alcoholPrice <= 0) {
                        errorMessage = "Por favor, insira um preço válido para o álcool."
                        return@Button
                    }

                    if (gasPrice == null || gasPrice <= 0) {
                        errorMessage = "Por favor, insira um preço válido para a gasolina."
                        return@Button
                    }

                    if (latitude == null || longitude == null) {
                        errorMessage = "Não foi possível obter a localização."
                        return@Button
                    }

                    if (posto == null) {
                        // Criar um novo posto
                        val novoPosto = Posto(
                            nome = nome,
                            precoAlcool = alcoholPrice,
                            precoGasolina = gasPrice,
                            coordenadas = Coordenadas(latitude!!, longitude!!)
                        )
                        repository.saveGasStation(novoPosto)
                        repository.setSelectedStationId(novoPosto.id)
                    } else {
                        // Atualizar posto existente
                        val postoAtualizado = posto.copy(
                            nome = nome,
                            precoAlcool = alcoholPrice,
                            precoGasolina = gasPrice
                        )
                        repository.updateGasStation(postoAtualizado)
                    }

                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.Save, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(context.getString(R.string.salvar_posto))
            }
        }
    }

    if (!permissaoConcedida && !estadoPermissao.status.isGranted) {
        LaunchedEffect(Unit) {
            estadoPermissao.launchPermissionRequest()
        }
    }
}