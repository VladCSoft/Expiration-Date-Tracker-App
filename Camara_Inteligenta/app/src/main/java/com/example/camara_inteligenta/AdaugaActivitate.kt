package com.example.camara_inteligenta

import android.Manifest
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.camara_inteligenta.databinding.AdaugareActivitateBinding
import kotlinx.coroutines.launch
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class AdaugaActivitate : AppCompatActivity() {
    private lateinit var legatura: AdaugareActivitateBinding
    private var produs: Produs? = null

    private val CAMERA_REQUEST_CODE = 123
    private val CAMERA_PERMISSION_REQUEST_CODE = 456

    private val canalId = "canal_expirare"

    companion object {
        const val NOTIFICARE_ID_INAINTE_CU_O_ZI = 1
        const val NOTIFICARE_ID_INAINTE_CU_SAPTE_ZILE = 2
    }

    override fun onCreate(stareSalvata: Bundle?) {
        super.onCreate(stareSalvata)
        creareCanalNotificare()
        legatura = AdaugareActivitateBinding.inflate(layoutInflater)
        setContentView(legatura.root)

        legatura.edDataExpirare.setOnClickListener {
            afisareAlegereData()
        }

        produs = intent.getSerializableExtra("Data") as? Produs

        if (produs == null) legatura.butonAdaugareActualizareProdus.text = "Adauga Produs"
        else {
            legatura.butonAdaugareActualizareProdus.text = "Actualizeaza"
            legatura.edNumeProdus.setText(produs?.NumeProdus.toString())
            legatura.edDataExpirare.setText(produs?.DataExpirare.toString())
        }

        legatura.icCamera.setOnClickListener { verificarePermisiuneCamera() }
        legatura.butonAdaugareActualizareProdus.setOnClickListener { adaugaProdus() }
    }

    private fun creareCanalNotificare() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                canalId,
                "Canal Expirare",
                NotificationManager.IMPORTANCE_HIGH
            )
            val notificareManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificareManager.createNotificationChannel(canal)
        }
    }

    private fun afisareAlegereData() {
        val dataCurenta = Calendar.getInstance()
        val an = dataCurenta.get(Calendar.YEAR)
        val luna = dataCurenta.get(Calendar.MONTH)
        val zi = dataCurenta.get(Calendar.DAY_OF_MONTH)

        val alegereDataDialog = DatePickerDialog(
            this,
            { _, anSelectat, lunaSelectata, ziSelectata ->
                val formattedDate = String.format("%02d-%02d-%04d", ziSelectata, lunaSelectata + 1, anSelectat)
                legatura.edDataExpirare.setText(formattedDate)
            },
            an,
            luna,
            zi
        )

        alegereDataDialog.show()
    }

    private fun verificarePermisiuneCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            deschideCamera()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun deschideCamera() {
        val cameraScop = Intent(this, ActCaptura::class.java)
        startActivityForResult(cameraScop, CAMERA_REQUEST_CODE)
    }

    private fun adaugaProdus() {
        val numeProdus = legatura.edNumeProdus.text.toString()
        val dataExpirare = legatura.edDataExpirare.text.toString()

        // Check if the dataExpirare is empty or not in the correct date format
        if (dataExpirare.isEmpty() || !validareData(dataExpirare)) {
            legatura.edDataExpirare.error = "Introdu o data in formatul DD-MM-YYYY!"
            return
        }

        val produsCodDeBare = ProduseCodDeBare(this)
        val produsDataExpirare = produsCodDeBare.primesteProdus(dataExpirare)
        if (produsDataExpirare != null) {
            legatura.edDataExpirare.setText(produsDataExpirare)
        }

        lifecycleScope.launch {
            val produsDataExpirare = ProduseCodDeBare(this@AdaugaActivitate).primesteProdus(dataExpirare)
            if (produsDataExpirare != null) {
                legatura.edDataExpirare.setText(produsDataExpirare)
            }

            if (produs == null) {
                // Add the new product
                val produs = Produs(NumeProdus = numeProdus, DataExpirare = dataExpirare)
                val produsDao = BazaDeDate(this@AdaugaActivitate).primesteProdusDao()
                val produsId = produsDao.adaugaProdus(produs)
                planificaNotificare(produsId, dataExpirare, numeProdus)
                finish()
            } else {
                val p = Produs(numeProdus, dataExpirare)
                p.id = produs?.id ?: 0
                BazaDeDate(this@AdaugaActivitate).primesteProdusDao().actualizeazaProdus(p)
                finish()
            }
        }
    }

    private fun planificaNotificare(produsId: Long, dataNotificare: String, numeProdus: String) {
        val dataExpirare = convertesteSirLaData(dataNotificare)
        val oZiInainteDeExpirare = primesteDataNotificare(dataExpirare, -1)
        val sapteZileInainteDeExpirare = primesteDataNotificare(dataExpirare, -7)

        planificareNotificareUnica(
            produsId,
            oZiInainteDeExpirare,
            NOTIFICARE_ID_INAINTE_CU_O_ZI,
            numeProdus
        )

        planificareNotificareUnica(
            produsId,
            sapteZileInainteDeExpirare,
            NOTIFICARE_ID_INAINTE_CU_SAPTE_ZILE,
            numeProdus
        )
    }


    private fun planificareNotificareUnica(produsId: Long, dataNotificare: Date, notificareId: Int, numeProdus: String) {
        val scop = Intent(this, PrimireNotificare::class.java)
        scop.putExtra("ProdusId", produsId)
        scop.putExtra("NotificationId", notificareId)
        scop.putExtra("ProductName", numeProdus) // Pass the product name

        scop.putExtra("NotificationIdBeforeOneDay", NOTIFICARE_ID_INAINTE_CU_O_ZI)
        scop.putExtra("NotificationIdBeforeSevenDays", NOTIFICARE_ID_INAINTE_CU_SAPTE_ZILE)

        val scopAsteptare = PendingIntent.getBroadcast(
            this,
            notificareId,
            scop,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val alarmaManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmaManager.setExact(
            AlarmManager.RTC_WAKEUP,
            dataNotificare.time,
            scopAsteptare
        )
    }

    private fun convertesteSirLaData(sirData: String): Date {
        val formatData = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        return formatData.parse(sirData) ?: Date()
    }

    private fun primesteDataNotificare(data: Date, zileInainte: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.time = data
        calendar.add(Calendar.DAY_OF_YEAR, zileInainte)
        return calendar.time
    }


    private fun validareData(sirData: String): Boolean {
        val dataFormat = "dd-MM-yyyy" // Customize the date format according to your requirements
        val sdf = SimpleDateFormat(dataFormat, Locale.getDefault())
        sdf.isLenient = false

        return try {
            sdf.parse(sirData)
            true
        } catch (e: ParseException) {
            false
        }
    }

    override fun onRequestPermissionsResult(
        requestCode : Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            deschideCamera()
        }
    }

    override fun onActivityResult(codSolicitat: Int, codRezultat: Int, data: Intent?) {
        super.onActivityResult(codSolicitat, codRezultat, data)
        if (codSolicitat == CAMERA_REQUEST_CODE && codRezultat == RESULT_OK) {
            val codbare = data?.getStringExtra("SCAN_RESULT") ?: ""
            legatura.edDataExpirare.setText(codbare)

            val produs = ProduseCodDeBare(this).primesteProdus(codbare)
            produs?.let {
                legatura.edDataExpirare.setText(it)
            }
        }
    }
}