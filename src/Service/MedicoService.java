package Service;

import DAO.MedicoDAO;
import DAO.MedicoDAOMySQL;
import Exceptions.DAOException;
import Exceptions.NotFoundException;
import Exceptions.ValidationException;
import Modelo.Medico;

import java.util.List;

public class MedicoService {

    private final MedicoDAO medicoDAO;

    public MedicoService() {
        this.medicoDAO = new MedicoDAOMySQL();
    }

    public int crear(Medico m) throws ValidationException, DAOException {
        validar(m);
        return medicoDAO.create(m);
    }

    public void actualizar(Medico m) throws ValidationException, DAOException, NotFoundException {
        if (m == null) throw new ValidationException("Médico no puede ser null.");
        if (m.getId() <= 0) throw new ValidationException("ID inválido.");
        validar(m);
        medicoDAO.update(m);
    }

    public void eliminar(int id) throws ValidationException, DAOException, NotFoundException {
        if (id <= 0) throw new ValidationException("ID inválido.");
        medicoDAO.delete(id);
    }

    public Medico buscarPorId(int id) throws ValidationException, DAOException, NotFoundException {
        if (id <= 0) throw new ValidationException("ID inválido.");
        return medicoDAO.findById(id);
    }

    public List<Medico> listar() throws DAOException {
        return medicoDAO.findAll();
    }

    private void validar(Medico m) throws ValidationException {
        if (m == null) throw new ValidationException("Médico no puede ser null.");

        if (m.getNombre() == null || m.getNombre().trim().isEmpty())
            throw new ValidationException("Nombre obligatorio.");

        if (m.getApellido() == null || m.getApellido().trim().isEmpty())
            throw new ValidationException("Apellido obligatorio.");

        if (m.getMatricula() == null || m.getMatricula().trim().isEmpty())
            throw new ValidationException("Matrícula obligatoria.");

        if (m.getEspecialidad() == null || m.getEspecialidad().trim().isEmpty())
            throw new ValidationException("Especialidad obligatoria.");
    }
}
