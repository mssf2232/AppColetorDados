package com.informatica.mycoletor


import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.informatica.mycoletor.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: ArrayAdapter<Produtos>
    private lateinit var viewModel: MainViewModel
    private var pos: Int = -1
    private  val db = DBHelper(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.btnPesquisa.setOnClickListener{

            val intent = Intent(this,CargaInicioActivity :: class.java)

            startActivity(intent)

        }
        binding.btnInventario.setOnClickListener{

            val intent = Intent(this,InventarioInicioActivity :: class.java)

            startActivity(intent)

        }

        binding.btnImpexp.setOnClickListener{

        val intent = Intent(this, UpdateActivity :: class.java);

            startActivity(intent)


        }
        binding.btnFrigo.setOnClickListener{

            val intent = Intent(this, FrigoActivity :: class.java);

            startActivity(intent)


        }

        binding.btnPreco.setOnClickListener {

            val intent = Intent(this, PrecoActivity :: class.java)

            startActivity(intent)
        }


    }


    }