package com.example.bugs_android.network

import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import java.io.StringReader

data class GoldRate(
    val buy: Double,
    val sell: Double
)

fun parseGoldRate(xml: String): GoldRate? {
    val parser = Xml.newPullParser()
    parser.setInput(StringReader(xml))

    var code: String? = null
    var buy: Double? = null
    var sell: Double? = null

    while (parser.next() != XmlPullParser.END_DOCUMENT) {
        if (parser.eventType == XmlPullParser.START_TAG) {
            when (parser.name) {
                "Record" -> {
                    code = parser.getAttributeValue(null, "Code")
                    buy = null
                    sell = null
                }

                "Buy" -> {
                    if (code == "1") {
                        parser.next()
                        buy = parser.text
                            .replace(',', '.')
                            .toDoubleOrNull()
                    }
                }

                "Sell" -> {
                    if (code == "1") {
                        parser.next()
                        sell = parser.text
                            .replace(',', '.')
                            .toDoubleOrNull()
                    }
                }
            }
        }

        if (code == "1" && buy != null && sell != null) {
            return GoldRate(buy, sell)
        }
    }

    return null
}
