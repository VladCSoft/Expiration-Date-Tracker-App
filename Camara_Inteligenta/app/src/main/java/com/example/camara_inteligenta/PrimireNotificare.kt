package com.example.camara_inteligenta

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PrimireNotificare : BroadcastReceiver() {

    companion object {
        private const val channelId = "CanalExpirare"
        private const val channelName = "Canal Expirare"
        private const val notificationId = 1
    }

    override fun onReceive(context: Context, scop: Intent) {
        val produsId = scop.getLongExtra("ProdusId", -1)
        val notificareId = scop.getIntExtra("NotificationId", -1)

        val notificareIdInainteCuOZi = scop.getIntExtra("NotificationIdBeforeOneDay", -1)
        val notificareIdInainteCuSapteZile = scop.getIntExtra("NotificationIdBeforeSevenDays", -1)

        val scope = CoroutineScope(Dispatchers.Main)
        scope.launch {
            val produs = primesteProdusulDinBazaDeDate(context, produsId)

            val titlu: String
            val descriere = "Consuma produsul cat mai curand."

            if (notificareId == notificareIdInainteCuOZi) {
                titlu = "Produsul ${produs?.NumeProdus} expira intr-o zi!"
            } else if (notificareId == notificareIdInainteCuSapteZile) {
                titlu = "Produsul ${produs?.NumeProdus} expira in cateva zile!"
            } else {
                titlu = "Titlu Notificare"
            }

            val notificareManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_HIGH
                )
                notificareManager.createNotificationChannel(channel)
            }

            val notificare = construireNotificare(context, titlu, descriere)

            notificareManager.notify(notificationId, notificare)
        }
    }

    private fun construireNotificare(context: Context, titlu: String, mesaj: String): Notification {
        return NotificationCompat.Builder(context, channelId)
            .setContentTitle(titlu)
            .setContentText(mesaj)
            .setSmallIcon(R.drawable.ic_notificare)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
    }

    private suspend fun primesteProdusulDinBazaDeDate(context: Context, produsId: Long): Produs? {
        return withContext(Dispatchers.IO) {
            val bazaDeDate = BazaDeDate(context)
            bazaDeDate.primesteProdusDao().primesteProdusDupaId(produsId)
        }
    }
}
