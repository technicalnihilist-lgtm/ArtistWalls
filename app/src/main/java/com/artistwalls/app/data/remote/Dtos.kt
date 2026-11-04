package com.artistwalls.app.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Minimal, tolerant model of the MediaWiki action API response.
 * Only the fields the app needs; everything is nullable so a schema tweak on the
 * server can never crash the app.
 */
@Serializable
data class CommonsResponse(
    val batchcomplete: Boolean? = null,
    val query: CommonsQuery? = null,
    @SerialName("continue") val continueInfo: CommonsContinue? = null,
    val error: CommonsError? = null
)

@Serializable
data class CommonsQuery(
    val pages: List<CommonsPage>? = null,
    val searchinfo: SearchInfo? = null
)

@Serializable
data class SearchInfo(val totalhits: Long? = null)

@Serializable
data class CommonsPage(
    val pageid: Long? = null,
    val ns: Int? = null,
    val title: String = "",
    val imageinfo: List<ImageInfo>? = null
)

@Serializable
data class ImageInfo(
    val url: String? = null,
    val descriptionurl: String? = null,
    val descriptionshorturl: String? = null,
    val thumburl: String? = null,
    val thumbwidth: Int? = null,
    val thumbheight: Int? = null,
    val width: Int? = null,
    val height: Int? = null,
    val size: Long? = null,
    val mime: String? = null,
    val mediatype: String? = null,
    val extmetadata: Map<String, ExtMetaValue>? = null
)

@Serializable
data class ExtMetaValue(
    val value: String? = null,
    val source: String? = null,
    val hidden: Boolean? = null
)

@Serializable
data class CommonsContinue(
    val gsroffset: Int? = null,
    val sroffset: Int? = null,
    @SerialName("continue") val token: String? = null
)

@Serializable
data class CommonsError(val code: String? = null, val info: String? = null)
