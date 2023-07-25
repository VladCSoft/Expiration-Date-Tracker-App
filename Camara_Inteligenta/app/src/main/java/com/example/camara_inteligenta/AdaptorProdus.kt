package com.example.camara_inteligenta

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AdaptorProdus: RecyclerView.Adapter<AdaptorProdus.ProdusVizualizare>() {

    private var lista = mutableListOf<Produs>()
    private var actiuneActualizare: ((Produs) -> Unit)? = null
    private var actiuneStergere: ((Produs) -> Unit)? = null

    override fun onCreateViewHolder(parinte: ViewGroup, tipVizualizare: Int): ProdusVizualizare {
        val vizualizare =
            LayoutInflater.from(parinte.context)
                .inflate(R.layout.produs_vizualizare, parinte, false)

        return ProdusVizualizare(vizualizare)
    }

    override fun onBindViewHolder(recipient: ProdusVizualizare, pozitie: Int) {
        val produs = lista[pozitie]
        recipient.tvNumeProdus.text = produs.NumeProdus
        recipient.tvDataExpirare.text = produs.DataExpirare

        val dataExpirare = convertesteSirLaData(produs.DataExpirare)
        val zilePanaLaExpirare = primesteZilePanaLaExpirare(dataExpirare)

        if (zilePanaLaExpirare < 1) {
            recipient.tvExpiraCurand.apply {
                text = "Expirat"
                setTextColor(Color.RED)
            }
        } else if (zilePanaLaExpirare == 1) {
            recipient.tvExpiraCurand.apply {
                text = "Expira: $zilePanaLaExpirare Zi"
                setTextColor(Color.YELLOW)
            }
        } else if (zilePanaLaExpirare >= 7) {
            recipient.tvExpiraCurand.apply {
                text = "Expira: $zilePanaLaExpirare Zile"
                setTextColor(Color.GREEN)
            }
        } else {
            recipient.tvExpiraCurand.apply {
                text = "Expira: $zilePanaLaExpirare Zile"
                setTextColor(Color.YELLOW)
            }
        }

        recipient.actiuneActualizare.setOnClickListener { actiuneActualizare?.invoke(produs) }
        recipient.actiuneStergere.setOnClickListener { actiuneStergere?.invoke(produs) }
    }

    private fun convertesteSirLaData(sirData: String): Date {
        val formatData = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        return formatData.parse(sirData) ?: Date()
    }

    private fun primesteZilePanaLaExpirare(dataExpirare: Date): Int {
        val astazi = Calendar.getInstance().apply { time = Date() }
        val ziExpirare = Calendar.getInstance().apply { time = dataExpirare }

        // Clear time fields to compare only dates
        astazi.clear(Calendar.HOUR_OF_DAY)
        astazi.clear(Calendar.MINUTE)
        astazi.clear(Calendar.SECOND)
        astazi.clear(Calendar.MILLISECOND)

        ziExpirare.clear(Calendar.HOUR_OF_DAY)
        ziExpirare.clear(Calendar.MINUTE)
        ziExpirare.clear(Calendar.SECOND)
        ziExpirare.clear(Calendar.MILLISECOND)

        val diferenta = ziExpirare.timeInMillis - astazi.timeInMillis
        val zile = diferenta / (24 * 60 * 60 * 1000)
        return zile.toInt()
    }

    override fun getItemCount() = lista.size

    fun seteazaData (data: List<Produs>) {
        lista.apply {
            clear()
            addAll(data)
        }
        notifyDataSetChanged()
    }

    fun ActiuneEditListener (callback: (Produs) -> Unit) {
        this.actiuneActualizare = callback
    }

    fun ActiuneDeleteListener (callback: (Produs) -> Unit) {
        this.actiuneStergere = callback
    }

    class ProdusVizualizare(itemView: View): RecyclerView.ViewHolder(itemView) {
        val tvNumeProdus: TextView = itemView.findViewById(R.id.tv_nume_produs)
        val tvDataExpirare: TextView = itemView.findViewById(R.id.tv_data_expirare)
        val tvExpiraCurand: TextView = itemView.findViewById(R.id.tv_expira_curand)
        val actiuneActualizare: ImageView = itemView.findViewById(R.id.actiune_actualizare)
        val actiuneStergere: ImageView = itemView.findViewById(R.id.actiune_stergere)
    }


}