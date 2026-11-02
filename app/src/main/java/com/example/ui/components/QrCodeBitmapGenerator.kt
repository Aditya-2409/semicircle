package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

object QrCodeBitmapGenerator {
    fun generateQrBitmap(
        content: String,
        size: Int = 512,
        qrColor: Color = Color(0xFFFF8A24),
        backgroundColor: Color = Color(0xFF151515)
    ): Bitmap {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size)
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val fg = qrColor.toArgb()
        val bg = backgroundColor.toArgb()

        for (x in 0 until size) {
            for (y in 0 until size) {
                bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) fg else bg)
            }
        }
        return bitmap
    }
}
