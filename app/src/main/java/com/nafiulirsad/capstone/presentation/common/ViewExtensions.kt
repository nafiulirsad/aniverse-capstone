package com.nafiulirsad.capstone.presentation.common

import android.os.Bundle
import android.widget.ImageView
import androidx.navigation.NavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.nafiulirsad.capstone.R

fun ImageView.loadPoster(url: String?) {
    Glide.with(this)
        .load(url)
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .placeholder(R.drawable.bg_poster_placeholder)
        .error(R.drawable.bg_poster_placeholder)
        .centerCrop()
        .into(this)
}

/**
 * Single navigation entry point to the detail screen, reused by the dynamic feature module.
 * The guard swallows a double tap on two list items, which would otherwise stack two screens.
 */
fun NavController.navigateToAnimeDetail(animeId: Int) {
    if (currentDestination?.id == R.id.detailFragment) return
    navigate(R.id.action_global_detail, Bundle().apply { putInt(ARG_ANIME_ID, animeId) })
}

const val ARG_ANIME_ID = "animeId"
