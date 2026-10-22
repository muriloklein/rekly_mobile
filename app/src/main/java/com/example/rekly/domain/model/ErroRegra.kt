package com.example.rekly.domain.model

/** Motivos pelos quais uma operação foi barrada por regra de negócio. */
enum class ErroRegra {
    /** Nome vazio. */
    NOME_OBRIGATORIO,

    /** Já existe categoria (padrão ou do usuário) com esse nome. */
    NOME_DUPLICADO,

    /** Categorias padrão não podem ser renomeadas nem excluídas. */
    CATEGORIA_PADRAO,

    /** Categoria com assinaturas não pode ser excluída. */
    CATEGORIA_EM_USO,

    /** Assinatura com pagamentos não pode ser excluída; a alternativa é cancelar. */
    ASSINATURA_COM_PAGAMENTOS,

    /** Valor deve ser maior que zero. */
    VALOR_INVALIDO,

    /** Registro não existe mais no banco. */
    NAO_ENCONTRADO
}
