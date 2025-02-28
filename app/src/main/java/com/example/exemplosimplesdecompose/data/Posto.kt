package com.example.exemplosimplesdecompose.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Posto(
    val id: String = java.util.UUID.randomUUID().toString(),
    val nome: String,
    val precoGasolina: Double,
    val precoAlcool: Double,
    val coordenadas: Coordenadas
) : Parcelable

//{
//    // Construtor secundário com coordenadas de Fortaleza
//    constructor(nome: String) : this(nome, Coordenadas(41.40338, 2.17403))
//}