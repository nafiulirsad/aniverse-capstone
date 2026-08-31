package com.nafiulirsad.capstone.presentation.common

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nafiulirsad.capstone.databinding.ItemAnimeBinding
import com.nafiulirsad.capstone.presentation.model.AnimeUi

/** Shared by the Home screen and by the Favorite dynamic feature module. */
class AnimeAdapter(private val onItemClick: (AnimeUi) -> Unit) :
    ListAdapter<AnimeUi, AnimeAdapter.AnimeViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimeViewHolder {
        val binding = ItemAnimeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AnimeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AnimeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class AnimeViewHolder(private val binding: ItemAnimeBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AnimeUi) = with(binding) {
            imgPoster.loadPoster(item.posterUrl)
            imgPoster.contentDescription = item.title
            tvTitle.text = item.title
            tvScore.text = item.scoreLabel
            tvMeta.text = item.metaLabel
            root.setOnClickListener { onItemClick(item) }
        }
    }

    private companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<AnimeUi>() {
            override fun areItemsTheSame(oldItem: AnimeUi, newItem: AnimeUi): Boolean =
                oldItem.animeId == newItem.animeId

            override fun areContentsTheSame(oldItem: AnimeUi, newItem: AnimeUi): Boolean =
                oldItem == newItem
        }
    }
}
