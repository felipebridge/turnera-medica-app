package DAO;

import Exceptions.DAOException;
import Exceptions.NotFoundException;
import Modelo.Usuario;

import java.util.List;

public interface UsuarioDAO {
    int create(Usuario u) throws DAOException;

    void update(Usuario u) throws DAOException, NotFoundException;

    void delete(int id) throws DAOException, NotFoundException;

    Usuario findById(int id) throws DAOException, NotFoundException;

    List<Usuario> findAll() throws DAOException;
}
