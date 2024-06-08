package com.github.dimplykhan13.reservadesala.reserva_de_sala.repositories;

import com.github.dimplykhan13.reservadesala.reserva_de_sala.models.Sala;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SalaRepository extends JpaRepository<Sala, Long> {

}
