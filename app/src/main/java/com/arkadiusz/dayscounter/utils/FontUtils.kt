package com.arkadiusz.dayscounter.utils

import android.content.Context
import android.graphics.Typeface
import android.support.v4.content.res.ResourcesCompat
import com.arkadiusz.dayscounter.R

/**
 * Created by arkadiusz on 09.03.18
 */

object FontUtils {

    fun getFontFor(name: String, context: Context): Typeface? {
        return when (name) {
            "Allerta" -> ResourcesCompat.getFont(context, R.font.allerta)
            "Fira Sans" -> ResourcesCompat.getFont(context, R.font.firasans)
            "Josefin Sans" -> ResourcesCompat.getFont(context, R.font.josefinsans)
            "Lato" -> ResourcesCompat.getFont(context, R.font.lato)
            "Montserrat" -> ResourcesCompat.getFont(context, R.font.montserrat)
            "Open Sans" -> ResourcesCompat.getFont(context, R.font.opensans)
            "Pacifico" -> ResourcesCompat.getFont(context, R.font.pacifico)
            "PT Sans" -> ResourcesCompat.getFont(context, R.font.ptsans)
            "Roboto" -> ResourcesCompat.getFont(context, R.font.roboto)
            "Roboto Slab" -> ResourcesCompat.getFont(context, R.font.robotoslab)
            "Ropa Sans" -> ResourcesCompat.getFont(context, R.font.ropasans)
            "Source Sans Pro" -> ResourcesCompat.getFont(context, R.font.sourcesanspro)
            "Titillium Web" -> ResourcesCompat.getFont(context, R.font.titilliumweb)
            "Ubuntu" -> ResourcesCompat.getFont(context, R.font.ubuntu)
            else -> null
        }
    }

    fun getFontPositionFor(name: String): Int {
        return when (name) {
            "Allerta" -> 0
            "Fira Sans" -> 1
            "Josefin Sans" -> 2
            "Lato" -> 3
            "Montserrat" -> 4
            "Open Sans" -> 5
            "Pacifico" -> 6
            "PT Sans" -> 7
            "Roboto" -> 8
            "Roboto Slab" -> 9
            "Ropa Sans" -> 10
            "Source Sans Pro" -> 11
            "Titillium Web" -> 12
            "Ubuntu" -> 13
            else -> 8 //default Roboto
        }
    }
}