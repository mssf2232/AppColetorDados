package com.informatica.mycoletor

class InventarioColetados(val id: Int = 0, val caracter: String = "", val codigoean : String = "", val codigointerno : String = "",val secao: String="", val descricao : String="", val embalagem : String = "", val quantidade : String = "") {

    override fun toString(): String {
        return "$codigoean  $descricao  $embalagem  $quantidade"
    }
}