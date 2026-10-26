package com.example.mercadopreso

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Dao
interface ProdutoDao {
    @Insert suspend fun inserir(item: Produto): Long
    @Query("SELECT * FROM produtos ORDER BY nome") suspend fun buscarTodos(): List<Produto>
    @Query("SELECT * FROM produtos WHERE id = :id LIMIT 1") suspend fun buscarPorId(id: Int): Produto?
    @Update suspend fun atualizar(item: Produto): Unit
    @Delete suspend fun deletar(item: Produto): Unit
}

@Dao
interface CategoriaDao {
    @Insert suspend fun inserir(item: Categoria): Long
    @Query("SELECT * FROM categorias ORDER BY nome") suspend fun buscarTodos(): List<Categoria>
    @Query("SELECT * FROM categorias WHERE id = :id LIMIT 1") suspend fun buscarPorId(id: Int): Categoria?
    @Update suspend fun atualizar(item: Categoria): Unit
    @Delete suspend fun deletar(item: Categoria): Unit
}

@Database(entities = [Produto::class, Categoria::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun produtoDao(): ProdutoDao
    abstract fun categoriaDao(): CategoriaDao
    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE produtos ADD COLUMN imagemLocal TEXT DEFAULT NULL")
            }
        }

        @Volatile private var INSTANCE: AppDatabase? = null
        fun getDatabase(context: Context): AppDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "mercado_preso.db")
                .addMigrations(MIGRATION_1_2)
                .build().also { INSTANCE = it }
        }
    }
}

