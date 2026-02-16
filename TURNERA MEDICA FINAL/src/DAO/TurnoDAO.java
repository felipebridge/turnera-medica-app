package DAO;

import Exceptions.DAOException;
import Exceptions.NotFoundException;
import Modelo.Turno;

import java.time.LocalDate;
import java.util.List;

public interface TurnoDAO {

    int create(Turno t) throws DAOException;

    void update(Turno t) throws DAOException, NotFoundException;

    void delete(int id) throws DAOException, NotFoundException;

    Turno findById(int id) throws DAOException, NotFoundException;

    List<Turno> findAll() throws DAOException;

    boolean existsTurnoMedicoFechaHora(int medicoId, LocalDate fecha, String hora) throws DAOException;

    // Reportes
    List<Turno> findBetweenDates(LocalDate desde, LocalDate hasta) throws DAOException;

    List<Turno> findByMedicoBetweenDates(int medicoId, LocalDate desde, LocalDate hasta) throws DAOException;
}
