package DAO;

import Exceptions.DAOException;
import Exceptions.NotFoundException;
import Modelo.Paciente;

import java.util.List;

public interface PacienteDAO {
    int create(Paciente paciente) throws DAOException;
    void update(Paciente paciente) throws DAOException, NotFoundException;
    void delete(int id) throws DAOException, NotFoundException;

    Paciente findById(int id) throws DAOException, NotFoundException;
    List<Paciente> findAll() throws DAOException;
}
