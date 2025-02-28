package com.example.exemplosimplesdecompose.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.exemplosimplesdecompose.R
import com.example.exemplosimplesdecompose.repos.PostoRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaDePostos(navController: NavHostController) {
    val context = LocalContext.current
    val repository = remember { PostoRepository(context) }

    var postos by remember { mutableStateOf(repository.getAllGasStations()) }

    val postoSelecionado = remember { mutableStateOf(repository.getSelectedStationId()) }

    fun refreshStations() {
        postos = repository.getAllGasStations()
        postoSelecionado.value = repository.getSelectedStationId()
    }

    LaunchedEffect(Unit) {
        refreshStations()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(context.getString(R.string.titulo)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, "Voltar")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (postos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Nenhum posto cadastrado.\nAdicione um posto na tela principal.",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(postos) { posto ->
                    val isSelected = posto.id == postoSelecionado.value

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                repository.setSelectedStationId(posto.id)
                                postoSelecionado.value = posto.id
                                navController.popBackStack()
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp)
                            ) {
                                Text(
                                    text = posto.nome,
                                    style = MaterialTheme.typography.titleMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = context.getString(R.string.alcool, posto.precoAlcool),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = context.getString(R.string.gasolina, posto.precoGasolina),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Latitude: ${posto.coordenadas.latitude}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Longitude: ${posto.coordenadas.longitude}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }

                            // Editar
                            IconButton(
                                onClick = { navController.navigate("editar_posto/${posto.id}") }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Editar posto",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            // Delete button
                            IconButton(
                                onClick = {
                                    repository.deleteGasStation(posto.id)
                                    refreshStations()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Excluir posto",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }

                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                        }
                    }
                }
            }
        }
    }
}