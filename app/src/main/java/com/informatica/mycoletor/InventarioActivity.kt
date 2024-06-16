package com.informatica.mycoletor

import android.R
import android.content.DialogInterface
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import android.os.Environment
import android.view.KeyEvent
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.mlkit.vision.barcode.common.Barcode
import com.informatica.mycoletor.databinding.ActivityInventarioBinding
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class InventarioActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInventarioBinding
    private lateinit var adapter: ArrayAdapter<InventarioColetados>
    private var pos: Int = -1
    val db = DBHelper(this)

    val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC,100)

    //METODOS CAMERAS

    private val cameraPermission = android.Manifest.permission.CAMERA
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){ isGranted ->
        if(isGranted){
            startScanner()
        }
    }
    //FIM METODOS CAMERAS

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInventarioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //este modulo recupera os dados enviados pela ActivityInicioInventario
        val i = intent
        val area = i.extras?.getString("area")
        val coleta = i.extras?.getString("coleta")
        val localinventario = i.extras?.getString("localInventario")
        val primeiroCaracter = localinventario?.substring(0,1).toString()



        //Cabecalho da ActivityInventario
        val local = "INVENTÁRIO: $localinventario"
        val local2 = "Área: $area Coleta: $coleta"
        binding.cabecalho.text = local
        binding.cabecalho2.text = local2

        val listaProdutosColetados = db.coletadoInvetarioListaTodos()

        binding.codigoEAN.requestFocus()
        adapter =
            ArrayAdapter(this, R.layout.simple_list_item_1, listaProdutosColetados)
        binding.listView.adapter = adapter

        //INICIO PERMISSOES SCANNER

        /*
  *Solicita a Permissão para utilizar a Camera do smartphone
   */


        binding.btnScanner.setOnClickListener {

            requestCameraAndStartScanner()

        }


















        //FIM PERMISSOES SCANNER



        binding.codigoEAN.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if(keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP){

                binding.btnInserir.callOnClick()


                return@OnKeyListener true
            }
            false

        })

        //metodo Inserir - faz a busca na tabela produtos pelo codigo EAN

        binding.btnInserir.setOnClickListener {

            val checkqtd = binding.quantidade.text.toString()
            val checkean = binding.codigoEAN.text.toString()
            val produto = db.produtosObjectSelectByCodigoEAN(checkean)


            if (checkqtd.isEmpty()) {


                if (produto.descricao.isEmpty()) {
                    Balerta("INFORMATIVO","PRODUTO NÃO CADASTRADO")
                    toneGen.startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK,2000)
                    binding.codigoEAN.requestFocus()
                }


                if (produto.descricao.isNotBlank()) {

                    if(primeiroCaracter == "L"){
                        val Car = "UN1 "
                        binding.descricao.setText(produto.descricao)
                        binding.codigoInterno.setText(produto.codigointerno)
                        binding.embalagem.setText(Car)
                        binding.quantidade.setText("1")
                        binding.quantidade.setSelection(binding.quantidade.length())
                        binding.quantidade.setSelectAllOnFocus(true)
                        binding.quantidade.requestFocus()

                    }else{

                        binding.descricao.setText(produto.descricao)
                        binding.codigoInterno.setText(produto.codigointerno)
                        binding.embalagem.setText(produto.embalagem)
                        binding.quantidade.setText("1")
                        binding.quantidade.setSelection(binding.quantidade.length())
                        binding.quantidade.setSelectAllOnFocus(true)
                        binding.quantidade.requestFocus()

                    }

                }

            }
            if (checkqtd.isNotEmpty()){
                val caracter = primeiroCaracter
                val codigoean = binding.codigoEAN.text.toString()
                val codigointerno = binding.codigoInterno.text.toString()
                val secao = produto.secao
                val quantidade = binding.quantidade.text.toString()
                val descricao = binding.descricao.text.toString()
                val embalagem = binding.embalagem.text.toString()

                val res =
                    db.coletadosInventarioInsert(
                        caracter,
                        codigoean,
                        codigointerno,
                        secao,
                        descricao,
                        embalagem,
                        quantidade
                    )
                if (res > 0) {

                    Toast.makeText(
                        applicationContext,
                        "Produto Cadastrado com sucesso.",
                        Toast.LENGTH_SHORT
                    ).show()
                    listaProdutosColetados.add(
                        InventarioColetados(
                            res.toInt(),
                            caracter,
                            codigoean,
                            codigointerno,
                            secao,
                            descricao,
                            embalagem,
                            quantidade,
                        )
                    )
                    limpar()
                    adapter.notifyDataSetChanged()


                } else {

                    Balerta("INFORMATIVO","ERRO AO INSERIR ITEM NA LISTA")
                    toneGen.startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK,2000)
                }

                db.close()
            }

        }

        binding.btnLimpar.setOnClickListener {
            limpar()
        }

        binding.btnDelete.setOnClickListener {

            if (pos >= 0) {

                val id = listaProdutosColetados[pos].id

                var res = db.coletadosCargaDelete(id)

                if (res >= 0) {
                    listaProdutosColetados.removeAt(pos)
                    adapter.notifyDataSetChanged()
                    limpar()
                    Balerta("INFORMATIVO","PRODUTO EXCLUÍDO DA LISTA")

                } else {
                    toneGen.startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK,2000)
                    Balerta("INFORMATIVO", "ERRO AO EXCLUIR PRODUTO DA LISTA")
                       }

            }

        }
        binding.btnIniciar.setOnClickListener {

            showQuestionDialog(listaProdutosColetados)

        }

        binding.listView.setOnItemClickListener { _, _, position, _ ->
            binding.codigoEAN.setText(listaProdutosColetados[position].codigoean)
            binding.codigoInterno.setText(listaProdutosColetados[position].codigointerno)
            binding.descricao.setText(listaProdutosColetados[position].descricao)
            binding.embalagem.setText(listaProdutosColetados[position].embalagem)
            binding.quantidade.setText(listaProdutosColetados[position].quantidade)
            pos = position
        }
        binding.btnExportar.setOnClickListener {
            val areace = area.toString()
            val areac = areace.padStart(2,'0')
            val coletac = coleta.toString()
            exportar(areac,coletac,primeiroCaracter)
        }

    }
    //Limpa os Campos da Tela de Conferencia devolvendo o Foco para o campo codigoEan
    fun limpar(){
        binding.codigoEAN.setText("")
        binding.descricao.setText("")
        binding.codigoInterno.setText("")
        binding.embalagem.setText("")
        binding.quantidade.setText("")
        binding.codigoEAN.requestFocus()

    }

    fun showQuestionDialog(listarProdutosConsultaPreco :ArrayList<InventarioColetados>){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Atenção")
        builder.setMessage("Tem Certeza? Ira excluir tudo.")

        builder.setPositiveButton("SIM"){ dialogInterface : DialogInterface, _: Int ->

            val db = DBHelper(this)
            listarProdutosConsultaPreco.removeAll(listarProdutosConsultaPreco)
            adapter.notifyDataSetChanged()
            db.limparTabelaInventarioColetado()
            binding.codigoEAN.requestFocus()
            dialogInterface.dismiss()
        }

        builder.setNegativeButton("NÃO"){ dialogInterface: DialogInterface, _: Int ->

            dialogInterface.dismiss()
        }

        val dialog = builder.create()
        dialog.show()

    }

    /*
Função para Exportar a lista dos produtos Coletados no Inventario
 */
    fun exportar(area : String, coleta:String, p:String): List<InventarioColetados> {
        val db2 = DBHelper(this)
        val listConferencia = db2.coletadoInvetarioListaTodos()
        val data = LocalDateTime.now()
        val dataFormatada = DateTimeFormatter.ofPattern("ddMMyyyyHHmm")
        val formatada = data.format(dataFormatada)
        val fileName = "$p-$area-$formatada.txt"
        val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        val path = "$directory/inventario"
        Files.createDirectories(Paths.get(path))
        val file = File(path, fileName)
        file.bufferedWriter().use { writer ->
                val line1 = "A0${area}C${coleta}\r\n"
                writer.write(line1)

                listConferencia.forEach { product ->
                    val line =
                        "${product.caracter.padEnd(1)}${product.codigoean.padEnd(14,' ')}${product.codigointerno.padEnd(6)}${product.secao.padStart(2,' ')}${
                            product.descricao.padEnd(22)
                        }${product.embalagem.padEnd(5)}${product.quantidade.padStart(5, '0')}\r\n"
                    writer.write(line)

                }
                Toast.makeText(
                    applicationContext,
                    "Arquivo do Inventario Exportado com Sucesso /Documents/Inventario",
                    Toast.LENGTH_SHORT
                ).show()


            return listConferencia
        }
    }


    /*
    Com o codigoEan digitado a função busca o Ean na tabela Produtos
    caso encontre, o campos serão preenchidos.
    passando o Foco para o campo quantidade.
     */
    fun checarEanScan(checkean : String){

        val db3 = DBHelper(this)
        val produto = db3.produtosObjectSelectByCodigoEAN(checkean)

        if (produto.descricao.isEmpty()) {
            toneGen.startTone(ToneGenerator.TONE_CDMA_PIP,2000)
            binding.codigoEAN.setSelection(binding.codigoEAN.getText().length)
            binding.codigoEAN.requestFocus()
            Balerta("INFORMATIVO","PRODUTO NÃO CADASTRADO")

        }else {
            binding.descricao.setText(produto.descricao)
            binding.codigoInterno.setText(produto.codigointerno)
            binding.embalagem.setText(produto.embalagem)

            binding.quantidade.setText("1")
            binding.quantidade.setSelection(binding.quantidade.getText().length)
            binding.quantidade.setSelectAllOnFocus(true)
            binding.quantidade.requestFocus()
        }

    }
    private fun startScanner(){
        ScannerActivity.startScanner(this){barcodes ->
            barcodes.forEach{barcode ->
                when(barcode.valueType){
                    Barcode.TYPE_URL -> {
                        binding.codigoEAN.setText( barcode.url?.url)


                    }

                    else -> {
                        binding.codigoEAN.setText(barcode.rawValue.toString())
                        val checkean = barcode.rawValue.toString()
                        checarEanScan(checkean)



                    }
                }

            }

        }
    }
    private fun requestCameraAndStartScanner(){

        if(isPermissionGranted(cameraPermission)){
            startScanner()
        }else{
            requestCameraPermission()
        }

    }
    private fun requestCameraPermission(){

        when{
            shouldShowRequestPermissionRationale(cameraPermission) -> {
                cameraPermissionRequest {
                    openPermissionSetting()
                }
            }else -> {
            requestPermissionLauncher.launch(cameraPermission)
        }
        }

    }


}