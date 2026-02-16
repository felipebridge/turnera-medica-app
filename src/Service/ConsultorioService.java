package Service;

import DAO.ConsultorioDAO;
import DAO.ConsultorioDAOMySQL;
import Exceptions.DAOException;
import Exceptions.NotFoundException;
import Exceptions.ValidationException;
import Modelo.Consultorio;

import java.util.List;

public class ConsultorioService {

    private final ConsultorioDAO consultorioDAO;

    public ConsultorioService() {
        this.consultorioDAO = new ConsultorioDAOMySQL();
    }

    public int crear(Consultorio c) throws ValidationException, DAOException {
        validar(c);
        return consultorioDAO.create(c);
    }

    public void actualizar(Consultorio c) throws ValidationException, DAOException, NotFoundException {
        if (c.getId() <= 0) throw new ValidationException("ID inválido.");
        validar(c);
        consultorioDAO.update(c);
    }

    public void eliminar(int id) throws ValidationException, DAOException, NotFoundException {
        if (id <= 0) throw new ValidationException("ID inválido.");
        consultorioDAO.delete(id);
    }

    public Consultorio buscarPorId(int id) throws ValidationException, DAOException, NotFoundException {
        if (id <= 0) throw new ValidationException("ID inválido.");
        return consultorioDAO.findById(id);
    }

    public List<Consultorio> listar() throws DAOException {
        return consultorioDAO.findAll();
    }

    private void validar(Consultorio c) throws ValidationException {
        if (c == null) throw new ValidationException("Consultorio no puede ser null.");
        if (c.getNumero() == null || c.getNumero().trim().isEmpty())
            throw new ValidationException("El número es obligatorio.");
    }
}
