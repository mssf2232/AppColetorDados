package com.informatica.mycoletor

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.informatica.mycoletor.databinding.ActivityUpdateBinding


class UpdateActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUpdateBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.importar.setOnClickListener {

           // checkFileExists()


            val intent = Intent(this, ProgressActiviity::class.java);

            startActivity(intent)
        }
/*
        binding.Exportar.setOnClickListener {


            val intent = Intent(this, ProgressActiviity::class.java);

            startActivity(intent)

        }
*/
        binding.importarFindprice.setOnClickListener{

            val intent = Intent(this, PrecoProgressActivity:: class.java )

            startActivity(intent)


        }


    }
}