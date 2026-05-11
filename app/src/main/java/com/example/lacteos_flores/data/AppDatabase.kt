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
    ProductosEntity::class,ExistenciaEntity::class,Kdm1Entity::class,Kdm2Entity::class,ItemAuxEntity::class, CarteraEntity::class],
    version = 12
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao
    abstract fun doctosDao(): DoctosDao
    abstract fun gastosDao(): GastosDao
    abstract fun clientsDao(): ClientsDao
    abstract fun bancoDao(): BancosDao
    abstract fun monedaDao(): MonedaDao
    abstract fun listaPreciosDao(): ListaPrecioDao
    abstract fun productosDao(): ProductosDao
    abstract fun existenciasDao(): ExistenciaDao
    abstract fun kdm1Dao(): Kdm1Dao
    abstract fun kdm2Dao(): Kdm2Dao
    abstract fun itemAuxDao(): ItemAuxDao
    abstract fun carteraDao(): CarteraDao





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
            database.execSQL("DROP TABLE IF EXISTS documentos")//tabla para los documentos a utilizar configuracion
               database.execSQL("DROP TABLE IF EXISTS bancos")//catalogo bancos
               database.execSQL("DROP TABLE IF EXISTS gastos")//catalogo gastos
               database.execSQL("DROP TABLE IF EXISTS productos")//lista de productos
               database.execSQL("DROP TABLE IF EXISTS existencias")//existencias de productos
               database.execSQL("DROP TABLE IF EXISTS clientes")//catalogo clientes
               database.execSQL("DROP TABLE IF EXISTS monedas")//Catalogo de monedas
               database.execSQL("DROP TABLE IF EXISTS listaprecios")//lista de precios
               database.execSQL("DROP TABLE IF EXISTS kdm1_doctos")//tabla para los encabezados de documentos equivalente a kdm1
               database.execSQL("DROP TABLE IF EXISTS kdm2_partidas")//tabla para los detalles de documentos equivalente a kdm2
               database.execSQL("DROP TABLE IF EXISTS itemAux")//tabla para existencias de controles auxiliares equivalente kdij
               database.execSQL("DROP TABLE IF EXISTS cartera")//tabla para cartera de clientes(documentos vencidos)
           }
        }
    }
}
