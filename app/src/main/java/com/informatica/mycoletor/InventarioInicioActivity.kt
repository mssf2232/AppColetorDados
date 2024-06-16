package com.informatica.mycoletor

import android.content.Intent
import android.os.Bundle
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import com.informatica.mycoletor.databinding.ActivityInventarioInicioBinding

private var localInventario : String =""
private var resultado : String=""
class InventarioInicioActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInventarioInicioBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInventarioInicioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnAvancar.setOnClickListener {

            val selectedRadioButtonId = binding.radiolocalinvenatrio.checkedRadioButtonId
            val area = binding.area.text.toString()
            val coleta = binding.coleta.text.toString()

            if (selectedRadioButtonId == -1 || area == "" || coleta ==""){
                Balerta("Atenção","Dados incorretos")
            }else {

                if (selectedRadioButtonId != -1) {
                    val selectedRadioButton = findViewById<RadioButton>(selectedRadioButtonId)
                    val selectedValue = selectedRadioButton.text.toString()

                    localInventario = selectedValue
                    println("valor selecionado: $selectedValue")
                    val intent = Intent(this, InventarioActivity::class.java)
                    intent.putExtra("area", area)
                    intent.putExtra("coleta", coleta)
                    intent.putExtra("localInventario", localInventario)
                    println("avancarElse: $localInventario")
                    startActivity(intent)
                }
                println("avancar: $localInventario")
            }
        }

        binding.btnVoltar.setOnClickListener{

            val i = Intent(this, MainActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
            startActivity(i)
        }

    }
}