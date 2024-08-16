package com.mohit.dynamicwallpaper

import kotlinx.serialization.Serializable


@Serializable
data class UnsplashApiSchema(
    val id: String,
    val slug: String,
    val width: Int,
    val height: Int,
    val color: String,
    val blur_hash: String,
    val description: String?,
    val alt_description: String?,
    val breadcrumbs: List<String>,
    val urls: Urls,
    val links: Links,
    val likes: Int,
    val liked_by_user: Boolean,
)


@Serializable
data class Urls(
    val raw: String,
    val full: String,
    val regular: String,
    val small: String,
    val thumb: String,
    val small_s3: String
)

@Serializable
data class Links(
    val self: String,
    val html: String,
    val download: String,
    val download_location: String
)