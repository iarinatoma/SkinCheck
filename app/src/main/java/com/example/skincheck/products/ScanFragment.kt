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
import com.google.android.material.textfield.TextInputEditText

class ScanFragment : Fragment(R.layout.fragment_scan) {

    private lateinit var viewModel: SearchViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel = ViewModelProvider(this)[SearchViewModel::class.java]

        val input = view.findViewById<TextInputEditText>(R.id.input_search)
        val info = view.findViewById<TextView>(R.id.txt_info)
        val empty = view.findViewById<TextView>(R.id.txt_empty)
        val list = view.findViewById<RecyclerView>(R.id.list_products)

        // Tapping a product opens its page on top of the search screen
        val adapter = ProductAdapter(viewModel.skinCode()) { product ->
            (requireActivity() as MainActivity).open(ProductFragment.newInstance(product.id))
        }
        list.adapter = adapter

        // Redraw the list every time the results change
        viewModel.results.observe(viewLifecycleOwner) { products ->
            adapter.submitList(products)
            empty.isVisible = products.isEmpty()

            val query = input.text?.toString()?.trim().orEmpty()
            info.text = if (query.isEmpty()) {
                getString(R.string.search_top_rated, viewModel.totalCount)
            } else {
                resources.getQuantityString(R.plurals.search_results, products.size, products.size)
            }
        }

        // Search again on every letter typed
        input.doAfterTextChanged { text -> viewModel.search(text?.toString().orEmpty()) }

        // First time: show the best rated products
        if (viewModel.results.value == null) viewModel.search("")
    }
}