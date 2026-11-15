package com.example.skincheck.history

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.skincheck.MainActivity
import com.example.skincheck.R
import com.example.skincheck.products.ProductAdapter
import com.example.skincheck.products.ProductFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup

class HistoryFragment : Fragment(R.layout.fragment_history) {

    private lateinit var viewModel: HistoryViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel = ViewModelProvider(this)[HistoryViewModel::class.java]

        val toggle = view.findViewById<MaterialButtonToggleGroup>(R.id.toggle_tabs)
        val clear = view.findViewById<MaterialButton>(R.id.btn_clear)
        val empty = view.findViewById<TextView>(R.id.txt_empty)
        val list = view.findViewById<RecyclerView>(R.id.list_items)

        // Same rows and same adapter as on the search screen
        val adapter = ProductAdapter(viewModel.skinCode()) { product ->
            (requireActivity() as MainActivity).open(ProductFragment.newInstance(product.id))
        }
        list.adapter = adapter

        // Keep the tab that was open before (e.g. after coming back from a product)
        toggle.check(if (viewModel.showingMyList) R.id.btn_tab_list else R.id.btn_tab_recent)
        toggle.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) viewModel.showTab(myList = checkedId == R.id.btn_tab_list)
        }

        viewModel.items.observe(viewLifecycleOwner) { products ->
            adapter.submitList(products)
            empty.isVisible = products.isEmpty()
            empty.setText(if (viewModel.showingMyList) R.string.my_list_empty else R.string.history_empty)
            clear.isVisible = !viewModel.showingMyList && products.isNotEmpty()
        }

        clear.setOnClickListener { viewModel.clearHistory() }
    }

    // Reload every time the screen becomes visible (e.g. after saving a product)
    override fun onResume() {
        super.onResume()
        viewModel.refresh()
    }
}