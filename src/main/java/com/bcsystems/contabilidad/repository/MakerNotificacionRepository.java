package com.bcsystems.contabilidad.repository;
import com.bcsystems.contabilidad.domain.MakerNotificacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
public interface MakerNotificacionRepository extends JpaRepository<MakerNotificacion, Integer> {
    Page<MakerNotificacion> findByUsuarioIdUserOrderByFechaDesc(Integer usuarioId, Pageable pageable);
    long countByUsuarioIdUserAndLeidaFalse(Integer usuarioId);
    @Modifying @Query("update MakerNotificacion n set n.leida = true where n.usuario.idUser = :userId and n.leida = false")
    int markAllRead(@Param("userId") Integer userId);
}
