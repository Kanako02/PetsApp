package jp.techacademy.kanako.takahashi.petsapp

import java.io.Serializable

class Pet(
    val uid: String,
    val name: String,
    val gender: String,
    val birth: String,
//    val old: String,
//    val profilememo: String,
//    val petUid: String,
    bytes: ByteArray
): Serializable {
    val imageBytes: ByteArray

    init {
        imageBytes = bytes.clone()
    }
}