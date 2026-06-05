package mx.gob.imss.medgemma.repository;

import mx.gob.imss.medgemma.entity.PamcCostoProcedimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PamcCostoProcedimientoRepository extends JpaRepository<PamcCostoProcedimiento, Integer> {
    List<PamcCostoProcedimiento> findByIndActivoTrue();
}
