package lpiemam.com.humiditeplantes

import com.google.firebase.database.DatabaseReference

class Zone (val id: String?, val autoModeValue: DatabaseReference?, val inValue: DatabaseReference?, val outValue: DatabaseReference?) {
    override fun toString(): String {
        return "Zone $id"
    }
}