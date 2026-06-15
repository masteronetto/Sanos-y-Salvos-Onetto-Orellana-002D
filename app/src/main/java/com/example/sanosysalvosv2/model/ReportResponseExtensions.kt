package com.example.sanosysalvosv2.model

// Backwards-compatibility extensions mapping the new ReportResponse fields
// to the legacy property names used across the UI.

val ReportResponse.petName: String?
    get() = this.species ?: this.breed ?: this.locationName

val ReportResponse.reportedBy: String?
    get() = this.reporterId

val ReportResponse.comuna: String?
    get() = this.locationName

val ReportResponse.date: String?
    get() = this.eventDate ?: this.createdAt

val ReportResponse.location: String?
    get() = this.locationName

val ReportResponse.petPhotoUrl: String?
    get() = this.photoUrl
