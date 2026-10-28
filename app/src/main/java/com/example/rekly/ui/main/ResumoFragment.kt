package com.example.rekly.ui.main

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.rekly.R
import com.example.rekly.databinding.FragmentResumoBinding

/** Aba inicial: totais do mês, próximos vencimentos, pendentes e sugestões (tarefa 10). */
class ResumoFragment : Fragment(R.layout.fragment_resumo) {

    // A view pode ser destruída antes do próprio fragment; liberar o binding em
    // onDestroyView evita segurar uma view que não está mais na tela.
    private var _binding: FragmentResumoBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentResumoBinding.bind(view)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
