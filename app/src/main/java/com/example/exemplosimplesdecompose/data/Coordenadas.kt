package com.example.exemplosimplesdecompose.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Coordenadas(
val latitude: Double,
val longitude: Double
) : Parcelable
