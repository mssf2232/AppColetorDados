package com.informatica.mycoletor
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter

import com.informatica.mycoletor.databinding.ListItemBinding

class CustomArrayAdapter(context: Context, private var items: MutableList<FrigoColetados>)
    : ArrayAdapter<FrigoColetados>(context, 0, items) {

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding: ListItemBinding
        val view: View

        if (convertView == null) {
            binding = ListItemBinding.inflate(inflater, parent, false)
            view = binding.root
            view.tag = binding
        } else {
            view = convertView
            binding = view.tag as ListItemBinding
        }

        val item = getItem(position)
        binding.itemText.text = item?.codigobarra
        return view
    }

    // Método para atualizar a lista de dados
    fun updateItems(newItems: List<FrigoColetados>) {
        items.clear()
        items.addAll(newItems.reversed())  // Adicionar os itens na ordem inversa
        notifyDataSetChanged()
    }
    fun clearItems() {
        items.clear()
        notifyDataSetChanged()
    }
}

