package com.example.alertapp.ui

fun formatThreatTypeLabel(type: String): String =
    type.replaceFirstChar { if (it.isLowerCase()) it.uppercase() else it.toString() }

fun formatDetectedAtLabel(iso: String): String {
    if (!iso.contains("T")) return iso
    val parts = iso.split("T")
    val dateStr = parts.getOrNull(0) ?: return iso
    val timeStr = parts.getOrNull(1)?.take(8) ?: return iso
    val dateFormatted = dateStr.split("-").reversed().joinToString(".")
    return "$timeStr · $dateFormatted"
}
