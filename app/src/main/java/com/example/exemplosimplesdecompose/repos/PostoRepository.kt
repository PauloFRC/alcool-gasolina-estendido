package com.example.exemplosimplesdecompose.repos

import android.content.Context
import android.content.SharedPreferences
import com.example.exemplosimplesdecompose.data.Posto
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken
import com.google.gson.Gson

class PostoRepository(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("gas_stations_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val GAS_STATIONS_KEY = "gas_stations"

    private val SELECTED_STATION_ID_KEY = "selected_station_id"

    private val SWITCH_STATE_KEY = "switch_state"

    fun getAllGasStations(): List<Posto> {
        val stationsJson = sharedPreferences.getString(GAS_STATIONS_KEY, null) ?: return emptyList()
        val type = object : TypeToken<List<Posto>>() {}.type
        return gson.fromJson(stationsJson, type)
    }

    fun saveGasStation(station: Posto) {
        val currentStations = getAllGasStations().toMutableList()

        val existingIndex = currentStations.indexOfFirst { it.id == station.id }
        if (existingIndex >= 0) {
            currentStations[existingIndex] = station
        } else {
            currentStations.add(station)
        }

        val stationsJson = gson.toJson(currentStations)
        sharedPreferences.edit().putString(GAS_STATIONS_KEY, stationsJson).apply()
    }

    fun updateGasStation(updatedStation: Posto) {
        val currentStations = getAllGasStations().toMutableList()

        val existingIndex = currentStations.indexOfFirst { it.id == updatedStation.id }
        if (existingIndex >= 0) {
            currentStations[existingIndex] = updatedStation

            val stationsJson = gson.toJson(currentStations)
            sharedPreferences.edit().putString(GAS_STATIONS_KEY, stationsJson).apply()
        }
    }

    fun deleteGasStation(stationId: String) {
        val currentStations = getAllGasStations().filter { it.id != stationId }
        val stationsJson = gson.toJson(currentStations)
        sharedPreferences.edit().putString(GAS_STATIONS_KEY, stationsJson).apply()

        if (getSelectedStationId() == stationId) {
            setSelectedStationId(null)
        }
    }

    fun getSelectedStationId(): String? {
        return sharedPreferences.getString(SELECTED_STATION_ID_KEY, null)
    }

    fun setSelectedStationId(stationId: String?) {
        sharedPreferences.edit()
            .putString(SELECTED_STATION_ID_KEY, stationId)
            .apply()
    }

    fun getSelectedStation(): Posto? {
        val selectedId = getSelectedStationId() ?: return null
        return getAllGasStations().find { it.id == selectedId }
    }

    fun getSwitchState(): Boolean {
        return sharedPreferences.getBoolean(SWITCH_STATE_KEY, true)
    }

    fun setSwitchState(state: Boolean) {
        sharedPreferences.edit().putBoolean(SWITCH_STATE_KEY, state).apply()
    }
}