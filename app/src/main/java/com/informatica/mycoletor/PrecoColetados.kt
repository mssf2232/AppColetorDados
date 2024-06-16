package com.informatica.mycoletor

class PrecoColetados(val id: Int = 0, val caracter: String = "", val codigoean : String = "", val codigointerno : String = "", val descricao : String="", val embalagem : String="",val preco : String = "") {

    override fun toString(): String {

        return "$codigoean$descricao"
    }
}