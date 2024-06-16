package com.informatica.mycoletor
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(context: Context) : SQLiteOpenHelper (context,"price.db",null,1) {

    val sql = arrayOf(
        "CREATE TABLE produtos (id INTEGER PRIMARY KEY AUTOINCREMENT, codigointerno TEXT, descricao TEXT, codigoean TEXT, embalagem TEXT, secao TEXT)",
        "CREATE TABLE coletadoscarga (id INTEGER PRIMARY KEY AUTOINCREMENT, caracter TEXT,codigoean TEXT ,codigointerno TEXT,caracterespecial TEXT, descricao TEXT, embalagem TEXT, quantidade TEXT)",
        "CREATE TABLE coletadosinventario (id INTEGER PRIMARY KEY AUTOINCREMENT, caracter TEXT,codigoean TEXT ,codigointerno TEXT,secao TEXT, descricao TEXT, embalagem TEXT, quantidade TEXT)",
        "CREATE TABLE produtosprecos (id INTEGER PRIMARY KEY AUTOINCREMENT, codigoean TEXT ,codigointerno TEXT, descricao TEXT, embalagem TEXT,preco TEXT)",
        "CREATE TABLE coletadosprecos (id INTEGER PRIMARY KEY AUTOINCREMENT, caracter TEXT,codigoean TEXT ,codigointerno TEXT, descricao TEXT,embalagem TEXT, preco TEXT, quantidade TEXT)",
        "CREATE TABLE coletadosfrigo (id INTEGER PRIMARY KEY AUTOINCREMENT,filial TEXT,numdoc TEXT,codigoean TEXT , descricao TEXT,codigobarracx TEXT)"

    )

    //Classe do Banco
    override fun onCreate(db: SQLiteDatabase) {
        println("Entrou na funcao onCreate do DBHELPER")
        sql.forEach {
            println("Executando SQL $it")
            db.execSQL(it)
        }

    }

    //Classe do Banco
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        println("Entrou na fun onUpgrade e Chamando o onCREATE abaixo")
        // db.execSQL("DROP TABLE preco")
        onCreate(db)

    }

    /*

    Metodo Principal  Cadastro de Produtos Utlizado pelo Carga e Inventario


    */


    //Metodos do Cadastro princioal dos Produtos utilizado na importação do Consulta.txt ----
    fun produtosInsert(
        codigointerno: String,
        descricao: String,
        codigoean: String,
        embalagem: String,
        secao: String
    ): Long {

        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put("codigointerno", codigointerno)
        contentValues.put("descricao", descricao)
        contentValues.put("codigoean", codigoean)
        contentValues.put("embalagem", embalagem)
        contentValues.put("secao", secao)

        val res = db.insert("produtos", null, contentValues)
        db.close()
        return res

    }

    fun inserirProdutos(produtos: List<Produto>): Int {

        val db = writableDatabase
        db.beginTransaction()

        try {
            for (produto in produtos) {

                val values = ContentValues().apply {
                    put("codigointerno", produto.codigointerno)
                    put("descricao", produto.descricao)
                    put("codigoean", produto.codigoean)
                    put("embalagem", produto.embalagem)
                    put("secao", produto.secao)
                }
                db.insert("produtos", null, values)

            }
            db.setTransactionSuccessful()
            // Progresso(10)
        } finally {
            db.endTransaction()
        }
        //Progresso(100)
        db.close()
        // println("FECHOU O BANCO")
        return 0

    }

    fun inserirCadprod(precos: List<Preco>): Int {
        //PRECO -> codigoean,codigointerno,descricao,embalagem, preco

        val db = writableDatabase
        db.beginTransaction()

        try {
            for (produto in precos) {

                val values = ContentValues().apply {
                    put("codigointerno", produto.codigointerno)
                    put("descricao", produto.descricao)
                    put("embalagem", produto.embalagem)
                    put("codigoean", produto.codigoean)
                    put("preco", produto.preco)
                    //put("precooferta",produto.precooferta)
                }
                db.insert("produtosprecos", null, values)

            }
            db.setTransactionSuccessful()
            // Progresso(10)
        } finally {
            db.endTransaction()
        }
        //Progresso(100)
        db.close()
        // println("FECHOU O BANCO")
        return 0

    }


    //Metodo utilizado para consulta dos itens incluidos na tabela Produto devolvendo um array(Produtos)
    fun produtosListaSelectAll(): ArrayList<Produtos> {

        val db = this.readableDatabase
        val c = db.rawQuery("Select * From produtos", null,)
        val listaProdutos: ArrayList<Produtos> = ArrayList()

        if (c.count > 0) {

            c.moveToFirst()
            do {
                val idIndex = c.getColumnIndex("id")
                val codigointernoIndex = c.getColumnIndex("codigointerno")
                val descricaoIndex = c.getColumnIndex("descricao")
                val codigoeanIndex = c.getColumnIndex("codigoean")
                val embalagemIndex = c.getColumnIndex("embalagem")
                val secaoIndex = c.getColumnIndex("secao")

                val id = c.getInt(idIndex)
                val codigointerno = c.getString(codigointernoIndex)
                val descricao = c.getString(descricaoIndex)
                val codigoean = c.getString(codigoeanIndex)
                val embalagem = c.getString(embalagemIndex)
                val secao = c.getString(secaoIndex)

                listaProdutos.add(
                    Produtos(
                        id,
                        codigointerno,
                        descricao,
                        codigoean,
                        embalagem,
                        secao
                    )
                )
            } while (c.moveToNext())

            db.close()

        }



        return listaProdutos

    }


    //Fim Metodos do Cadastro princioal dos Produtos utilizado na importação do Consulta.txt ----

    //Selecionar todos os itens Coletados pela conferencia devolvendo um Cursor
    fun coletadoCargaTodos(): Cursor {

        val db = this.readableDatabase
        val c = db.rawQuery("Select * From coletadoscarga", null,)
        db.close()
        return c
    }

    //Selecionar todos os itens Coletados pela conferencia devolvendo um array ColetadosCarga

    fun coletadoCargaListaTodos(): ArrayList<ColetadosCarga> {

        val db = this.readableDatabase
        val c = db.rawQuery("Select * From coletadoscarga", null,)

        val listaProdutosColetados: ArrayList<ColetadosCarga> = ArrayList()

        if (c.count > 0) {

            c.moveToFirst()
            do {
                val idIndex = c.getColumnIndex("id")
                val caracterIndex = c.getColumnIndex("caracter")
                val codigoeanIndex = c.getColumnIndex("codigoean")
                val codigointernoIndex = c.getColumnIndex("codigointerno")
                val caracterespecialIndex = c.getColumnIndex("caracterespecial")
                val descricaoIndex = c.getColumnIndex("descricao")
                val embalagemIndex = c.getColumnIndex("embalagem")
                val quantidadeIndex = c.getColumnIndex("quantidade")

                val id = c.getInt(idIndex)
                val caracter = c.getString(caracterIndex)
                val codigoean = c.getString(codigoeanIndex)
                val codigointerno = c.getString(codigointernoIndex)
                val caracterespecial = c.getString(caracterespecialIndex)
                val descricao = c.getString(descricaoIndex)
                val embalagem = c.getString(embalagemIndex)
                val quantidade = c.getString(quantidadeIndex)

                listaProdutosColetados.add(
                    ColetadosCarga(
                        id,
                        caracter,
                        codigoean,
                        codigointerno,
                        caracterespecial,
                        descricao,
                        embalagem,
                        quantidade
                    )
                )
            } while (c.moveToNext())

        }

        db.close()
        return listaProdutosColetados

    }


    //Metodo para inserir dados na lista do ColetadosCarga
    fun coletadosCargaInsert(
        caracter: String,
        codigoean: String,
        codigointerno: String,
        caracterespecial: String,
        descricao: String,
        embalagem: String,
        quantidade: String
    ): Long {

        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put("caracter", caracter)
        contentValues.put("codigoean", codigoean)
        contentValues.put("codigointerno", codigointerno)
        contentValues.put("caracterespecial", caracterespecial)
        contentValues.put("descricao", descricao)
        contentValues.put("embalagem", embalagem)
        contentValues.put("quantidade", quantidade)

        val res = db.insert("coletadosCarga", null, contentValues)
        db.close()
        return res

    }

    //Deletar um item especifico da lista dos Produtos coletadosCarga
    fun coletadosCargaDelete(id: Int): Int {
        val db = this.writableDatabase
        val res = db.delete("coletadoscarga", "id=?", arrayOf(id.toString()))
        db.close()
        return res
    }

    //Limpar a tabela ColetadosCarga dos Produtos Coletados para iniciar uma nova Coletagem
    fun limparTabela() {
        val db = writableDatabase
        db.delete("coletadoscarga", null, null)
        db.close()
    }

    fun limparTabelaProdutos() {
        println("Limpando a tabela de produtos")
        val db = writableDatabase
        db.delete("produtos", null, null)
        db.close()
    }

    fun limparTabelaPrecos() {
        println("Limpando a tabela de PRECOS")
        val db = writableDatabase
        db.delete("produtosprecos", null, null)
        db.close()
    }

    //Limpar a tabela  ColetadosInventario dos Produtos Coletados para iniciar uma nova Coletagem
    fun limparTabelaInventarioColetado() {
        val db = writableDatabase
        db.delete("coletadosinventario", null, null)
        db.close()
    }

    //Limpar a tabela  ColetadosInventario dos Produtos Coletados para iniciar uma nova Coletagem
    fun limparTabelaLivroPrecosColetados() {
        val db = writableDatabase
        db.delete("coletadosprecos", null, null)
        db.close()
    }

    //Limpar a tabela ColetadosCarga dos Produtos Coletados para iniciar uma nova Coletagem
    fun limparTabela(nomeTabela: String) {
        val db = writableDatabase
        db.delete("coletadosfrigo", null, null)
        db.close()
    }


    //Metods  Compartilhadps com Livro de Preço e Inventario

    //metodo Coletor de preços selcionar pelo EAN que devolve um Cursor
    fun produtosSelectByCodigoEAN(codigoean: String): Cursor {
        val db = this.readableDatabase
        val c = db.rawQuery("Select * From produtos where codigoean=?", arrayOf(codigoean))
        db.close()
        return c
    }


    //Metodo Coletor de preco selecionar pelo EAN e devolve o Produto
    fun produtosObjectSelectByCodigoEAN(codigoean: String): Produtos {
        val db = this.readableDatabase
        val c = db.rawQuery("Select * From produtos where codigoean=?", arrayOf(codigoean))
        var produtos = Produtos()

        if (c.count == 1) {
            c.moveToFirst()
            val idIndex = c.getColumnIndex("id")
            val codigointernoIndex = c.getColumnIndex("codigointerno")
            val descricaoIndex = c.getColumnIndex("descricao")
            val codigoeanIndex = c.getColumnIndex("codigoean")
            val embalagemIndex = c.getColumnIndex("embalagem")
            val secaoIndex = c.getColumnIndex("secao")

            val id = c.getInt(idIndex)
            val codigointerno = c.getString(codigointernoIndex)
            val descricao = c.getString(descricaoIndex)
            val codigoean = c.getString(codigoeanIndex)
            val embalagem = c.getString(embalagemIndex)
            val secao = c.getString(secaoIndex)

            produtos = Produtos(id, codigointerno, descricao, codigoean, embalagem, secao)
        }
        db.close()
        return produtos
    }



    fun PrecosColetadosTodos(): Cursor {
        val db = this.readableDatabase
        val c = db.rawQuery("Select * From coletadosprecos", null,)
        db.close()
        return c
    }

    fun PrecosColetadosListaTodos(): ArrayList<PrecoColetados> {

        val db = this.readableDatabase
        val c = db.rawQuery("Select * From coletadosprecos order By id desc ", null,)

        val listarPrecoColetados: ArrayList<PrecoColetados> = ArrayList()

        if (c.count > 0) {

            c.moveToFirst()
            do {
                val idIndex = c.getColumnIndex("id")
                val caracterIndex = c.getColumnIndex("caracter")
                val codigoeanIndex = c.getColumnIndex("codigoean")
                val codigointernoIndex = c.getColumnIndex("codigointerno")
                val descricaoIndex = c.getColumnIndex("descricao")
                val embalagemIndex = c.getColumnIndex("embalagem")
                val precoIndex = c.getColumnIndex("preco")
                //val quantidadeIndex = c.getColumnIndex("quantidade")


                val id = c.getInt(idIndex)
                val caracter = c.getString(caracterIndex)
                val codigoean = c.getString(codigoeanIndex)
                val codigointerno = c.getString(codigointernoIndex)
                val descricao = c.getString(descricaoIndex)
                val embalagem = c.getString(embalagemIndex)
                val preco = c.getString(precoIndex)
                //val quantidade = c.getString(quantidadeIndex)

                listarPrecoColetados.add(
                    PrecoColetados(
                        id,
                        caracter,
                        codigoean,
                        codigointerno,
                        descricao,
                        embalagem,
                        preco
                    )
                )
            } while (c.moveToNext())
        }
        db.close()
        return listarPrecoColetados
    }


    //Metodo para inserir dados na lista do Livro de Precos COLETADOS
    fun livroPrecoInsert(
        caracter: String,
        codigoean: String,
        codigointerno: String,
        descricao: String,
        embalagem: String,
        preco: String,
        quantidade: String
    ): Long {

        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put("caracter", caracter)
        contentValues.put("codigoean", codigoean)
        contentValues.put("codigointerno", codigointerno)
        contentValues.put("descricao", descricao)
        contentValues.put("embalagem", embalagem)
        contentValues.put("preco", preco)
        contentValues.put("quantidade", quantidade)
        val res = db.insert("coletadosprecos", null, contentValues)
        db.close()
        return res
    }

    //Deletar
    fun livroPrecosDelete(id: Int): Int {
        val db = this.writableDatabase
        val res = db.delete("coletadosprecos", "id=?", arrayOf(id.toString()))
        db.close()
        return res
    }


    //Metodos do Inventario+++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    fun nventarioSelectById(codigoean: String): Cursor {
        val db = this.readableDatabase
        val c = db.rawQuery("Select * from produto where codigoean=?", arrayOf(codigoean))
        db.close()
        return c

    }

    //Selecionar todos os itens Coletados pelo Inventario devolvendo um array InventarioColetados

    fun coletadoInvetarioListaTodos(): ArrayList<InventarioColetados> {

        val db = this.readableDatabase
        val c = db.rawQuery("Select * From coletadosinventario", null,)

        val listaProdutosColetados: ArrayList<InventarioColetados> = ArrayList()

        if (c.count > 0) {

            c.moveToFirst()
            do {
                val idIndex = c.getColumnIndex("id")
                val caracterIndex = c.getColumnIndex("caracter")
                val codigoeanIndex = c.getColumnIndex("codigoean")
                val codigointernoIndex = c.getColumnIndex("codigointerno")
                val secaoIndex = c.getColumnIndex("secao")
                val descricaoIndex = c.getColumnIndex("descricao")
                val embalagemIndex = c.getColumnIndex("embalagem")
                val quantidadeIndex = c.getColumnIndex("quantidade")

                val id = c.getInt(idIndex)
                val caracter = c.getString(caracterIndex)
                val codigoean = c.getString(codigoeanIndex)
                val codigointerno = c.getString(codigointernoIndex)
                val secao = c.getString(secaoIndex)
                val descricao = c.getString(descricaoIndex)
                val embalagem = c.getString(embalagemIndex)
                val quantidade = c.getString(quantidadeIndex)

                listaProdutosColetados.add(
                    InventarioColetados(
                        id,
                        caracter,
                        codigoean,
                        codigointerno,
                        secao,
                        descricao,
                        embalagem,
                        quantidade
                    )
                )
            } while (c.moveToNext())

        }

        db.close()
        return listaProdutosColetados

    }


    //Metodo para inserir dados na lista do ColetadosInventario
    fun coletadosInventarioInsert(
        caracter: String,
        codigoean: String,
        codigointerno: String,
        secao: String,
        descricao: String,
        embalagem: String,
        quantidade: String
    ): Long {

        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put("caracter", caracter)
        contentValues.put("codigoean", codigoean)
        contentValues.put("codigointerno", codigointerno)
        contentValues.put("secao", secao)
        contentValues.put("descricao", descricao)
        contentValues.put("embalagem", embalagem)
        contentValues.put("quantidade", quantidade)

        val res = db.insert("coletadosinventario", null, contentValues)
        db.close()
        return res

    }

    //Deletar
    fun coletadosInventarioDelete(id: Int): Int {
        val db = this.writableDatabase
        val res = db.delete("coletadosinventario", "id=?", arrayOf(id.toString()))
        db.close()
        return res
    }

    //FIM METODO INVENTARIO *************************************************

    //TODOS OS METODOS PrecoProdutos ************************************************************

    //Metodo para inserir cadastro principal livro de Preco Activity
    fun implivroPrecoInsert(
        codigoean: String,
        codigointerno: String,
        descricao: String,
        embalagem: String,
        preco: String,
        //precooferta: String
    ): Long {

        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put("codigoean", codigoean)
        contentValues.put("codigointerno", codigointerno)
        contentValues.put("descricao", descricao)
        contentValues.put("embalagem", embalagem)
        contentValues.put("preco", preco)
        //contentValues.put("precooferta", precooferta)

        val res = db.insert("produtosprecos", null, contentValues)
        db.close()
        return res

    }


    //metodo Livro de Preco Consulta por EAN
    fun livroPrecoSelectByCodigoEAN(codigoean: String): Cursor {
        val db = this.readableDatabase
        val c = db.rawQuery("Select * From produtosprecos where codigoean=?", arrayOf(codigoean))
        db.close()
        return c
    }


    //Metodo Preco Produtos
    fun livroPrecoObjectSelectByCodigoEAN(codigoean: String): PrecoProdutos {

        val db = this.readableDatabase
        val c = db.rawQuery("Select * From produtosprecos where codigoean=?", arrayOf(codigoean))

        var findpreco = PrecoProdutos()

        if (c.count == 1) {
            c.moveToFirst()
            val idIndex = c.getColumnIndex("id")
            val codigoeanIndex = c.getColumnIndex("codigoean")
            val codigointernoIndex = c.getColumnIndex("codigointerno")
            val descricaoIndex = c.getColumnIndex("descricao")
            val embalagemIndex = c.getColumnIndex("embalagem")
            val precoIndex = c.getColumnIndex("preco")
            //val precoofertaIndex = c.getColumnIndex("precooferta")

            val id = c.getInt(idIndex)
            val codigoean = c.getString(codigoeanIndex)
            val codigointerno = c.getString(codigointernoIndex)
            val descricao = c.getString(descricaoIndex)
            val embalagem = c.getString(embalagemIndex)
            val preco = c.getString(precoIndex)
            //val precooferta = c.getString(precoofertaIndex)


            //Responsavel pela ordem da exibicao do activity
            findpreco = PrecoProdutos(id, codigoean, codigointerno, descricao, embalagem, preco)


        }
        db.close()

        return findpreco
    }

    //Metodo PrecosProdutos
    fun livroPrecoSelectAll(): Cursor {

        val db = this.readableDatabase
        val c = db.rawQuery("Select * From produtosprecos", null,)
        db.close()
        return c
    }

    //metodo PrecosProdutos
    fun livroPrecoListaSelectAll(): ArrayList<PrecoProdutos> {

        val db = this.readableDatabase
        val c = db.rawQuery("Select * From produtosprecos", null,)
        val listaPrecoProdutos: ArrayList<PrecoProdutos> = ArrayList()

        if (c.count > 0) {

            c.moveToFirst()
            do {
                val idIndex = c.getColumnIndex("id")
                val codigoeanIndex = c.getColumnIndex("codigoean")
                val codigointernoIndex = c.getColumnIndex("codigointerno")
                val descricaoIndex = c.getColumnIndex("descricao")
                val precoIndex = c.getColumnIndex("preco")
                val precoofertaIndex = c.getColumnIndex("precooferta")

                val id = c.getInt(idIndex)
                val codigoean = c.getString(codigoeanIndex)
                val codigointerno = c.getString(codigointernoIndex)
                val descricao = c.getString(descricaoIndex)
                val preco = c.getString(precoIndex)
                val precooferta = c.getString(precoofertaIndex)

                listaPrecoProdutos.add(
                    PrecoProdutos(
                        id,
                        codigointerno,
                        descricao,
                        codigoean,
                        preco,
                        precooferta
                    )
                )
            } while (c.moveToNext())

        }

        db.close()
        return listaPrecoProdutos

    }


    //Metodo para inserir dados na lista do ColetadosFrigo
    fun coletadosFrigoInsert(
        filial: String,
        numdoc: String,
        codigoean: String,
        descricao: String,
        codigobarracx: String
    ): Long {

        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put("filial", filial)
        contentValues.put("numdoc", numdoc)
        contentValues.put("codigoean", codigoean)
        contentValues.put("descricao", descricao)
        contentValues.put("codigobarracx", codigobarracx)

        val res = db.insert("coletadosfrigo", null, contentValues)
        db.close()
        return res

    }

    fun frigoColetadosSelectAll(): Cursor {

        val db = this.readableDatabase
        val c = db.rawQuery("Select * From coletadosfrigo", null,)
        db.close()
        return c
    }


    //metodo para selecionar todos os dados da tabela coletadosfrigo
    fun coletadosFrigooListaSelectAll(sql: String): MutableList<FrigoColetados> {

        val db = this.readableDatabase
        val c = db.rawQuery(sql, null,)

        val listaColetadosFrigo: ArrayList<FrigoColetados> = ArrayList()

        if (c.count > 0) {

            c.moveToFirst()
            do {
                val idIndex = c.getColumnIndex("id")
                val filialIndex = c.getColumnIndex("filial")
                val numdocIndex = c.getColumnIndex("numdoc")
                val codigoeanIndex = c.getColumnIndex("codigoean")
                val descricaoIndex = c.getColumnIndex("descricao")
                val codigobarracxIndex = c.getColumnIndex("codigobarracx")


                val id = c.getInt(idIndex)
                val filial = c.getString(filialIndex)
                val numdoc = c.getString(numdocIndex)
                val codigoean = c.getString(codigoeanIndex)
                val descricao = c.getString(descricaoIndex)
                val codigobarracx = c.getString(codigobarracxIndex)

                listaColetadosFrigo.add(
                    FrigoColetados(
                        id,
                        filial,
                        numdoc,
                        codigoean,
                        descricao,
                        codigobarracx,

                        )
                )
            } while (c.moveToNext())

        }

        db.close()
        return listaColetadosFrigo

    }

    //Deletar um item especifico da lista dos Produtos coletadosCarga
    fun coletadosFrigoDelete(id: Int): Int {
        val db = this.writableDatabase
        val res = db.delete("coletadosfrigo", "id=?", arrayOf(id.toString()))
        db.close()
        return res
    }

    fun coletadosFrigoByFilial(filial:String):Cursor{

        val db = this.readableDatabase
        val c = db.rawQuery("select * from coletadosfrigo where filial = ?", arrayOf(filial))
        db.close()
        return c
    }
    fun coletadosFrigoObjetoByFilial(filial: String):FrigoColetados{
        val db = this.readableDatabase
        val c = db.rawQuery("select * from coletadosfrigo where filial = ?", arrayOf(filial))

        var coletadosByFilial = FrigoColetados()

        if (c.count > 0) {

            c.moveToFirst()

                val idIndex = c.getColumnIndex("id")
                val filialIndex = c.getColumnIndex("filial")
                val numdocIndex = c.getColumnIndex("numdoc")
                val codigoeanIndex = c.getColumnIndex("codigoean")
                val descricaoIndex = c.getColumnIndex("descricao")
                val codigobarracxIndex = c.getColumnIndex("codigobarracx")


                val id = c.getInt(idIndex)
                val filial = c.getString(filialIndex)
                val numdoc = c.getString(numdocIndex)
                val codigoean = c.getString(codigoeanIndex)
                val descricao = c.getString(descricaoIndex)
                val codigobarracx = c.getString(codigobarracxIndex)

                coletadosByFilial = FrigoColetados(id,filial,numdoc,codigoean,descricao,codigobarracx)

        }

            return  coletadosByFilial

    }

}

