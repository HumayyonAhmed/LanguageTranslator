data class HistoryData(val sourceLang: String, val targetLang: String, val sourceTxt: String, val translatedTxt: String,val dateCreated: String, val favourite: Boolean) {
    constructor() : this("", "","","","",false)
}
