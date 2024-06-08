package com.github.dimplykhan13.reservadesala.reserva_de_sala.repositories;

import com.github.dimplykhan13.reservadesala.reserva_de_sala.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, String> {

    public Usuario findByCpf(String cpf);

}
