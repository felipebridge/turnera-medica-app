package DAO;

import Exceptions.DAOException;
import Exceptions.NotFoundException;
import Modelo.Medico;

import java.util.List;

public interface MedicoDAO {
    int create(Medico medico) throws DAOException;
    void update(Medico medico) throws DAOException, NotFoundException;
    void delete(int id) throws DAOException, NotFoundException;

    Medico findById(int id) throws DAOException, NotFoundException;
    List<Medico> findAll() throws DAOException;
}
