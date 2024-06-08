package com.github.dimplykhan13.reservadesala.reserva_de_sala.controllers;

import java.util.List;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import com.github.dimplykhan13.reservadesala.reserva_de_sala.models.*;
import com.github.dimplykhan13.reservadesala.reserva_de_sala.repositories.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ReservaDeSalaController {

    @Autowired
    private SalaRepository salaRepo;
    @Autowired
    private UsuarioRepository usuarioRepo;
    @Autowired
    private ReservaRepository reservaRepo;

    @GetMapping({ "", "/", "index" })
    public ModelAndView index(Model model) {

        ModelAndView view = getDefaultIndex();

        return view;
    }

    private ModelAndView getDefaultIndex() {
        ModelAndView view = new ModelAndView("index");

        List<Sala> salas = salaRepo.findAll();
        view.addObject("salas", salas);

        Disponibilidade disponibilidade = new Disponibilidade();
        view.addObject("disponibilidade", disponibilidade);

        Usuario usuario = new Usuario();
        view.addObject("usuario", usuario);

        return view;
    }

    @GetMapping("usuario/cadastro")
    public ModelAndView cadastro(Model model) {

        Usuario usuario = new Usuario();

        ModelAndView view = new ModelAndView("cadastroUsuario");
        view.addObject("usuario", usuario);

        return view;
    }

    @PostMapping("usuario/cadastro")
    public String addUsuario(
            Usuario usuario,
            Model model,
            BindingResult result) {
        List<Usuario> usuarios = usuarioRepo.findAll();

        for (Usuario u : usuarios) {
            if (u.getCpf().equals(usuario.getCpf())) {
                model.addAttribute("error", "CPF ja cadastrado");
                result.rejectValue("cpf", null, "CPF ja cadastrado");
                return "cadastroUsuario";
            }
        }
        try {
            usuarioRepo.save(usuario);
            model.addAttribute("success", "Usuário cadastrado com sucesso");
            return "redirect:/?success=" + URLEncoder.encode(model.asMap().get("success").toString(), "UTF-8");
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            result.reject("error", e.getMessage());
            return "cadastroUsuario";
        }

    }

    @PostMapping("reserva/disponibilidade")
    public ModelAndView checkDisponibilidade(Disponibilidade disponibilidade,
            Model model,
            BindingResult result) {

        if (disponibilidade.getCpf() == null) {
            model.addAttribute("error", "CPF inválido");
            result.rejectValue("cfp", null, "CPF Inválido");
            ModelAndView view = getDefaultIndex();
            return view;
        }

        List<Usuario> usuarios = usuarioRepo.findAll();

        if (!usuarios.stream().anyMatch(u -> u.getCpf().equals(disponibilidade.getCpf()))) {
            model.addAttribute("error", "CPF não encontrado");
            result.rejectValue("cpf", null, "CPF não encontrado");
            ModelAndView view = getDefaultIndex();
            return view;
        }

        List<Sala> salas = salaRepo.findAll();

        if (disponibilidade.getDia() == null || disponibilidade.getHora() == null) {
            model.addAttribute("error", "Dia inválido");
            result.rejectValue("dia", null, "Dia inválida");
            ModelAndView view = getDefaultIndex();
            return view;
        }
        List<Reserva> reservas = reservaRepo.findByDia(disponibilidade.getDia());

        reservas.removeIf(r -> !r.getHora().equals(disponibilidade.getHora()));

        List<Sala> salasDisponiveis = new ArrayList<Sala>();
        salasDisponiveis.addAll(salas);

        if (salasDisponiveis.isEmpty()) {
            model.addAttribute("error", "Nenhuma sala disponível");
            ModelAndView view = getDefaultIndex();
            return view;
        }

        salasDisponiveis.removeIf(s -> reservas.stream()
                .anyMatch(r -> r.getIdSala() == s.getId()));

        salasDisponiveis.removeIf(s -> s.getCapacidade() < disponibilidade.getParticipantes());

        if (salasDisponiveis.isEmpty()) {
            model.addAttribute("error", "Somente salas menores disponíveis");
            ModelAndView view = getDefaultIndex();
            return view;
        }

        Reserva reserva = new Reserva();
        ModelAndView view = new ModelAndView("disponibilidade");
        view.addObject("salasDisponiveis", salasDisponiveis);
        view.addObject("disponibilidade", disponibilidade);
        view.addObject("reserva", reserva);

        return view;
    }

    @PostMapping("reserva/sala")
    public String addReserva(Reserva reserva, Model model, BindingResult result) {

        try {
            reservaRepo.save(reserva);
            model.addAttribute("success", "Reserva efetuada com sucesso");
            return "redirect:/?success=" +
                    URLEncoder.encode(model.asMap().get("success").toString(), "UTF-8");
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            result.reject("error", e.getMessage());
            return "disponibilidade";
        }

    }

    @GetMapping("/reserva/usuario")
    public ModelAndView getReservas(String cpf, Model model) {

        Usuario usuario = usuarioRepo.findByCpf(cpf);

        if (usuario == null) {
            model.addAttribute("error", "CPF não foi encontrado");
            ModelAndView view = getDefaultIndex();
            return view;
        }

        List<Reserva> reservas = reservaRepo.findByCpfUsuario(usuario.getCpf());
        List<Sala> salas = salaRepo.findAll();

        ModelAndView view = new ModelAndView("reservasUsuario");
        view.addObject("reservas", reservas);
        view.addObject("usuario", usuario);
        view.addObject("salas", salas);
        return view;
    }

    @PutMapping("usuario/atualizar/{cpf}")
    public String putMethodName(@PathVariable("cpf") String cpf,
            Usuario usuario,
            Model model,
            BindingResult result) {

        usuario.setCpf(cpf);
        try {
            usuarioRepo.save(usuario);
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            result.reject("error", e.getMessage());
            return "/reserva/usuario?cpf=" + cpf;
        }

        return "redirect:/reserva/usuario?cpf=" + cpf;
    }

    @GetMapping("usuario/atualizar/{cpf}")
    public ModelAndView getMethodName(@PathVariable("cpf") String cpf, Model model) {

        ModelAndView view = getReservas(cpf, model);

        return view;
    }

    @DeleteMapping("reserva/remover/{id}")
    public ModelAndView deleteMethodName(@PathVariable("id") Long id, Model model) {

        try {
            reservaRepo.deleteById(id);
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            ModelAndView view = getDefaultIndex();
            return view;
        }
        model.addAttribute("success", "Reserva removida com sucesso");
        ModelAndView view = getDefaultIndex();
        return view;
    }

}