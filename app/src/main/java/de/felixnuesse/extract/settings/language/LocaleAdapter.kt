package de.felixnuesse.extract.settings.language

import java.util.Locale


class LocaleAdapter(private var mLocale: Locale) {

    override fun toString(): String {
        return mLocale.displayLanguage
    }

    fun getLocale(): Locale {
        return mLocale
    }
}