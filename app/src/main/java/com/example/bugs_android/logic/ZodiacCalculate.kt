package com.example.bugs_android.logic

fun сalculateZodiac(day: Int, month: Int) : String {
    if ((day >= 21 && month  == 3) || (day <= 19 && month == 4)){
        return "Овен"
    }
    else if ((day >= 20 && month  == 4) || (day <= 20 && month == 5)){
        return "Телец"
    }
    else if ((day >= 21 && month  == 5) || (day <= 20 && month == 6)){
        return "Близнецы"
    }
    else if ((day >= 21 && month  == 6) || (day <= 22 && month == 7)){
        return "Рак"
    }
    else if ((day >= 23 && month  == 7) || (day <= 22 && month == 8)){
        return "Лев"
    }
    else if ((day >= 23 && month  == 8) || (day <= 22 && month == 9)){
        return "Дева"
    }
    else if ((day >= 23 && month  == 9) || (day <= 22 && month == 10)){
        return "Весы"
    }
    else if ((day >= 23 && month  == 10) || (day <= 21 && month == 11)){
        return "Скорпион"
    }
    else if ((day >= 22 && month  == 11) || (day <= 21 && month == 12)){
        return "Стрелец"
    }
    else if ((day >= 22 && month  == 12) || (day <= 19 && month == 1)){
        return "Козерог"
    }
    else if ((day >= 20 && month  == 1) || (day <= 18 && month == 2)){
        return "Водолей"
    }
    else if ((day >= 19 && month  == 2) || (day <= 20 && month == 3)){
        return "Рыбы"
    }
    else
        return "Неизвестно"
}