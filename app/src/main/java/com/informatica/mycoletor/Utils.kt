package com.informatica.mycoletor

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.provider.Settings
import androidx.core.content.ContextCompat
import java.io.File

fun Context.isPermissionGranted(permission: String):Boolean{

    return ContextCompat.checkSelfPermission(this,permission) == PackageManager.PERMISSION_GRANTED

}
inline fun Context.cameraPermissionRequest(crossinline positive: () -> Unit) {
    AlertDialog.Builder(this)
        .setTitle("Permissão da Camera Requerida")
        .setMessage("Sem Acesso a Camera não será possivél Scannear codigos de Barras")
        .setPositiveButton("Permitir a Camera") {dialog, which ->
            positive.invoke()
        }.setNegativeButton("Cancelar") {dialog, which ->

        }.show()
}

fun Context.openPermissionSetting(){

    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).also {
        val uri: Uri = Uri.fromParts("package",packageName, null)
        it.data = uri
        startActivity(it)
    }

}

fun Context.Alerta(Titulo: String, Msg: String) {

    val builder = AlertDialog.Builder(this)
    builder.setTitle("$Titulo")
    builder.setMessage("$Msg")
    builder.setPositiveButton("OK"){ dial, _ ->

        val dialog = builder.create()

        try{

            dialog.show()

        }catch (e: Exception){

            dialog.dismiss()
        }

    }

}
fun Context.Balerta(Titulo: String, Msg: String) {

    val builder = AlertDialog.Builder(this)

    with(builder)
    {
        setTitle("$Titulo")
        setMessage("$Msg")
        setPositiveButton("OK", null)
              show()
    }


}

fun Context.checkFileExists():Boolean {

    val downloadsDirectory =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
    val caminhoArquivo = File(downloadsDirectory, "/importar/CONSULTA.TXT")

    val result = caminhoArquivo.toString()

    val file = File(result)
    if (!file.exists()) {
               Balerta("INFORMATIVO","ARQUIVO NÃO ENCONTRADO:\n /DUCUMENTS/importar/\nCONSULTA.TXT")
        return false
    }
    return true
}




