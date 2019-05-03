package lpiemam.com.humiditeplantes

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import com.google.firebase.FirebaseApp
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), ValueEventListener {
    override fun onCancelled(error: DatabaseError) {
        Log.w("file", "Failed to read value.", error.toException())
    }

    override fun onDataChange(dataSnapshot: DataSnapshot) {
        // This method is called once with the initial value and again
        // whenever data at this location is updated.
        if (dataSnapshot.key == "inValue") {
            val value = dataSnapshot.getValue(Long::class.java)
            var textToPrint = ""
            when (value) {
                in 0L..300L -> textToPrint = "Too Low"
                in 300L..800L -> textToPrint = "Normal"
                in 800L..Long.MAX_VALUE -> textToPrint = "Too High"
                else -> textToPrint = "No Data"
            }
            Log.d("file", "Value is: " + value!!)
            tvValeurHumidite.text = textToPrint
        } else if (dataSnapshot.key == "autoValue") {
            val value = dataSnapshot.getValue(Long::class.java)
            autoModeSwitch.isChecked = value!! == 1L
            btHigh.isEnabled = !autoModeSwitch.isChecked
            btClose.isEnabled = !autoModeSwitch.isChecked
        }
    }

    var database: FirebaseDatabase? = null
    var myRef: DatabaseReference? = null

    var zoneList: ArrayList<Zone> = ArrayList()
    var adapter: ArrayAdapter<Zone>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        FirebaseApp.initializeApp(this)

        database = FirebaseDatabase.getInstance()
        myRef = database?.reference
        myRef?.addChildEventListener(object : ChildEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                if (p0 != null) {
                    val id = p0.key!!
                    val outValue = myRef?.child(id)?.child("outValue")
                    val inValue = myRef?.child(id)?.child("inValue")
                    val autoModeValue = myRef?.child(id)?.child("autoValue")

                    val sensor = Zone(id, autoModeValue, inValue, outValue)
                    zoneList.add(sensor)
                    adapter?.notifyDataSetChanged()
                }
            }

            override fun onChildRemoved(p0: DataSnapshot) {
                if (p0 != null) {
                    var zoneToRemove: Zone? = null
                    val id = p0.key!!
                    for (zone: Zone in zoneList) {
                        if (zone.id == id) {
                            zoneToRemove = zone
                        }
                    }
                    zoneList.remove(zoneToRemove!!)
                    adapter?.notifyDataSetChanged()
                }
            }

        })

        sensorSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val sensor = zoneList[position]
                resetAllListeners()
                supportActionBar?.title = "Zone " + sensor.id
                sensor.inValue?.addValueEventListener(this@MainActivity)
                sensor.autoModeValue?.addValueEventListener(this@MainActivity)

                btHigh.setOnClickListener { sensor.outValue?.setValue(1) }

                btClose.setOnClickListener { sensor.outValue?.setValue(0) }

                autoModeSwitch.setOnClickListener {
                    btHigh.isEnabled = !autoModeSwitch.isChecked
                    btClose.isEnabled = !autoModeSwitch.isChecked

                    sensor.autoModeValue?.setValue(if (autoModeSwitch.isChecked) 1 else 0)
                    sensor.outValue?.setValue(0)
                }
            }

        }
        adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, zoneList)

        sensorSpinner.adapter = adapter

        adapter?.notifyDataSetChanged()


        tvValeurHumidite!!.text = "Low"
    }

    private fun resetAllListeners() {
        for (zone: Zone in zoneList) {
            zone.inValue?.removeEventListener(this)
        }
    }


}
