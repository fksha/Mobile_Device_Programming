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
    var date: String? = null
    var buy: Double? = null
    var sell: Double? = null

    var result: GoldRate? = null
    var latestDate = ""

    while (parser.next() != XmlPullParser.END_DOCUMENT) {
        if (parser.eventType == XmlPullParser.START_TAG) {
            when (parser.name) {
                "Record" -> {
                    code = parser.getAttributeValue(null, "Code")
                    date = parser.getAttributeValue(null, "Date")
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

        if(parser.eventType == XmlPullParser.END_TAG && parser.name == "Record"){
            if (code == "1" && buy != null && sell != null && date != null) {
                val parts = date!!.split(".")
                val compareDate = parts[2] + parts[1] + parts[0]

                if(compareDate > latestDate){
                    latestDate = compareDate
                    result = GoldRate(buy!!, sell!!)
                }
            }
        }


    }

    return result
}
