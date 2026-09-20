package com.example.bugs_android.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.SimpleAdapter
import androidx.fragment.app.Fragment
import com.example.bugs_android.R

class AuthorsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(
            R.layout.fragment_authors,
            container,
            false
        )

        val listView = view.findViewById<ListView>(R.id.listViewAuthors)

        val authors = listOf(
            mapOf(
                "name" to "Фатеева Ксения Алексеевна",
                "image" to R.drawable.author1
            )

        )
        val adapter = SimpleAdapter(
            requireContext(),
            authors,
            R.layout.item_author,
            arrayOf("name", "image"),
            intArrayOf(R.id.textViewAuthor, R.id.imageViewAuthor)
        )
        listView.adapter = adapter


        return view

    }

}