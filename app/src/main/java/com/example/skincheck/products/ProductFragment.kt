package com.example.skincheck.products

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.skincheck.R
import com.example.skincheck.data.CommunityRating
import com.example.skincheck.data.Product
import com.example.skincheck.data.ProductAnalysis
import com.example.skincheck.data.Rating
import com.example.skincheck.data.SkinType
import com.example.skincheck.data.Verdict
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import java.util.Locale

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

        // Top of the page + "According to the brand"
        viewModel.product.observe(viewLifecycleOwner) { product ->
            if (product == null) return@observe
            brand.text = product.brand
            name.text = product.name
            meta.text = listOfNotNull(
                product.category, product.priceText(), product.ratingText(), product.lovesText()
            ).joinToString("  ·  ")
            showMatch(view, product, viewModel.skinCode)
        }

        // "SkinCheck recommends" + colored ingredient list
        viewModel.analysis.observe(viewLifecycleOwner) { analysis ->
            if (analysis == null) return@observe
            showVerdict(view, analysis, viewModel.skinCode)
            ingredientsTitle.text = getString(R.string.product_ingredients, analysis.ingredients.size)
            ingredients.text = coloredIngredients(analysis)
        }

        // "Community recommends"
        viewModel.community.observe(viewLifecycleOwner) { ratings ->
            showCommunity(view, ratings, viewModel.skinCode)
        }

        // ♡ button: save to / remove from My list
        val favoriteButton = view.findViewById<MaterialButton>(R.id.btn_favorite)
        viewModel.isFavorite.observe(viewLifecycleOwner) { saved ->
            favoriteButton.setText(if (saved) R.string.btn_saved else R.string.btn_save)
        }
        favoriteButton.setOnClickListener { viewModel.toggleFavorite() }

        if (viewModel.product.value == null) viewModel.load(productId)
    }

    // ---------- SkinCheck recommends ----------

    private fun showVerdict(view: View, analysis: ProductAnalysis, skinCode: String?) {
        val icon = view.findViewById<TextView>(R.id.txt_verdict_icon)
        val title = view.findViewById<TextView>(R.id.txt_verdict_title)
        val counts = view.findViewById<TextView>(R.id.txt_verdict_counts)
        val container = view.findViewById<LinearLayout>(R.id.list_flagged)

        when (analysis.verdict) {
            Verdict.RECOMMENDED -> {
                icon.setText(R.string.match_icon_yes)
                title.setText(R.string.verdict_recommended)
            }
            Verdict.CAREFUL -> {
                icon.setText(R.string.verdict_icon_careful)
                title.setText(R.string.verdict_careful)
            }
            Verdict.NOT_RECOMMENDED -> {
                icon.setText(R.string.match_icon_no)
                title.setText(R.string.verdict_not_recommended)
            }
            Verdict.UNKNOWN -> {
                icon.setText(R.string.match_icon_unknown)
                title.setText(R.string.verdict_unknown)
            }
        }
        val rating = when (analysis.verdict) {
            Verdict.RECOMMENDED -> Rating.GOOD
            Verdict.CAREFUL -> Rating.CAUTION
            Verdict.NOT_RECOMMENDED -> Rating.AVOID
            Verdict.UNKNOWN -> Rating.NEUTRAL
        }
        icon.backgroundTintList = ColorStateList.valueOf(dotColor(rating))
        title.setTextColor(textColor(rating) ?: color(R.color.ink))

        val skinName = SkinType.displayName(requireContext(), skinCode)
            ?: getString(R.string.verdict_no_quiz)
        counts.text = getString(
            R.string.verdict_counts, analysis.good, analysis.caution, analysis.avoid, skinName
        )

        // One row per ingredient group we know something about, worst first
        container.removeAllViews()
        for ((info, items) in analysis.groups()) {
            val row = layoutInflater.inflate(R.layout.item_ingredient, container, false)
            row.findViewById<View>(R.id.view_dot).backgroundTintList =
                ColorStateList.valueOf(dotColor(items.first().rating))
            row.findViewById<TextView>(R.id.txt_ing_name).text = info.name
            row.findViewById<TextView>(R.id.txt_ing_found).text =
                getString(R.string.verdict_found, items.joinToString(", ") { it.name })
            row.findViewById<TextView>(R.id.txt_ing_why).text = info.why
            container.addView(row)
        }
    }

    // All ingredients in one text; the known ones are colored and bold
    private fun coloredIngredients(analysis: ProductAnalysis): CharSequence = buildSpannedString {
        analysis.ingredients.forEachIndexed { index, item ->
            if (index > 0) append("  ·  ")
            val tint = textColor(item.rating)
            if (tint == null) {
                append(item.name)
            } else {
                color(tint) { bold { append(item.name) } }
            }
        }
    }

    // ---------- Community recommends ----------

    private fun showCommunity(view: View, ratings: List<CommunityRating>, skinCode: String?) {
        val card = view.findViewById<MaterialCardView>(R.id.card_community)
        val main = view.findViewById<TextView>(R.id.txt_community_main)
        val detail = view.findViewById<TextView>(R.id.txt_community_detail)
        val others = view.findViewById<TextView>(R.id.txt_community_others)

        // No reviews for this product -> no card (we don't invent anything)
        card.isVisible = ratings.isNotEmpty()
        if (ratings.isEmpty()) return

        val mySkin = skinCode?.split(",")?.first()     // "combination,sensitive" -> "combination"
        val mine = ratings.find { it.skinType == mySkin }
        val myRating = mine?.avgRating

        if (mine != null && myRating != null) {
            main.text = getString(
                R.string.community_mine, oneDecimal(myRating), skinName(mine.skinType).lowercase()
            )
            val level = when {
                myRating >= 4.0 -> Rating.GOOD
                myRating >= 3.5 -> Rating.CAUTION
                else -> Rating.AVOID
            }
            main.setTextColor(textColor(level) ?: color(R.color.ink))
            detail.isVisible = true
            detail.text = if (mine.recommendedPct != null) {
                getString(R.string.community_mine_detail, mine.recommendedPct, mine.reviews)
            } else {
                getString(R.string.community_reviews_only, mine.reviews)
            }
        } else {
            main.setText(if (mySkin == null) R.string.community_no_quiz else R.string.community_none_for_me)
            main.setTextColor(color(R.color.ink))
            detail.isVisible = false
        }

        // The other skin types, e.g. "Oily 2.8★ (22) · Dry 4.5★ (40)"
        val rest = ratings.filter { it !== mine && it.avgRating != null }
        others.isVisible = rest.isNotEmpty()
        others.text = getString(
            R.string.community_others,
            rest.joinToString(" · ") {
                "${skinName(it.skinType)} ${oneDecimal(it.avgRating ?: 0.0)}★ (${it.reviews})"
            }
        )
    }

    private fun oneDecimal(value: Double) = String.format(Locale.US, "%.1f", value)

    private fun skinName(code: String) = getString(
        when (code) {
            "dry" -> R.string.skin_dry
            "oily" -> R.string.skin_oily
            "combination" -> R.string.skin_combination
            else -> R.string.skin_normal
        }
    )

    // ---------- Colors ----------

    private fun color(res: Int) = ContextCompat.getColor(requireContext(), res)

    // Color of the dots and the big icon
    private fun dotColor(rating: Rating) = when (rating) {
        Rating.GOOD -> color(R.color.rating_green)
        Rating.CAUTION -> color(R.color.rating_yellow)
        Rating.AVOID -> color(R.color.rating_red)
        Rating.NEUTRAL -> color(R.color.ink_soft)
    }

    // Color for text (darker yellow, so it can be read on white); null = normal text
    private fun textColor(rating: Rating): Int? = when (rating) {
        Rating.GOOD -> color(R.color.rating_green)
        Rating.CAUTION -> color(R.color.rating_yellow_text)
        Rating.AVOID -> color(R.color.rating_red)
        Rating.NEUTRAL -> null
    }

    // ---------- According to the brand ----------

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
        card.setCardBackgroundColor(color(light))
        icon.backgroundTintList = ColorStateList.valueOf(color(strong))
        title.setTextColor(color(strong))
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