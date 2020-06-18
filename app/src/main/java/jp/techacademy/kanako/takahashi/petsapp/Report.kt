package jp.techacademy.kanako.takahashi.petsapp

import java.io.Serializable

class Report(
    val reportUid: String, val day: String,val orderCnt: String, val condition: String, val asa: String,
    val hiru: String, val yoru: String, val toilet: String, val weight: String, val detailmemo: String, bytes: ByteArray):
    Serializable{
    val imageBytes: ByteArray = bytes.clone()

    var id: Int = 0  //追加
}
