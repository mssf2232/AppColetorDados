package com.informatica.mycoletor

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.informatica.mycoletor.databinding.ActivityCargaInicioBinding

class CargaInicioActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCargaInicioBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCargaInicioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnavancarcarga.setOnClickListener {

            var filial = binding.filial.text.toString()
            val placa = binding.placa.text.toString()
            val filiais = arrayOf("2","3","5","6","7","8","11","12","13","17","18","20","21","23")
            val findFilial = filiais.find {it == filial}

            if (filial == "" || placa =="" || findFilial == null){
                Balerta("Atenção","Dados incorretos")
            }else {
                val intent = Intent(this, CargaActivity::class.java)
                intent.putExtra("filial", filial)
                intent.putExtra("placa", placa)
                startActivity(intent)
            }
        }

        binding.btnVoltarCarga.setOnClickListener {

            val i = Intent(this, MainActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
            startActivity(i)


        }
    }
}