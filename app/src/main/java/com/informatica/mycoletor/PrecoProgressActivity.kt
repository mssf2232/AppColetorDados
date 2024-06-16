package com.informatica.mycoletor

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.informatica.mycoletor.databinding.ActivityProgressActivityBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException

class PrecoProgressActivity  : AppCompatActivity() {

    private lateinit var binding: ActivityProgressActivityBinding
    private lateinit var adapter: ArrayAdapter<Produtos>
    private lateinit var viewModel: MainViewModel
    private var pos: Int = -1
    private val db = DBHelper(this)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProgressActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (isStoragePermissionGranted()) {
        } else {
            requestStoragePermission()
        }

    }

    private suspend fun updateProgress(percentage: String) {
        withContext(Dispatchers.Main) {
            binding.textProgress.text = "$percentage"
        }
    }

    private suspend fun Progresso(percentage: Int) {
        withContext(Dispatchers.Main) {
            binding.textProgress.text = "$percentage %"
        }
    }


    companion object {

        private const val REQUEST_PERMISSION_CODE = 123
    }

    private fun isStoragePermissionGranted(): Boolean {

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.MANAGE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun requestStoragePermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.MANAGE_EXTERNAL_STORAGE
            ),
            REQUEST_PERMISSION_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION_CODE) {
            // Limpar Tabela ProdutosPrecos
            db.limparTabelaPrecos()
            //deleteDatabase(this)
            ler()
        }
    }

    private fun ler() {

        lifecycleScope.launch {
            showLoading(true)
            val cadProd = mutableListOf<Preco>()
            val downloadsDirectory =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
            var caminhoArquivo = File(downloadsDirectory, "/importar/LivroDePrecos.txt")
            try {
                println(caminhoArquivo)

                val totalLines = countLinesInFile(caminhoArquivo)

                println(totalLines)

                var currentLine = 0

                withContext(Dispatchers.IO) {

                    val linhas = caminhoArquivo.readLines()

                    for (linha in linhas) {
                        currentLine++
                        // Extrair os campos com base nos tamanhos especificados
                        val codigointerno = linha.substring(0, 6)
                        val descricao = linha.substring(6, 28)
                        val codigoean = linha.substring(28, 42).trim()
                        val embalagem = linha.substring(42, 47)
                        val preco = linha.substring(49,54)
                     //   println ("$codigointerno,$descricao,$codigoean,$embalagem,$preco")
                        //val descricao = linha.substring(29, 69)
                        //val preco = linha.substring(104, 109)
                        //val precooferta = linha.substring(171, 176)
                        //val codigoean = linha.substring(5, 18)
                        //val codigointerno = linha.substring(23, 29)
                        //val descricao = linha.substring(29, 69)
                        //val preco = linha.substring(104, 109)
                        //val precooferta = linha.substring(171, 176)

                        cadProd.add(Preco(codigoean,codigointerno, descricao, embalagem,preco))


                        withContext(Dispatchers.Main) {

                            updateProgress(currentLine, totalLines)
                        }


                    }
                    db.inserirCadprod(cadProd)

                }
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    showSuccessMessage()

                }

            } catch (e: FileNotFoundException) {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    showErrorMessage("Arquivo não encontrado.")
                }
            }
        }

    }

    suspend fun countLinesInFile(fileName: File): Int {
        return withContext(Dispatchers.IO) {
            val line = fileName.readLines().size
            val lineCount = line.toInt()
            lineCount
        }
    }

    private fun updateProgress(currentLine: Int, totalLines: Int) {
        binding.progressBar.max = totalLines
        binding.progressBar.progress = currentLine
        binding.textViewCounter.text = "$currentLine/$totalLines"
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.textViewCounter.visibility = if (isLoading) View.VISIBLE else View.GONE

    }
    private fun showSuccessMessage() {
        binding.textProgress.text = "Arquivo processado com sucesso!"
    }
    private fun showErrorMessage(message: String) {
        binding.textProgress.text = message
    }
}




