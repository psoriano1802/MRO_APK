package com.example.lacteos_flores.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.lacteos_flores.models.DocumentoHistorial


@Dao
interface Kdm1Dao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertaDocumento(docto: Kdm1Entity): Long

    @Query("SELECT SUM(CAST(monto AS DOUBLE)) FROM kdm1_doctos WHERE cliente =:cli and staSinc = 'N'")
    suspend fun obtenerMovimiento(cli: String): Double?


    @Query("SELECT * FROM kdm1_doctos ")
    suspend fun obtenerMovimientos(): List<Kdm1Entity>

    @Query("UPDATE kdm1_doctos SET staSinc = :status, folioKepler = :folio WHERE id = :id")
    suspend fun actualizarSincronizacion(id: Long, status: String, folio: String?)

    @Query("SELECT * FROM kdm1_doctos WHERE id = :id")
    suspend fun obtenerDocumentoPorId(id: Long): Kdm1Entity?

    @Query("DELETE FROM kdm1_doctos")
    suspend fun eliminarTodoMovimiento()

    @Query("""
        SELECT k.id, k.gen, k.nat, k.grp, k.tip, k.staSinc, k.folioKepler, k.fecha, k.cliente, k.monto, d.descripcion
        FROM kdm1_doctos k
        LEFT JOIN documentos d ON k.gen = d.gen AND k.nat = d.nat AND k.grp = d.grp AND k.tip = d.tipo
    """)
    suspend fun obtenerHistorialDocumentos(): List<DocumentoHistorial>
}
