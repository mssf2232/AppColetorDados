package com.informatica.mycoletor

class FrigoColetados(val id:Int=0, val filial:String="", val numdoc:String="", val codigoean:String="", val descricao:String="", val codigobarra:String="") {

    override fun toString(): String {
        return "$codigobarra"
    }
}