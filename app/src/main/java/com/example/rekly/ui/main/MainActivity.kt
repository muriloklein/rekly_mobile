package com.example.rekly.ui.main

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.example.rekly.R
import com.example.rekly.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar

/**
 * Tela central do app: hospeda as abas Resumo, Assinaturas e Perfil, trocadas
 * pela navegação inferior, e o botão + para cadastrar assinatura.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar.root)
        aplicarEspacamentoDasBarrasDoSistema()
        configurarNavegacao(savedInstanceState)
        binding.fabNovaAssinatura.setOnClickListener { abrirNovaAssinatura() }
    }

    /**
     * Depois de uma rotação, o fragment da aba é recriado pelo FragmentManager e a
     * navegação inferior restaura o item selecionado aqui, depois do onCreate.
     * Só falta acertar o título e o botão + para a aba restaurada.
     */
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        atualizarCabecalho(binding.navegacaoInferior.selectedItemId)
    }

    private fun configurarNavegacao(savedInstanceState: Bundle?) {
        binding.navegacaoInferior.setOnItemSelectedListener { item ->
            mostrarAba(item.itemId)
            true
        }
        // Tocar na aba que já está aberta não recria o fragment
        binding.navegacaoInferior.setOnItemReselectedListener { }

        // Na primeira abertura mostra o Resumo; após rotação o fragment já foi restaurado
        if (savedInstanceState == null) {
            mostrarAba(R.id.nav_resumo)
        }
    }

    private fun mostrarAba(@IdRes itemId: Int) {
        val fragment: Fragment = when (itemId) {
            R.id.nav_assinaturas -> AssinaturasFragment()
            R.id.nav_perfil -> PerfilFragment()
            else -> ResumoFragment()
        }
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace(R.id.containerFragments, fragment)
        }
        atualizarCabecalho(itemId)
    }

    /** Título da toolbar conforme a aba; o botão + não aparece no Perfil. */
    private fun atualizarCabecalho(@IdRes itemId: Int) {
        val titulo = when (itemId) {
            R.id.nav_assinaturas -> R.string.aba_assinaturas
            R.id.nav_perfil -> R.string.aba_perfil
            else -> R.string.aba_resumo
        }
        supportActionBar?.setTitle(titulo)
        if (itemId == R.id.nav_perfil) {
            binding.fabNovaAssinatura.hide()
        } else {
            binding.fabNovaAssinatura.show()
        }
    }

    // Substituído pela abertura do formulário de assinatura na tarefa 6
    private fun abrirNovaAssinatura() {
        Snackbar.make(binding.root, R.string.aviso_em_breve, Snackbar.LENGTH_SHORT)
            .setAnchorView(binding.fabNovaAssinatura)
            .show()
    }

    /**
     * Com o app desenhando atrás das barras do sistema (edge-to-edge), cada parte
     * cuida do seu espaçamento: a toolbar estende a cor até a barra de status, a
     * navegação inferior se afasta da barra de navegação sozinha e as laterais
     * (celular deitado) ficam com a tela inteira.
     */
    private fun aplicarEspacamentoDasBarrasDoSistema() {
        val tipo = WindowInsetsCompat.Type.systemBars()

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val barras = insets.getInsets(tipo)
            view.updatePadding(left = barras.left, right = barras.right)
            // Repassa aos filhos só o topo e a base, que já foram tratados nas laterais
            WindowInsetsCompat.Builder(insets)
                .setInsets(tipo, Insets.of(0, barras.top, 0, barras.bottom))
                .build()
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.toolbar.root) { view, insets ->
            view.updatePadding(top = insets.getInsets(tipo).top)
            insets
        }
    }
}
