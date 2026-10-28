package com.example.rekly.ui.main

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.rekly.R
import com.example.rekly.databinding.FragmentPerfilBinding

/** Dados do usuário, acesso às categorias e botão Sair (tarefas 3 e 7). */
class PerfilFragment : Fragment(R.layout.fragment_perfil) {

    // A view pode ser destruída antes do próprio fragment; liberar o binding em
    // onDestroyView evita segurar uma view que não está mais na tela.
    private var _binding: FragmentPerfilBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPerfilBinding.bind(view)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
