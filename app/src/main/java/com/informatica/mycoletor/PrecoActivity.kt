package com.informatica.mycoletor

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
import com.informatica.mycoletor.databinding.ActivityFindPriceBinding
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PrecoActivity : AppCompatActivity() {

   private  lateinit var binding: ActivityFindPriceBinding
    private lateinit var adapterResearch: ArrayAdapter<PrecoColetados>
    private lateinit var viewModel: MainViewModel
    private var pos: Int = -1
    private val cameraPermission = android.Manifest.permission.CAMERA
    val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC,100)



    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                startScanner()

            }

        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFindPriceBinding.inflate(layoutInflater)
        binding.codigoEANPrice.requestFocus()
        setContentView(binding.root)

        val db = DBHelper(this)
        val listarPriceResearch = db.PrecosColetadosListaTodos()



        adapterResearch = ArrayAdapter(this, android.R.layout.simple_list_item_1, listarPriceResearch)
        binding.listView7.adapter = adapterResearch

        binding.listView7.setOnItemClickListener { _, _, position, _ ->
            binding.codigoEANPrice.setText(listarPriceResearch[position].codigoean)
            binding.codigoInternoPrice.setText(listarPriceResearch[position].codigointerno)
            binding.descricaoPrice.setText(listarPriceResearch[position].descricao)
            binding.price.setText(listarPriceResearch[position].preco)
           // binding.quantidadePrice.setText(listarPriceResearch[position].quantidade)
            pos = position
        }
        binding.btnScanner.setOnClickListener {

            requestCameraAndStartScanner()

        }

        binding.btnLimpar.setOnClickListener{

            limpar()
        }
        binding.btnExportar.setOnClickListener{

            exportar()
        }

        binding.btnDelete.setOnClickListener{
            if(pos >=0){
                val id = listarPriceResearch[pos].id
                var res = db.livroPrecosDelete(id)
                if(res > 0 ){

                    Balerta("INFORMATIVO","PRODUTO EXCLUIDO DA LISTA DE PREÇOS")
                    listarPriceResearch.removeAt(pos)
                    adapterResearch.notifyDataSetChanged()
                    limpar()
                }else{
                    toneGen.startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK,2000)
                    Balerta("INFORMATIVO","ERRO AO EXCLUIR PRODUTOS DA LISTA DE PREÇOS")
                }

            }
        }



        binding.codigoEANPrice.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if(keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP){
                Balerta("EAN COLETADO",binding.codigoEANPrice.text.toString())
                checarEanScan(binding.codigoEANPrice.text.toString())

                //binding.btnInserir.callOnClick()


                return@OnKeyListener true
            }
            false

        })




        binding.btnInserir.setOnClickListener {

            val checkqtd = binding.quantidadePrice.text.toString()
            val checkean = binding.codigoEANPrice.text.toString()
            Inserir(checkqtd,checkean,listarPriceResearch)
        }



        binding.btnIniciar.setOnClickListener {

            showQuestionDialog(listarPriceResearch)
        }

    }
    fun limpar() {
        binding.codigoEANPrice.setText("")
        binding.descricaoPrice.setText("")
        binding.codigoInternoPrice.setText("")
        binding.price.setText("")
        binding.quantidadePrice.setText("")
        binding.codigoEANPrice.requestFocus()
    }
    private fun startScanner() {
        ScannerActivity.startScanner(this) { barcodes ->
            barcodes.forEach { barcode ->
                when (barcode.valueType) {
                    Barcode.TYPE_URL -> {
                        binding.codigoEANPrice.setText(barcode.url?.url)
                    }
                    else -> {
                        binding.codigoEANPrice.setText(barcode.rawValue.toString())
                        val checkean = barcode.rawValue.toString()


                        checarEanScan(checkean)
                    }
                }
            }
        }
    }
    private fun requestCameraAndStartScanner() {
        if (isPermissionGranted(cameraPermission)) {
            startScanner()
        } else {
            requestCameraPermission()
        }
    }
    private fun requestCameraPermission() {
        when {
            shouldShowRequestPermissionRationale(cameraPermission) -> {
                cameraPermissionRequest {
                    openPermissionSetting()
                }
            }
            else -> {
                requestPermissionLauncher.launch(cameraPermission)
            }
        }
    }

    //METOD PRECO CAMERA
    fun checarEanScan(checkean: String) {
        val db3 = DBHelper(this)
        val findPrice = db3.livroPrecoObjectSelectByCodigoEAN(checkean)
        if (findPrice.descricao.isEmpty()) {
            toneGen.startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK,2000)
            Balerta("INFORMATIVO", "PRODUTO NÃO CADASTRADO")
            binding.codigoEANPrice.setSelection(binding.codigoEANPrice.length())
            binding.codigoEANPrice.requestFocus()
        }else {


            binding.descricaoPrice.setText(findPrice.descricao)
            binding.codigoInternoPrice.setText(findPrice.codigointerno)
            binding.quantidadePrice.setText("1")
            binding.quantidadePrice.setSelection(binding.quantidadePrice.length())
            binding.quantidadePrice.requestFocus()
            //validaPreco(checkean)

            println("id " + findPrice.id)
            println("codigoean " + findPrice.codigoean)
            println("embalagem " + findPrice.embalagem)
            println("codiginterno " + findPrice.codigointerno)
            println ("Preco " + findPrice.preco)
            println ("Descricao " + findPrice.descricao)
            println("PRECO ENVIADOantes  $findPrice.preco")
            val resu = converterDecimal(findPrice.preco).toString()
            println("PRECO ENVIADOdepois  $findPrice.preco")
            println("PRECO CONVERTIDO $resu")
            //binding.price.setText(findPrice.preco)
            binding.price.setText(resu.toString())








        }
    }

    fun converterDecimal(preco: String): String {
        val numeroInteiro = preco.dropWhile { it == '0' }
        val parteDecimal =
            if (numeroInteiro.length <= 2) "0$numeroInteiro" else numeroInteiro.takeLast(2)
        val parteInteira = if (numeroInteiro.length <= 2) "0" else numeroInteiro.dropLast(2)
        return "$parteInteira,$parteDecimal"
    }

    fun exportar(): List<PrecoColetados>{
        val db2 = DBHelper(this)
        val listConferencia = db2.PrecosColetadosListaTodos()
        val data = Date()
        val dataformatada = data.dateToString().toString()
        val fileName = "coletor$dataformatada.txt"
        val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        val path = "$directory/preco"
        Files.createDirectories(Paths.get(path))
        val file = File(path, fileName)
        file.bufferedWriter().use { writer ->
            listConferencia.forEach { product ->
                val line = "${product.caracter.padEnd(1)}${product.codigoean.padEnd(14)}" +
                "${product.codigointerno.padEnd(6)}" +
                "XX" + "${product.descricao.padEnd(14)}" +
                "${product.embalagem.padEnd(5)}" +
                "${product.preco.padEnd(5)}\r\n"
                println(line)
                writer.write(line)

            }
            toneGen.startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK,2000)
            Balerta("INFORMATIVO","ARQUIVO EXPORTADO\n /DOCUMENTS/PRECO/\nCOLETORDDMMAAAAHHMM.TXT")
        }
        return listConferencia
    }

    fun Inserir(checkqtd : String, checkean: String, PrecoColetados : ArrayList<PrecoColetados> ){

        val db3 =DBHelper(this)
        val  listarPriceResearch = PrecoColetados

        if (checkqtd.isEmpty()) {
            val produto = db3.livroPrecoObjectSelectByCodigoEAN(checkean)

            if (produto.descricao.isEmpty()) {
                toneGen.startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK,2000)
               Balerta("INFORMATIVO","PRODUTO NÃO CADASTRADO")
                binding.codigoEANPrice.requestFocus()

            }else{

                binding.codigoEANPrice.setText(produto.codigoean)
                binding.descricaoPrice.setText(produto.descricao)

               // if (produto.precooferta != "00000") {
               //     val prec = converterDecimal(produto.precooferta).toString()
               //     binding.price.setText("R$ $prec")
               //     binding.codigoInternoPrice.setText(produto.codigointerno)
               //     binding.quantidadePrice.setText("1")
               //     binding.quantidadePrice.setSelection(binding.quantidadePrice.length())
               //     binding.quantidadePrice.requestFocus()
               //     } else {
                    val resu = converterDecimal(produto.preco)
                    binding.price.setText("R$ $resu")
                    binding.codigoInternoPrice.setText(produto.codigointerno)
                    binding.quantidadePrice.setText("1")
                    binding.quantidadePrice.setSelection(binding.quantidadePrice.length())
                    binding.quantidadePrice.requestFocus()

                //}
            }
        }


        if (checkqtd.isNotEmpty()) {
            val produto = db3.livroPrecoObjectSelectByCodigoEAN(checkean)
            val caracter = "P"
            val codigoean = binding.codigoEANPrice.text.toString()
            val codigointerno = binding.codigoInternoPrice.text.toString()
            val quantidade = binding.quantidadePrice.text.toString()
            val embalagem = produto.embalagem
            val descricao = binding.descricaoPrice.text.toString()

            //val preco = binding.price.text.toString()
            val preco = produto.preco
            val res =
                db3.livroPrecoInsert(
                    caracter,
                    codigoean,
                    codigointerno,
                    descricao,
                    embalagem,
                    preco,
                    quantidade
                )
            if (res > 0) {
                Toast.makeText(
                    applicationContext,
                    "Produto Cadastrado com sucesso.",
                    Toast.LENGTH_SHORT
                ).show()
                listarPriceResearch.add(
                    PrecoColetados(
                        res.toInt(),
                        caracter,
                        codigoean,
                        codigointerno,
                        descricao,
                        preco,
                        quantidade,
                    )
                )

                adapterResearch.notifyDataSetChanged()
                limpar()

            } else {
                toneGen.startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK,2000)
              Balerta("INFORMATIVO","ERRO AO ADICIONAR PRODUTO Á LISTA")
            }
            db3.close()
        }
    }







    fun validaPreco(checkean: String){

        val db3 = DBHelper(this)

        val produto = db3.livroPrecoObjectSelectByCodigoEAN(checkean)

    //    if (produto.precooferta != "00000") {
    //        val prec = converterDecimal(produto.precooferta).toString()
    //        binding.price.setText(prec)
            /*
            binding.codigoInternoPrice.setText(produto.codigointerno)
            binding.quantidadePrice.setText("1")
            binding.quantidadePrice.setSelection(binding.quantidadePrice.length())
            binding.quantidadePrice.requestFocus()*/

    //    } else {
        println("id " + produto.id)
        println("codigoean " + produto.codigoean)
        println("embalagem " + produto.embalagem)
        println("codiginterno " + produto.codigointerno)
        println ("Preco " + produto.preco)
        println ("Descricao " + produto.descricao)
        println("PRECO ENVIADOantes  $produto.preco")
        val resu = converterDecimal(produto.preco).toString()
        println("PRECO ENVIADOdepois  $produto.preco")
        println("PRECO CONVERTIDO $resu")
        binding.price.setText(resu.toString())
            /*
            binding.codigoInternoPrice.setText(produto.codigointerno)
            binding.quantidadePrice.setText("1")
            binding.quantidadePrice.setSelection(binding.quantidadePrice.length())
            binding.quantidadePrice.requestFocus()*/

      //  }

           }
    //Metodo que recupera a data do Sistema e adiciona ao nome do arquivo
    fun Date.dateToString(format: String = "ddMMyyyyHHmm"): String {
        val formatter = SimpleDateFormat(format, Locale.getDefault())
        return formatter.format(this)
    }


    fun showQuestionDialog(precosColetados:ArrayList<PrecoColetados>){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Atenção")
        builder.setMessage("Tem Certeza? Ira excluir tudo.")

        builder.setPositiveButton("SIM"){ dialogInterface : DialogInterface, _: Int ->

            val db = DBHelper(this)
            precosColetados.removeAll(precosColetados)
            adapterResearch.notifyDataSetChanged()
            db.limparTabelaLivroPrecosColetados()
            binding.codigoEANPrice.requestFocus()
            dialogInterface.dismiss()
        }

        builder.setNegativeButton("NÃO"){ dialogInterface: DialogInterface, _: Int ->

            dialogInterface.dismiss()
        }

        val dialog = builder.create()
        dialog.show()

    }


}