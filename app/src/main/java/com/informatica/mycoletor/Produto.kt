package com.informatica.mycoletor

class Produto (var codigointerno:String="", var descricao: String = "", var codigoean: String = "", var embalagem: String ="", var secao: String ="") {

    override fun toString(): String {
        return "$codigointerno $descricao $codigoean $embalagem $secao"
    }
}