package com.informatica.mycoletor


import android.content.DialogInterface
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import android.os.Environment
import android.view.KeyEvent
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.informatica.mycoletor.databinding.ActivityFrigoBinding
import androidx.core.widget.addTextChangedListener
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FrigoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFrigoBinding
    val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
    private lateinit var adapter: CustomArrayAdapter
    private var pos: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFrigoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val db = DBHelper(this)

        val listaFrigoColetados =
            db.coletadosFrigooListaSelectAll("Select * From coletadosfrigo order by id desc ")

        val adapter = CustomArrayAdapter(this, listaFrigoColetados)

        binding.listView.adapter = adapter


        //Reponsavel  pelo preenchimento dos campos acima da lista
        binding.listView.setOnItemClickListener { _, _, position, _ ->
            binding.filial.setText(listaFrigoColetados[position].filial)
            binding.ndoc.setText(listaFrigoColetados[position].numdoc)
            binding.ean.setText(listaFrigoColetados[position].codigoean)
            binding.descricao.setText(listaFrigoColetados[position].descricao)
            binding.codigoBarraCx.setText(listaFrigoColetados[position].codigobarra)
            pos = position

        }

        binding.btnLimpar.setOnClickListener {

            limpar()

        }

        binding.btnIniciar.setOnClickListener {

            val builder = AlertDialog.Builder(this)
            builder.setTitle("Atenção")
            builder.setMessage("Tem Certeza? Ira excluir tudo.")
            builder.setPositiveButton("SIM") { dialogInterface: DialogInterface, _: Int ->
                val db = DBHelper(this)
                db.limparTabela("coletadosfrigo")
                adapter.clearItems()
                limpar()
                binding.filial.requestFocus()
                dialogInterface.dismiss()


            }

            builder.setNegativeButton("NÃO") { dialogInterface: DialogInterface, _: Int ->

                dialogInterface.dismiss()


            }

            val dialog = builder.create()
            dialog.show()


        }

        //Funcao responsavel por preencher a descricao EAN RMS
        binding.ean.addTextChangedListener { text ->
            val eanrms = binding.ean.text.toString()
            val item = db.produtosObjectSelectByCodigoEAN(eanrms)
            val newText = item.descricao
            binding.descricao.setText(newText)
        }


        binding.codigoBarraCx.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                binding.btnInserir.callOnClick()
                return@OnKeyListener true
            }
            false
        })

        binding.btnExportar.setOnClickListener {
            exportar()
        }

        binding.btnDelete.setOnClickListener {

            if (pos >= 0) {

                val id = listaFrigoColetados[pos].id

                var res = db.coletadosFrigoDelete(id)

                if (res >= 0) {
                    Balerta("INFORMATIVO", "PRODUTO EXCLUÍDA DA LISTA DE COLETADOS")
                    Toast.makeText(
                        applicationContext,
                        "PRODUTO EXCLUÍDA DA LISTA DE COLETADOS",
                        Toast.LENGTH_SHORT
                    ).show()
                    listaFrigoColetados.removeAt(pos)
                    adapter.notifyDataSetChanged()

                    limpar()

                } else {
                    Balerta("INFORMATIVO", "ERRO AO EXCLUÍR PRODUTO DA LISTA DE COLETADOS")
                    Toast.makeText(
                        applicationContext,
                        "ERRO AO EXCLUÍR PRODUTO DA LISTA DE COLETADOS",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }
        }

        binding.btnInserir.setOnClickListener {
            var filial = binding.filial.text.toString()
            val numdoc = binding.ndoc.text.toString()
            val eanrms = binding.ean.text.toString()
            val descricao = binding.descricao.text.toString()
            val codigobarracx = binding.codigoBarraCx.text.toString()


            val valid = validaDadosEntrada(filial,numdoc, eanrms,descricao, codigobarracx)

            if (valid == true) {

                val checarEanScan = checarEanScan(eanrms)

                if (checarEanScan == true) {

                    db.coletadosFrigoInsert(filial, numdoc, eanrms, descricao, codigobarracx)
                    Toast.makeText(
                        applicationContext,
                        "Codigo de Barras Inserido na lista",
                        Toast.LENGTH_LONG
                    ).show()

                    val listaFrigoColetadosNovos =
                        db.coletadosFrigooListaSelectAll("Select * From coletadosfrigo")


                    adapter.updateItems(listaFrigoColetadosNovos)
                    binding.codigoBarraCx.setText("")
                    binding.codigoBarraCx.requestFocus()
                }


            } else {

                Balerta("informativo", "Dados Incorretos")


            }
        }


    }

    fun validaDadosEntrada(
        filial: String,
        numdoc: String,
        eanrms: String,
        descricao: String,
        codigobarracx: String
    ): Boolean {

        val filiais =
            arrayOf("2", "3", "5", "6", "7", "8", "11", "12", "13", "17", "18", "20", "21", "23")
        val findFilial = filiais.find { it == filial }
        if (filial == "" || eanrms == "" || numdoc == "" || descricao == "" || codigobarracx == "" || findFilial == null) {

            return false
        }

        return true

    }

    fun checarEanScan(checkean: String): Boolean {

        val db3 = DBHelper(this)
        val produto = db3.produtosObjectSelectByCodigoEAN(checkean)

        if (produto.descricao.isEmpty()) {
            toneGen.startTone(ToneGenerator.TONE_CDMA_PIP, 2000)
            binding.ean.setSelection(binding.ean.getText().length)
            binding.ean.requestFocus()
            Balerta("INFORMATIVO", "PRODUTO NÃO CADASTRADO")

            return false

        }

        return true

    }

    fun limpar() {
        binding.filial.setText("")
        binding.ndoc.setText("")
        binding.ean.setText("")
        binding.descricao.setText("")
        binding.codigoBarraCx.setText("")
        binding.filial.requestFocus()
    }

    fun exportar() {

        val db = DBHelper(this)
        val listaColetadosFrigo = db.coletadosFrigooListaSelectAll("select * from coletadosfrigo")
        var produtos = Produtos()
        var coletadosFrigo = FrigoColetados()
        val data = Date()
        val dataFormatada = data.dateToString()
        val fileName = "frigo$dataFormatada.txt"
        val directory =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        val path = "$directory/frigo"
        Files.createDirectories(Paths.get(path))
        val file = File(path, fileName)


        file.bufferedWriter().use { writer ->

            val agrupadosPorEan = listaColetadosFrigo.groupBy { it.codigoean }
            val agrupadosPorFilail = listaColetadosFrigo.groupBy { it.filial }

            for((filial) in agrupadosPorFilail) {

                coletadosFrigo = db.coletadosFrigoObjetoByFilial(filial)

                    val fil = "FILIAL-"
                    val doc = "DOC-"
                    val numFilial = coletadosFrigo.filial
                    val numDoc = coletadosFrigo.numdoc

                    if (numFilial.length < 2 || numDoc.length < 2) {

                        val numF= numFilial.padStart(2, '0')
                        val docF = numDoc.padStart(2, '0')
                        val line = "${fil}${numF}${doc}${docF}\r\n"
                        writer.write(line)

                    }else{

                        val line = "${fil}${numFilial}${doc}${numDoc}\r\n"
                        writer.write(line)
                    }

                          for ((codigoEan, produtoAgrupados) in agrupadosPorEan) {

                            produtos = db.produtosObjectSelectByCodigoEAN(codigoEan)
                            val desc = produtos.descricao
                            val EanRms: String = "EANRMS-"
                            val line =
                                "${EanRms.padEnd(17)}${codigoEan}${desc}\r\n"
                            writer.write(line)

                            println("codigo ean $codigoEan - $desc")
                            produtoAgrupados.forEach { FrigoColetados ->

                                println("codigo de barra ${FrigoColetados.codigobarra}")

                                val line =
                                    "PELOLI-" + "${FrigoColetados.codigobarra}\r\n"
                                writer.write(line)

                            }


                    }

            }

        }
        Toast.makeText(
            applicationContext,
            "Arquivo Carga.txt Exportado com Sucesso para Pasta Documents/frigo",
            Toast.LENGTH_LONG
        ).show()
    }
}


    //Metodo que recupera a data do Sistema e adiciona ao nome do arquivo
    fun Date.dateToString(format: String = "ddMMyyyyHHmm"): String {
        val formatter = SimpleDateFormat(format, Locale.getDefault())
        return formatter.format(this)
    }


