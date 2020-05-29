package jp.techacademy.kanako.takahashi.petsapp

import java.io.Serializable

class Pet (val uid: String,val name: String,bytes: ByteArray): Serializable {
    val imageBytes: ByteArray

    init {
        imageBytes = bytes.clone()
    }
}