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
import androidx.lifecycle.ViewModelProvider
import com.google.mlkit.vision.barcode.common.Barcode
import com.informatica.mycoletor.databinding.ActivityCargaBinding
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class CargaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCargaBinding
    private lateinit var adapter: ArrayAdapter<ColetadosCarga>
    private lateinit var viewModel: MainViewModel
    private var pos: Int = -1
    private val cameraPermission = android.Manifest.permission.CAMERA

    val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC,100)

    /*
    *Solicita a Permissão para utilizar a Camera do smartphone
     */
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){ isGranted ->
        if(isGranted){
            startScanner()
        }
    }




    //metodo Principal da chamada da Class Android aonde faz  a interação do codigo com a tela(actvity)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCargaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        val db = DBHelper(this)
        val i = intent
        val filial = i.extras?.getString("filial")
        val placa = i.extras?.getString("placa")

       // if(filial.equals("") || filial == null || placa.equals("") || placa == null){
       //     Toast.makeText(applicationContext, "Os Campos Filias ou Placa estão Vazios", Toast.LENGTH_SHORT).show()
       //     val i = Intent(this, CargaInicioActivity::class.java)
       //     i.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
       //     startActivity(i)
       // }



        //
        val listaProdutosColetados = db.coletadoCargaListaTodos()

        binding.codigoEAN.requestFocus()

        adapter =
            ArrayAdapter(this, android.R.layout.simple_list_item_1, listaProdutosColetados)
        binding.listView.adapter = adapter

        binding.listView.setOnItemClickListener { _, _, position, _ ->
            binding.codigoEAN.setText(listaProdutosColetados[position].codigoean)
            binding.codigoInterno.setText(listaProdutosColetados[position].codigointerno)
            binding.descricao.setText(listaProdutosColetados[position].descricao)
            binding.embalagem.setText(listaProdutosColetados[position].embalagem)
            binding.quantidade.setText(listaProdutosColetados[position].quantidade)
            pos = position
        }


        binding.codigoEAN.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if(keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP){

                  binding.btnInserir.callOnClick()


            return@OnKeyListener true
        }
                false

        })



        binding.btnInserir.setOnClickListener {

            val checkqtd = binding.quantidade.text.toString()
            val checkean = binding.codigoEAN.text.toString()

            if (checkqtd.isEmpty()) {


                val produto = db.produtosObjectSelectByCodigoEAN(checkean)

                if (produto.descricao.isEmpty()) {
                    toneGen.startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK,2000)
                    Toast.makeText(applicationContext,"PRODUTO NÃO CADASTRADO", Toast.LENGTH_SHORT).show()
                    Balerta("INFORMATIVO","PRODUTO NÃO CADASTRADO")
                    binding.codigoEAN.requestFocus()

                }

                if (produto.descricao.isNotBlank()) {
                    binding.descricao.setText(produto.descricao)
                    binding.codigoInterno.setText(produto.codigointerno)
                    binding.embalagem.setText(produto.embalagem)
                    binding.quantidade.setText("1")
                    binding.quantidade.setSelection(binding.quantidade.getText().length)
                    binding.quantidade.setSelectAllOnFocus(true)
                    binding.quantidade.requestFocus()

                }

            }

                if (checkqtd.isNotEmpty()){
                    val caracter = "C"
                    val codigoean = binding.codigoEAN.text.toString()
                    val codigointerno = binding.codigoInterno.text.toString()
                    val caracterespecial = "XX"
                    val quantidade = binding.quantidade.text.toString()
                    val descricao = binding.descricao.text.toString()
                    val embalagem = binding.embalagem.text.toString()

                    val res =
                        db.coletadosCargaInsert(
                            caracter,
                            codigoean,
                            codigointerno,
                            caracterespecial,
                            descricao,
                            embalagem,
                            quantidade
                        )
                    if (res > 0) {

                        Toast.makeText(
                            applicationContext,
                            "Produto Coletado com sucesso.",
                            Toast.LENGTH_SHORT
                        ).show()
                        listaProdutosColetados.add(
                            ColetadosCarga(
                                res.toInt(),
                                caracter,
                                codigoean,
                                codigointerno,
                                caracterespecial,
                                descricao,
                                embalagem,
                                quantidade,
                            )
                        )
                        limpar()
                        adapter.notifyDataSetChanged()


                    } else {

                        Balerta("INFORMATIVO","ERRO AO INSERIR PRODUTO NA LISTA DE COLETADOS")
                        Toast.makeText(
                            applicationContext,
                            "ERRO AO INSERIR PRODUTO NA LISTA DE COLETADOS",
                            Toast.LENGTH_SHORT
                        ).show()

                    }

                    db.close()
                }

        }

        binding.btnDelete.setOnClickListener {

            if (pos >= 0) {

                val id = listaProdutosColetados[pos].id

                var res = db.coletadosCargaDelete(id)

                if (res >= 0) {
                    Balerta("INFORMATIVO","PRODUTO EXCLUÍDA DA LISTA DE COLETADOS")
                    Toast.makeText(
                        applicationContext,
                        "PRODUTO EXCLUÍDA DA LISTA DE COLETADOS",
                        Toast.LENGTH_SHORT
                    ).show()
                    listaProdutosColetados.removeAt(pos)
                    adapter.notifyDataSetChanged()

                    limpar()

                } else {
                    Balerta("INFORMATIVO","ERRO AO EXCLUÍR PRODUTO DA LISTA DE COLETADOS")
                    Toast.makeText(
                        applicationContext,
                        "ERRO AO EXCLUÍR PRODUTO DA LISTA DE COLETADOS",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }

        }

        binding.btnLimpar.setOnClickListener {
            limpar()
        }

        binding.btnExportar.setOnClickListener{
            val filia = filial.toString()
            val plac = placa.toString()

            exportar(filia,plac)
        }

        binding.btnScanner.setOnClickListener {

            requestCameraAndStartScanner()

        }

        binding.btnIniciar.setOnClickListener {

            showQuestionDialog(listaProdutosColetados)

        }

    }
    /*
    * Esta parte e limpa a lista de produtos coletados no Programa de Carga
    *
     */

    fun showQuestionDialog(listarProdutosConsultaPreco :ArrayList<ColetadosCarga>){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Atenção")
        builder.setMessage("Tem Certeza? Ira excluir tudo.")

        builder.setPositiveButton("SIM"){ dialogInterface : DialogInterface, _: Int ->

             val db = DBHelper(this)
            listarProdutosConsultaPreco.removeAll(listarProdutosConsultaPreco)
            adapter.notifyDataSetChanged()
            db.limparTabela()
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
Função para Exportar a lista dos produtos Coletados
 */
    fun exportar(filia : String, plac:String): List<ColetadosCarga> {

    val db2 = DBHelper(this)
    val listConferencia = db2.coletadoCargaListaTodos()
    val data = Date()
    val dataFormatada = data.dateToString()
    val fileName = "carga$dataFormatada.txt"
    val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
    val path = "$directory/carga"
    Files.createDirectories(Paths.get(path))
    val file = File(path, fileName)
    file.bufferedWriter().use { writer ->

        if (filia.length < 2 || plac.length < 4) {

            val fili =filia.padStart(2,'0')
            val pla = plac.padStart(4,'0')
            val line1 = "FILIAL-${fili}PLACA-${pla}\r\n"
            writer.write(line1)

            listConferencia.forEach { product ->
                val line =
                    "${product.caracter.padEnd(1)}${product.codigoean.padEnd(14)}${product.codigointerno}${product.caracterespecial}${
                        product.descricao.padEnd(22)
                    }${product.embalagem.padEnd(5)}${product.quantidade.padStart(5, '0')}\r\n"
                writer.write(line)

            }
            Toast.makeText(
                applicationContext,
                "Arquivo Carga.txt Exportado com Sucesso para Pasta Documents/carga",
                Toast.LENGTH_SHORT
            ).show()

        } else {

            val fili = filia.substring(filia.length - 2)
            val pla = plac.substring(plac.length - 4)
            val line1 = "FILIAL-${fili}PLACA-${pla}\r\n"
            writer.write(line1)

            listConferencia.forEach { product ->
                val line =
                    "${product.caracter.padEnd(1)}${product.codigoean.padEnd(14)}${product.codigointerno}${product.caracterespecial}${
                        product.descricao.padEnd(22)
                    }${product.embalagem.padEnd(5)}${product.quantidade.padStart(5, '0')}\r\n"
                writer.write(line)

            }
            Toast.makeText(
                applicationContext,
                "Arquivo Carga.txt Exportado com Sucesso para Pasta Documents/carga",
                Toast.LENGTH_SHORT
            ).show()
        }

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
            binding.quantidade.requestFocus()
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
    //Metodo que recupera a data do Sistema e adiciona ao nome do arquivo
    fun Date.dateToString(format: String = "ddMMyyyyHHmm"): String {
        val formatter = SimpleDateFormat(format, Locale.getDefault())
        return formatter.format(this)
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