package jp.techacademy.kanako.takahashi.petsapp

import java.io.Serializable

class Report (val day: String,val food: String, val toilet: String, val weight: String, val memo: String, bytes: ByteArray):
    Serializable{
    val imageBytes: ByteArray

    init {
        imageBytes = bytes.clone()
    }
}
