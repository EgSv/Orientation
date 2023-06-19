package ru.startandroid.develop.orientation

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.Display
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class MainActivity : AppCompatActivity() {
    private var tvText: TextView? = null
    private var sensorManager: SensorManager? = null
    private var sensorAccel: Sensor? = null
    private var sensorMagnet: Sensor? = null
    private var sb = StringBuilder()
    private var timer: Timer? = null
    private var rotation = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tvText = findViewById<View>(R.id.tvText) as TextView
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        sensorAccel = sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorMagnet = sensorManager!!.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    }

    override fun onResume() {
        super.onResume()
        sensorManager!!.registerListener(listener, sensorAccel, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager!!.registerListener(listener, sensorMagnet, SensorManager.SENSOR_DELAY_NORMAL)
        timer = Timer()
        val task: TimerTask = object : TimerTask() {
            override fun run() {
                runOnUiThread {
                    deviceOrientation
                    actualDeviceOrientation
                    showInfo()
                }
            }
        }
        timer!!.schedule(task, 0, 400)
        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display: Display = windowManager.defaultDisplay
        rotation = display.rotation
    }

    override fun onPause() {
        super.onPause()
        sensorManager!!.unregisterListener(listener)
        timer?.cancel()
    }

    private fun format(values: FloatArray): String {
        return String.format("%1$.1f\t\t%2$.1f\t\t%3$.1f", values[0], values[1], values[2])
    }

    fun showInfo() {
        sb.setLength(0)
        sb.append("Orientation : " + format(valuesResult))
            .append("""
    
    Orientation 2: ${format(valuesResult2)}
    """.trimIndent())
        tvText!!.text = sb
    }

    private var r = FloatArray(9)
    val deviceOrientation: Unit
        get() {
            SensorManager.getRotationMatrix(r, null, valuesAccel, valuesMagnet)
            SensorManager.getOrientation(r, valuesResult)
            valuesResult[0] = Math.toDegrees(valuesResult[0].toDouble()).toFloat()
            valuesResult[1] = Math.toDegrees(valuesResult[1].toDouble()).toFloat()
            valuesResult[2] = Math.toDegrees(valuesResult[2].toDouble()).toFloat()
            return
        }
    private var inR = FloatArray(9)
    private var outR = FloatArray(9)
    val actualDeviceOrientation: Unit
        get() {
            SensorManager.getRotationMatrix(inR, null, valuesAccel, valuesMagnet)
            var xAxis = SensorManager.AXIS_X
            var yAxis = SensorManager.AXIS_Y
            when (rotation) {
                android.view.Surface.ROTATION_0 -> {}
                android.view.Surface.ROTATION_90 -> {
                    xAxis = SensorManager.AXIS_Y
                    yAxis = SensorManager.AXIS_MINUS_X
                }
                android.view.Surface.ROTATION_180 -> yAxis = SensorManager.AXIS_MINUS_Y
                android.view.Surface.ROTATION_270 -> {
                    xAxis = SensorManager.AXIS_MINUS_Y
                    yAxis = SensorManager.AXIS_X
                }
                else -> {}
            }
            SensorManager.remapCoordinateSystem(inR, xAxis, yAxis, outR)
            SensorManager.getOrientation(outR, valuesResult2)
            valuesResult2[0] = Math.toDegrees(valuesResult2[0].toDouble()).toFloat()
            valuesResult2[1] = Math.toDegrees(valuesResult2[1].toDouble()).toFloat()
            valuesResult2[2] = Math.toDegrees(valuesResult2[2].toDouble()).toFloat()
            return
        }
    var valuesAccel = FloatArray(3)
    var valuesMagnet = FloatArray(3)
    private var valuesResult = FloatArray(3)
    private var valuesResult2 = FloatArray(3)
    private var listener: SensorEventListener = object : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        override fun onSensorChanged(event: SensorEvent) {
            when (event.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> {
                    var i = 0
                    while (i < 3) {
                        valuesAccel[i] = event.values[i]
                        i++
                    }
                }
                Sensor.TYPE_MAGNETIC_FIELD -> {
                    var i = 0
                    while (i < 3) {
                        valuesMagnet[i] = event.values[i]
                        i++
                    }
                }
            }
        }
    }
}
