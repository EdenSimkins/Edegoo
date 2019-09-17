package com.example.fragments

import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import java.io.DataOutputStream
import java.io.IOException
import java.net.Socket
import kotlin.math.sqrt

class CarControlFragment : Fragment() {

    companion object{
        lateinit var cmd: String
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment

        val view = inflater?.inflate(R.layout.fragment_carcontrol, container, false)

        val knobView: ImageView = view.findViewById(R.id.knob)
        val baseView: ImageView = view.findViewById(R.id.base)


        var distance: Float = 0.00f
        var direction: String

        var knobListener = View.OnTouchListener(function = {view, motionEvent ->

            val (centrex, centrey) = getCoordinates(baseView)

            if (motionEvent.action == MotionEvent.ACTION_MOVE){
                distance = sqrt((motionEvent.rawX - centrex)*(motionEvent.rawX - centrex) + (motionEvent.rawY - centrey)*(motionEvent.rawY - centrey )).toFloat()

                if (distance < 200){
                    view.setY(motionEvent.rawY -1260- knobView.height/2)
                    view.setX(motionEvent.rawX - knobView.width/2)
                    direction = getCommand(baseView, knobView)

                }else{
                    view.x = (centrex + (200/distance)*(motionEvent.rawX - centrex) - view.width/2).toFloat()
                    view.y = (centrey + (200/distance)*(motionEvent.rawY - centrey) -1260- view.height/2).toFloat()
                    direction = getCommand(baseView, knobView)

                }

                var (kx, ky) = getCoordinates(knobView)
                Log.d("ES", "Base Height: "+ baseView.height.toString()+ "Base Width: " + baseView.width.toString())
                Log.d("ES", "knob Height: "+ knobView.height.toString()+ "Base Width: " + knobView.width.toString())
                Log.d("ES", "knob centrex = "+kx.toString()+ " knob centre y= " +ky.toString())
                Log.d("ES", distance.toString())
                Log.d("ES", "centre x=" + centrex.toString() + " centre y = " +centrey.toString())
                Log.d("ES", "mousex = "+ motionEvent.rawX.toString() + " mousey = " +motionEvent.rawY.toString())

            } else {


                view.y = (centrey -1260- view.height/2).toFloat()
                view.x = (centrex - view.width/2).toFloat()
                direction = "S:0/n"
            }

            Log.d("ES", "direction =" +direction)

            cmd = direction
            var move: Socket_AsyncTask = Socket_AsyncTask()
            move.execute()

            true
        })

        knobView.setOnTouchListener(knobListener)

        return view
    }

    fun getCoordinates( view: ImageView): Array<Double>{
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        val x = (location[0] + view.width/2).toDouble() //width
        val y = (location[1] + view.height/2).toDouble() //height

        return arrayOf(x, y)
    }

    fun getCommand(viewBase: ImageView, viewKnob: ImageView): String{
        var (cos, opposite, speed) = getCos(viewBase, viewKnob)

        var direction: String = "S"
        var command: String

        if ((1/ sqrt(2.0) < cos) && (cos <= 1.0) ){
            direction = "F"
        } else if ((-1/ sqrt(2.0) <= cos) && (1/ sqrt(2.0) >= cos)){
            if(opposite >= 0){
                direction = "R"
            } else if(opposite < 0){
                direction = "L"
            }
        } else if ((cos < -1/ sqrt(2.0)) && (cos >= -1.0)) {
            direction = "B"
        }

        command = direction +":"+speed.toInt().toString()+"/n"
        return command

    }

    fun getCos(viewBase: ImageView, viewKnob: ImageView): Array<Double> {
        val (baseX, baseY) = getCoordinates(viewBase)
        var (knobX, knobY) = getCoordinates(viewKnob)
        var hypotenuse: Double = getHypotenuse(baseX, baseY, knobX, knobY)
        var adjacent: Double = getAdjacent(baseY, knobY)
        var opposite: Double = getOpposite(baseX, knobX)
        var cos: Double = adjacent/hypotenuse
        var speed: Double = 60 + 40*hypotenuse/200

        return arrayOf(cos,opposite, speed)
    }

    fun getHypotenuse(bx: Double, by: Double, kx: Double, ky: Double): Double {
        var hypotenuse: Double = sqrt((bx-kx)*(bx-kx) + (by-ky)*(by-ky))
        return hypotenuse
    }

    fun getAdjacent(by: Double, ky: Double): Double {
        var adjacent: Double = by - ky
        return adjacent
    }

    fun getOpposite(bx: Double, kx: Double): Double{
        var opposite = kx - bx
        return opposite
    }

    private class Socket_AsyncTask: AsyncTask<Void, Void, Void>(){

        lateinit var socket: Socket
        var wifiModuleIp = "192.168.4.1"
        var wifiModulePort = 12345

        override fun doInBackground(vararg p0: Void?): Void? {
            try{
                socket = Socket(wifiModuleIp, wifiModulePort)
                socket.reuseAddress = true
                var dataOutputStream: DataOutputStream = DataOutputStream(socket.getOutputStream())
                dataOutputStream.writeBytes(cmd)
                dataOutputStream.close()
                socket.close()
            } catch (e: IOException){
                e.printStackTrace()
            }

            return null
        }

    }

}