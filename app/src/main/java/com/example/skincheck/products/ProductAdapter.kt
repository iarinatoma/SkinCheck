package com.example.skincheck.products

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.skincheck.R
import com.example.skincheck.data.Product

// Turns a list of Product objects into rows on the screen (item_product.xml)
class ProductAdapter(
    private val skinCode: String?,              // the user's skin type, e.g. "oily,sensitive"
    private val onClick: (Product) -> Unit      // what happens when a row is tapped
) : ListAdapter<Product, ProductAdapter.ProductViewHolder>(DIFF) {

    // Holds the views of one row, so we don't search for them every time
    class ProductViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val brand: TextView = view.findViewById(R.id.txt_brand)
        val name: TextView = view.findViewById(R.id.txt_name)
        val meta: TextView = view.findViewById(R.id.txt_meta)
        val rating: TextView = view.findViewById(R.id.txt_rating)
        val match: TextView = view.findViewById(R.id.txt_match)
    }

    // Called only for the few rows that fit on the screen
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    // Called every time a row is (re)filled with a product, e.g. while scrolling
    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = getItem(position)
        val context = holder.itemView.context

        holder.brand.text = product.brand
        holder.name.text = product.name
        holder.meta.text = listOfNotNull(product.category, product.priceText()).joinToString(" · ")

        val rating = product.ratingText()
        holder.rating.isVisible = rating != null
        holder.rating.text = rating

        when (product.isSuitableFor(skinCode)) {
            true -> {
                holder.match.isVisible = true
                holder.match.setText(R.string.match_yes_short)
                holder.match.setTextColor(ContextCompat.getColor(context, R.color.rating_green))
            }
            false -> {
                holder.match.isVisible = true
                holder.match.setText(R.string.match_no_short)
                holder.match.setTextColor(ContextCompat.getColor(context, R.color.rating_red))
            }
            null -> holder.match.isVisible = false // no quiz yet -> we don't guess
        }

        holder.itemView.setOnClickListener { onClick(product) }
    }

    companion object {
        // Lets the list update only the rows that really changed (smooth, no flicker)
        private val DIFF = object : DiffUtil.ItemCallback<Product>() {
            override fun areItemsTheSame(oldItem: Product, newItem: Product) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Product, newItem: Product) = oldItem == newItem
        }
    }
}