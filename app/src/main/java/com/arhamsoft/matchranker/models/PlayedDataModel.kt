package com.arhamsoft.matchranker.models



data class PlayedDataModel (
    var id: String,
    val type: String,
    val href: String,
    val attributes: PlayedAttributes,
    val relationships: Relationships
)