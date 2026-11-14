package com.example.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

object QiblaCalculator {
    private const val KAABA_LAT = 21.4225
    private const val KAABA_LON = 39.8262

    fun calculateQiblaBearing(latitude: Double, longitude: Double): Float {
        val phi1 = Math.toRadians(latitude)
        val phi2 = Math.toRadians(KAABA_LAT)
        val deltaLambda = Math.toRadians(KAABA_LON - longitude)

        val y = sin(deltaLambda) * cos(phi2)
        val x = cos(phi1) * sin(phi2) - sin(phi1) * cos(phi2) * cos(deltaLambda)
        val theta = atan2(y, x)
        val deg = (Math.toDegrees(theta) + 360.0) % 360.0
        return deg.toFloat()
    }
}

class QiblaSensorManager(context: Context) {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    val hasCompassSensor: Boolean
        get() = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) != null ||
            (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null &&
                sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null)

    fun observeAzimuthDegrees(): Flow<Float?> = callbackFlow {
        val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magnetSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        if (rotationSensor == null && (accelSensor == null || magnetSensor == null)) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val accelReading = FloatArray(3)
        val magnetReading = FloatArray(3)
        var hasAccel = false
        var hasMagnet = false
        var smoothSin = 0.0
        var smoothCos = 1.0
        var initialized = false

        fun emitSmoothed(rawAzimuthDeg: Float) {
            val rad = rawAzimuthDeg * PI / 180.0
            val s = sin(rad)
            val c = cos(rad)
            if (!initialized) {
                smoothSin = s
                smoothCos = c
                initialized = true
            } else {
                val alpha = 0.16
                smoothSin = smoothSin * (1 - alpha) + s * alpha
                smoothCos = smoothCos * (1 - alpha) + c * alpha
            }
            val smoothed = ((atan2(smoothSin, smoothCos) * 180.0 / PI) + 360.0) % 360.0
            trySend(smoothed.toFloat())
        }

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                when (event.sensor.type) {
                    Sensor.TYPE_ROTATION_VECTOR -> {
                        val rotMatrix = FloatArray(9)
                        val orientation = FloatArray(3)
                        SensorManager.getRotationMatrixFromVector(rotMatrix, event.values)
                        SensorManager.getOrientation(rotMatrix, orientation)
                        val deg = ((Math.toDegrees(orientation[0].toDouble()) + 360.0) % 360.0).toFloat()
                        emitSmoothed(deg)
                    }
                    Sensor.TYPE_ACCELEROMETER -> {
                        System.arraycopy(event.values, 0, accelReading, 0, 3)
                        hasAccel = true
                        if (hasMagnet) {
                            val r = FloatArray(9)
                            val i = FloatArray(9)
                            if (SensorManager.getRotationMatrix(r, i, accelReading, magnetReading)) {
                                val orientation = FloatArray(3)
                                SensorManager.getOrientation(r, orientation)
                                val deg = ((Math.toDegrees(orientation[0].toDouble()) + 360.0) % 360.0).toFloat()
                                emitSmoothed(deg)
                            }
                        }
                    }
                    Sensor.TYPE_MAGNETIC_FIELD -> {
                        System.arraycopy(event.values, 0, magnetReading, 0, 3)
                        hasMagnet = true
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }

        if (rotationSensor != null) {
            sensorManager.registerListener(listener, rotationSensor, SensorManager.SENSOR_DELAY_UI)
        } else {
            sensorManager.registerListener(listener, accelSensor, SensorManager.SENSOR_DELAY_UI)
            sensorManager.registerListener(listener, magnetSensor, SensorManager.SENSOR_DELAY_UI)
        }

        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }
}
