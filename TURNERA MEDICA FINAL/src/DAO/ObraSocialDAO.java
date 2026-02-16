package DAO;

import Exceptions.DAOException;
import Exceptions.NotFoundException;
import Modelo.ObraSocial;

import java.util.List;

public interface ObraSocialDAO {
    int create(ObraSocial obraSocial) throws DAOException;
    void update(ObraSocial obraSocial) throws DAOException, NotFoundException;
    void delete(int id) throws DAOException, NotFoundException;

    ObraSocial findById(int id) throws DAOException, NotFoundException;
    List<ObraSocial> findAll() throws DAOException;
}
