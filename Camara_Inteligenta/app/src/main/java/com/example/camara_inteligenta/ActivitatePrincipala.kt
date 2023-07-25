package com.example.camara_inteligenta

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.camara_inteligenta.databinding.ActivitatePrincipalaBinding
import android.content.Intent
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch

class ActivitatePrincipala : AppCompatActivity() {
    private lateinit var legatura : ActivitatePrincipalaBinding
    private var mAdaptor: AdaptorProdus? = null

    override fun onCreate(stareSalvata: Bundle?) {
        super.onCreate(stareSalvata)
        legatura = ActivitatePrincipalaBinding.inflate(layoutInflater)
        setContentView(legatura.root)

        legatura.butonAdaugare.setOnClickListener {
            val scop = Intent(this, AdaugaActivitate::class.java)
            startActivity(scop)
        }
    }

    private fun seteazaAdaptor(lista: List<Produs>) {
            mAdaptor?.seteazaData(lista)
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            val listaProduse = BazaDeDate( this@ActivitatePrincipala).primesteProdusDao().primesteToateProdusele()

            mAdaptor = AdaptorProdus()
            legatura.recycleView.apply {
                layoutManager = LinearLayoutManager (this@ActivitatePrincipala)
                adapter = mAdaptor
                seteazaAdaptor(listaProduse)



                    mAdaptor?.ActiuneEditListener {
                        val scop = Intent( this@ActivitatePrincipala, AdaugaActivitate::class.java )
                        scop.putExtra("Data", it)
                        startActivity(scop)
                    }

                    mAdaptor?.ActiuneDeleteListener {
                        val constructor = AlertDialog.Builder( this@ActivitatePrincipala)
                        constructor.setMessage("Esti sigur ca doresti sa stergi acest produs?")
                        constructor.setPositiveButton("DA") {p0, p1 ->
                            lifecycleScope.launch {
                                BazaDeDate( this@ActivitatePrincipala).primesteProdusDao().stergeProdus(it)
                                val lista = BazaDeDate( this@ActivitatePrincipala).primesteProdusDao().primesteToateProdusele()
                                seteazaAdaptor(lista)
                            }
                            p0.dismiss()
                        }

                        constructor.setNegativeButton("NU"){p0, p1 ->
                            p0.dismiss()
                        }

                        val dialog = constructor.create()
                        dialog.show()

                    }

            }

        }

    }

}