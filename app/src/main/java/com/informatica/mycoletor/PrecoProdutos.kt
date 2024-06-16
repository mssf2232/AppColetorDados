package com.informatica.mycoletor

class PrecoProdutos(val id: Int = 0, val codigoean: String ="", val codigointerno: String="", val descricao: String="", val embalagem: String="",val preco: String="") {

    override fun toString(): String {
        //return "FindPrice(id=$id, codigoean='$codigoean', codigointerno='$codigointerno', descricao='$descricao', preco='$preco', precooferta='$precooferta')"
        return "FindPrice(id=$id, codigoean='$codigoean', codigointerno='$codigointerno', descricao='$descricao',embalagem='$embalagem', preco='$preco')"
    }
}