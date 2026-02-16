package Service;

import DAO.PacienteDAO;
import DAO.PacienteDAOMySQL;
import Exceptions.DAOException;
import Exceptions.NotFoundException;
import Exceptions.ValidationException;
import Modelo.Paciente;

import java.util.List;

public class PacienteService {

    private final PacienteDAO pacienteDAO;

    public PacienteService() {
        this.pacienteDAO = new PacienteDAOMySQL();
    }

    public int crear(Paciente p) throws ValidationException, DAOException {
        validar(p);
        return pacienteDAO.create(p);
    }

    public void actualizar(Paciente p) throws ValidationException, DAOException, NotFoundException {
        if (p == null) throw new ValidationException("Paciente no puede ser null.");
        if (p.getId() <= 0) throw new ValidationException("ID inválido.");
        validar(p);
        pacienteDAO.update(p);
    }

    public void eliminar(int id) throws ValidationException, DAOException, NotFoundException {
        if (id <= 0) throw new ValidationException("ID inválido.");
        pacienteDAO.delete(id);
    }

    public Paciente buscarPorId(int id) throws ValidationException, DAOException, NotFoundException {
        if (id <= 0) throw new ValidationException("ID inválido.");
        return pacienteDAO.findById(id);
    }

    public List<Paciente> listar() throws DAOException {
        return pacienteDAO.findAll();
    }

    private void validar(Paciente p) throws ValidationException {
        if (p == null) throw new ValidationException("Paciente no puede ser null.");

        if (p.getDni() == null || p.getDni().trim().isEmpty())
            throw new ValidationException("DNI obligatorio.");

        String dni = p.getDni().trim();
        if (!dni.matches("\\d{7,9}")) {
            throw new ValidationException("DNI inválido. Debe tener 7 a 9 dígitos.");
        }

        if (p.getTelefono() != null && !p.getTelefono().trim().isEmpty()) {
            String tel = p.getTelefono().trim();
            if (!tel.matches("[0-9+()\\-\\s]{6,20}")) {
                throw new ValidationException("Teléfono inválido.");
            }
        }

        if (p.getEmail() != null && !p.getEmail().trim().isEmpty()) {
            String email = p.getEmail().trim();
            if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
                throw new ValidationException("Email inválido.");
            }
        }

    }
}
