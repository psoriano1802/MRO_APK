package com.example.lacteos_flores.data

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [UsuarioEntity::class,  DoctosEntity::class, ClientsEntity::class,
        BancoEntity::class, GastosEntity::class, MonedaEntity::class, ListaPreciosEntity::class,
    ProductosEntity::class,ExistenciaEntity::class],
    version = 6
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao
    abstract fun doctosDao(): DoctosDao
    abstract fun gastosDao(): GastosDao
    abstract fun clientsDao(): ClientsDao
    abstract fun bancoDao(): BancosDao
    abstract fun monedaDao(): MonedaDao
   // abstract fun listaPreciosDao(): ListaPreciosDao
    abstract fun productosDao(): ProductosDao
    abstract fun existenciasDao(): ExistenciaDao



    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "db_local"
                ).addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
        private val MIGRATION_1_2 = object: Migration(1, 2) {
           override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("DROP TABLE IF EXISTS documentos")
               database.execSQL("DROP TABLE IF EXISTS documentos")
               database.execSQL("DROP TABLE IF EXISTS documentos")
               database.execSQL("DROP TABLE IF EXISTS documentos")
               database.execSQL("DROP TABLE IF EXISTS documentos")
               database.execSQL("DROP TABLE IF EXISTS documentos")
               database.execSQL("DROP TABLE IF EXISTS documentos")
           }
        }
    }
}
