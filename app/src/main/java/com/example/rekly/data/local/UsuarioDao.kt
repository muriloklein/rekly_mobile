package com.example.rekly.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.rekly.data.entity.Usuario

@Dao
interface UsuarioDao {

    @Insert
    suspend fun inserir(usuario: Usuario): Long

    @Query("SELECT * FROM usuarios WHERE email = :email LIMIT 1")
    suspend fun buscarPorEmail(email: String): Usuario?
}
