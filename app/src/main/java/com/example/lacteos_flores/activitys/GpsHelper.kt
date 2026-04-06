package com.example.lacteos_flores.activitys
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.io.IOException
import java.util.Locale

class GpsHelper(private val context: Context) {
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    // Usamos lambdas para manejar el éxito y el error
    fun obtenerUbicacionActual(
        onSuccess: (lat: Double, lon: Double, address: String) -> Unit,
        onError: (message: String) -> Unit
    ) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            onError("Permisos no concedidos")
            return
        }

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location ->
                if (location != null) {
                    val lat = location.latitude
                    val lon = location.longitude
                    val direccion = obtenerDireccion(lat, lon)
                    onSuccess(lat, lon, direccion)
                } else {
                    onError("No se pudo obtener la ubicación")
                }
            }
            .addOnFailureListener { exception ->
                onError(exception.message ?: "Error desconocido al obtener GPS")
            }
    }

    private fun obtenerDireccion(lat: Double, lon: Double): String {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(lat, lon, 1)

            if (!addresses.isNullOrEmpty()) {
                addresses[0].getAddressLine(0) ?: "Dirección sin detalles"
            } else {
                "Dirección no encontrada"
            }
        } catch (e: IOException) {
            e.printStackTrace()
            "Error de red al buscar dirección"
        }
    }
}
