package com.informatica.mycoletor

class ColetadosCarga(val id: Int = 0, val caracter: String = "", val codigoean : String = "", val codigointerno : String = "", val caracterespecial: String="", val descricao : String="", val embalagem : String = "", val quantidade : String = "") {

    override fun toString(): String {
        return "$codigoean  $descricao  $embalagem  $quantidade"
    }


}