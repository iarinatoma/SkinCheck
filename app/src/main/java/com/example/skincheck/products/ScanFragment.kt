package com.example.skincheck.products

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.skincheck.MainActivity
import com.example.skincheck.R
import com.example.skincheck.data.SortOrder
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText

class ScanFragment : Fragment(R.layout.fragment_scan) {

    private lateinit var viewModel: SearchViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel = ViewModelProvider(this)[SearchViewModel::class.java]

        val input = view.findViewById<TextInputEditText>(R.id.input_search)
        val sortChips = view.findViewById<ChipGroup>(R.id.chips_sort)
        val onlyMe = view.findViewById<Chip>(R.id.chip_only_me)
        val info = view.findViewById<TextView>(R.id.txt_info)
        val empty = view.findViewById<TextView>(R.id.txt_empty)
        val list = view.findViewById<RecyclerView>(R.id.list_products)

        // Tapping a product opens its page on top of the search screen
        val adapter = ProductAdapter(viewModel.skinCode) { product ->
            (requireActivity() as MainActivity).open(ProductFragment.newInstance(product.id))
        }
        list.adapter = adapter

        // Redraw the list every time the results change
        viewModel.results.observe(viewLifecycleOwner) { products ->
            adapter.submitList(products)
            empty.isVisible = products.isEmpty()

            val total = viewModel.totalMatches
            info.text = when {
                viewModel.query.isEmpty() && !viewModel.onlyForMe ->
                    getString(R.string.search_top_rated, viewModel.totalCount)
                total > products.size ->
                    getString(R.string.search_showing, products.size, total)
                else ->
                    resources.getQuantityString(R.plurals.search_results, total, total)
            }
        }

        // Sorting: exactly one chip is selected. First show the one chosen before.
        sortChips.check(
            when (viewModel.sort) {
                SortOrder.PRICE_LOW -> R.id.chip_price_low
                SortOrder.PRICE_HIGH -> R.id.chip_price_high
                SortOrder.TOP_RATED -> R.id.chip_top
            }
        )
        sortChips.setOnCheckedStateChangeListener { _, checkedIds ->
            val sort = when (checkedIds.firstOrNull()) {
                R.id.chip_price_low -> SortOrder.PRICE_LOW
                R.id.chip_price_high -> SortOrder.PRICE_HIGH
                else -> SortOrder.TOP_RATED
            }
            viewModel.sortBy(sort)
        }

        // "Only for my skin" makes sense only after the quiz
        onlyMe.isVisible = viewModel.skinCode != null
        onlyMe.isChecked = viewModel.onlyForMe
        onlyMe.setOnCheckedChangeListener { _, isChecked -> viewModel.showOnlyForMe(isChecked) }

        // Search again on every letter typed
        input.doAfterTextChanged { text -> viewModel.search(text?.toString().orEmpty()) }

        // First time: show the best rated products
        if (viewModel.results.value == null) viewModel.search("")
    }
}