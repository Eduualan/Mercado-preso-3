package com.example.mercadopreso.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.mercadopreso.data.local.dao.CategoriaDao
import com.example.mercadopreso.data.local.dao.ProdutoDao
import com.example.mercadopreso.data.local.dao.EstadoCatalogoDao
import com.example.mercadopreso.data.local.entity.Categoria
import com.example.mercadopreso.data.local.entity.Produto
import com.example.mercadopreso.data.local.entity.EstadoCatalogo

@Database(entities = [Produto::class, Categoria::class, EstadoCatalogo::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun produtoDao(): ProdutoDao
    abstract fun categoriaDao(): CategoriaDao
    abstract fun estadoCatalogoDao(): EstadoCatalogoDao

    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE produtos ADD COLUMN imagemLocal TEXT DEFAULT NULL")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `estado_catalogo` (`id` INTEGER NOT NULL, `inicializado` INTEGER NOT NULL, PRIMARY KEY(`id`))")
                // Bancos já existentes já tiveram a carga inicial. Não repor itens que o usuário apagou.
                db.execSQL("INSERT OR REPLACE INTO `estado_catalogo` (`id`, `inicializado`) VALUES (1, 1)")
            }
        }

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "mercado_preso.db"
            ).addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .build()
                .also { INSTANCE = it }
        }
    }
}
