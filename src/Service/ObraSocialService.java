package Service;

import DAO.ObraSocialDAO;
import DAO.ObraSocialDAOMySQL;
import Exceptions.DAOException;
import Exceptions.NotFoundException;
import Exceptions.ValidationException;
import Modelo.ObraSocial;

import java.util.List;

public class ObraSocialService {

    private final ObraSocialDAO obraSocialDAO;

    public ObraSocialService() {
        this.obraSocialDAO = new ObraSocialDAOMySQL();
    }

    public int crear(ObraSocial os) throws ValidationException, DAOException {
        validar(os);
        return obraSocialDAO.create(os);
    }

    public void actualizar(ObraSocial os) throws ValidationException, DAOException, NotFoundException {
        if (os.getId() <= 0) throw new ValidationException("ID inválido para actualizar.");
        validar(os);
        obraSocialDAO.update(os);
    }

    public void eliminar(int id) throws ValidationException, DAOException, NotFoundException {
        if (id <= 0) throw new ValidationException("ID inválido para eliminar.");
        obraSocialDAO.delete(id);
    }

    public ObraSocial buscarPorId(int id) throws ValidationException, DAOException, NotFoundException {
        if (id <= 0) throw new ValidationException("ID inválido para buscar.");
        return obraSocialDAO.findById(id);
    }

    public List<ObraSocial> listar() throws DAOException {
        return obraSocialDAO.findAll();
    }

    private void validar(ObraSocial os) throws ValidationException {
        if (os == null) throw new ValidationException("ObraSocial no puede ser null.");
        if (os.getNombre() == null || os.getNombre().trim().isEmpty())
            throw new ValidationException("El nombre es obligatorio.");
        if (os.getDescuento() < 0 || os.getDescuento() > 100)
            throw new ValidationException("El descuento debe estar entre 0 y 100.");
    }
}
