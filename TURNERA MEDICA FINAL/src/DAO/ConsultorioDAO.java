package DAO;

import Exceptions.DAOException;
import Exceptions.NotFoundException;
import Modelo.Consultorio;

import java.util.List;

public interface ConsultorioDAO {
    int create(Consultorio consultorio) throws DAOException;
    void update(Consultorio consultorio) throws DAOException, NotFoundException;
    void delete(int id) throws DAOException, NotFoundException;

    Consultorio findById(int id) throws DAOException, NotFoundException;
    List<Consultorio> findAll() throws DAOException;
}
