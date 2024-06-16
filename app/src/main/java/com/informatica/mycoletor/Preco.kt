package com.informatica.mycoletor

//class Preco (var codigointerno:String="", var descricao: String = "", var codigoean: String = "", var preco: String ="", var precooferta: String ="") {
class Preco(val codigoean: String ="", val codigointerno: String="", val descricao: String="", val embalagem: String="",val preco: String="") {

    override fun toString(): String {
        //return "Preco(codigointerno='$codigointerno', descricao='$descricao', codigoean='$codigoean', preco='$preco', precooferta='$precooferta')"
        return "Preco(codigoean='$codigoean', codigointerno='$codigointerno', descricao='$descricao',embalagem='$embalagem', preco='$preco')"
    }
}