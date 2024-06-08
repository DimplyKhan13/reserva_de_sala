package com.github.dimplykhan13.reservadesala.reserva_de_sala.repositories;

import com.github.dimplykhan13.reservadesala.reserva_de_sala.models.Reserva;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    List<Reserva> findByCpfUsuario(String cpf);

    List<Reserva> findByDia(String dia);
}
