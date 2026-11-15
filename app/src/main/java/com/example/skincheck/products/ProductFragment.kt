package com.example.skincheck.products

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.skincheck.R
import com.example.skincheck.data.Product
import com.example.skincheck.data.SkinType
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.card.MaterialCardView
import com.google.android.material.button.MaterialButton

class ProductFragment : Fragment(R.layout.fragment_product) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val viewModel = ViewModelProvider(this)[ProductViewModel::class.java]
        val productId = requireArguments().getLong(ARG_PRODUCT_ID)

        view.findViewById<MaterialToolbar>(R.id.toolbar)
            .setNavigationOnClickListener { parentFragmentManager.popBackStack() }

        val brand = view.findViewById<TextView>(R.id.txt_brand)
        val name = view.findViewById<TextView>(R.id.txt_name)
        val meta = view.findViewById<TextView>(R.id.txt_meta)
        val ingredientsTitle = view.findViewById<TextView>(R.id.txt_ingredients_title)
        val ingredients = view.findViewById<TextView>(R.id.txt_ingredients)

        viewModel.product.observe(viewLifecycleOwner) { product ->
            if (product == null) return@observe

            brand.text = product.brand
            name.text = product.name
            meta.text = listOfNotNull(product.category, product.priceText(), product.ratingText())
                .joinToString("  ·  ")

            showMatch(view, product, viewModel.skinCode)

            val list = product.ingredientList()
            ingredientsTitle.text = getString(R.string.product_ingredients, list.size)
            ingredients.text = list.joinToString("  ·  ")
        }

        // ♡ button: save to / remove from My list
        val favoriteButton = view.findViewById<MaterialButton>(R.id.btn_favorite)
        viewModel.isFavorite.observe(viewLifecycleOwner) { saved ->
            favoriteButton.setText(if (saved) R.string.btn_saved else R.string.btn_save)
        }
        favoriteButton.setOnClickListener { viewModel.toggleFavorite() }

        if (viewModel.product.value == null) viewModel.load(productId)
    }

    // Fills the big colored card: green ✓, red ✕ or neutral ?
    private fun showMatch(view: View, product: Product, skinCode: String?) {
        val card = view.findViewById<MaterialCardView>(R.id.card_match)
        val icon = view.findViewById<TextView>(R.id.txt_match_icon)
        val title = view.findViewById<TextView>(R.id.txt_match_title)
        val desc = view.findViewById<TextView>(R.id.txt_match_desc)

        val mySkin = SkinType.displayName(requireContext(), skinCode)

        when (product.isSuitableFor(skinCode)) {
            true -> {
                paint(card, icon, title, R.color.rating_green, R.color.rating_green_light)
                icon.setText(R.string.match_icon_yes)
                title.setText(R.string.match_yes)
                desc.text = getString(R.string.match_desc, madeFor(product), mySkin)
            }
            false -> {
                paint(card, icon, title, R.color.rating_red, R.color.rating_red_light)
                icon.setText(R.string.match_icon_no)
                title.setText(R.string.match_no)
                desc.text = getString(R.string.match_desc, madeFor(product), mySkin)
            }
            null -> {
                paint(card, icon, title, R.color.rose_dark, R.color.rose_light)
                icon.setText(R.string.match_icon_unknown)
                if (skinCode.isNullOrBlank()) {
                    title.setText(R.string.match_no_quiz)
                    desc.setText(R.string.match_no_quiz_desc)
                } else {
                    title.setText(R.string.match_no_info)
                    desc.setText(R.string.match_no_info_desc)
                }
            }
        }
    }

    private fun paint(card: MaterialCardView, icon: TextView, title: TextView, strong: Int, light: Int) {
        val context = requireContext()
        card.setCardBackgroundColor(ContextCompat.getColor(context, light))
        icon.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, strong))
        title.setTextColor(ContextCompat.getColor(context, strong))
    }

    // {"dry", "normal"} -> "dry, normal" (in the phone's language)
    private fun madeFor(product: Product): String {
        val names = linkedMapOf(
            "dry" to R.string.skin_dry,
            "oily" to R.string.skin_oily,
            "combination" to R.string.skin_combination,
            "normal" to R.string.skin_normal,
            "sensitive" to R.string.skin_sensitive
        )
        return names.filterKeys { it in product.suitableFor }
            .values.joinToString(", ") { getString(it).lowercase() }
    }

    companion object {
        private const val ARG_PRODUCT_ID = "product_id"

        // Always create this screen with newInstance(id), never with ProductFragment()
        fun newInstance(productId: Long) = ProductFragment().apply {
            arguments = bundleOf(ARG_PRODUCT_ID to productId)
        }
    }
}