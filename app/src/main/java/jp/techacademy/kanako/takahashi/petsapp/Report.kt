package jp.techacademy.kanako.takahashi.petsapp

import java.io.Serializable

class Report(val day: String, val asa: String,val hiru: String, val yoru: String, val toilet: String, val weight: String, val detailmemo: String,  bytes: ByteArray):
    Serializable{
    val imageBytes: ByteArray = bytes.clone()

}
