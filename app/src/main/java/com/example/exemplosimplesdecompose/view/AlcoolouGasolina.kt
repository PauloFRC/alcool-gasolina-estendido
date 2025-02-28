package com.example.exemplosimplesdecompose.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.exemplosimplesdecompose.R
import com.example.exemplosimplesdecompose.data.Coordenadas
import com.example.exemplosimplesdecompose.data.Posto
import com.example.exemplosimplesdecompose.repos.PostoRepository
import kotlin.math.roundToInt

@Composable
fun AlcoolGasolinaPreco(navController: NavHostController) {
    val context = LocalContext.current
    val repository = remember { PostoRepository(context) }

    val postoSelecionado = remember { mutableStateOf(repository.getSelectedStation()) }
    var checkedState by remember { mutableStateOf(repository.getSwitchState()) }

    var alcool by remember { mutableStateOf(postoSelecionado.value?.precoAlcool?.toString() ?: "") }
    var gasolina by remember { mutableStateOf(postoSelecionado.value?.precoGasolina?.toString() ?: "") }
    var nomeDoPosto by remember { mutableStateOf(postoSelecionado.value?.nome ?: "") }
    var latitude by remember { mutableDoubleStateOf(postoSelecionado.value?.coordenadas?.latitude ?: 0.0) }
    var longitude by remember { mutableDoubleStateOf(postoSelecionado.value?.coordenadas?.longitude ?: 0.0) }

    var resultado by remember { mutableStateOf(context.getString(R.string.vamos_calcular)) }

    LaunchedEffect(postoSelecionado.value) {
        postoSelecionado.value?.let { station ->
            alcool = station.precoAlcool.toString()
            gasolina = station.precoGasolina.toString()
            nomeDoPosto = station.nome
            latitude = station.coordenadas.latitude
            longitude = station.coordenadas.longitude
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            postoSelecionado.value?.let {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Posto selecionado: ${it.nome}",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Álcool: R$ ${it.precoAlcool}")
                            Text("Gasolina: R$ ${it.precoGasolina}")
                        }
                    }
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = alcool,
                    onValueChange = { alcool = it },
                    label = { Text(context.getString(R.string.preco_alcool)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                OutlinedTextField(
                    value = gasolina,
                    onValueChange = { gasolina = it },
                    label = { Text(context.getString(R.string.preco_gasolina)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                OutlinedTextField(
                    value = nomeDoPosto,
                    onValueChange = { nomeDoPosto = it },
                    label = { Text(context.getString(R.string.nome_posto)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                )

                Text(
                    text = "Latitude: $latitude",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Longitude: $longitude",
                    style = MaterialTheme.typography.bodyMedium
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = context.getString(R.string.rend_75),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        modifier = Modifier.semantics { contentDescription = "Switch rendimento" },
                        checked = checkedState,
                        onCheckedChange = { newState ->
                            checkedState = newState
                            repository.setSwitchState(newState)
                        },
                        thumbContent = {
                            if (checkedState) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(SwitchDefaults.IconSize),
                                )
                            }
                        }
                    )
                }

                Button(
                    onClick = {
                        try {
                            val precoAlcool = alcool.toDoubleOrNull() ?: 0.0
                            val precoGasolina = gasolina.toDoubleOrNull() ?: 0.0

                            if (precoAlcool > 0 && precoGasolina > 0) {
                                val ratio = precoAlcool / precoGasolina
                                val threshold = if (checkedState) 0.75 else 1.0

                                resultado = if (ratio <= threshold) {
                                    context.getString(R.string.alcool_vantajoso, (ratio * 100).roundToInt() / 100.0)
                                } else {
                                    context.getString(R.string.galosina_vantajosa, (ratio * 100).roundToInt() / 100.0)
                                }

                                if (nomeDoPosto.isNotBlank()) {
                                    val station = Posto(
                                        id = postoSelecionado.value?.id ?: java.util.UUID.randomUUID().toString(),
                                        nome = nomeDoPosto,
                                        precoGasolina = precoGasolina,
                                        precoAlcool = precoAlcool,
                                        coordenadas = Coordenadas(10.0, 10.0)
                                    )
                                    repository.saveGasStation(station)
                                    repository.setSelectedStationId(station.id)
                                    postoSelecionado.value = station
                                }
                            } else {
                                resultado = context.getString(R.string.insira_valores)
                            }
                        } catch (e: Exception) {
                            resultado = context.getString(R.string.erro_calcular)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(context.getString(R.string.calcular))
                }

                Text(
                    text = resultado,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                FloatingActionButton(
                    onClick = { navController.navigate("ListaDePostos") },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Icon(Icons.Filled.List, "Listar Postos")
                }

                FloatingActionButton(
                    onClick = { navController.navigate("AdicionarPosto") }
                ) {
                    Icon(Icons.Filled.Add, "Adicionar Posto")
                }
            }
        }
    }
}

