package Service;

import DAO.UsuarioDAO;
import DAO.UsuarioDAOMySQL;
import Exceptions.DAOException;
import Exceptions.NotFoundException;
import Exceptions.ValidationException;
import Modelo.Usuario;
import Modelo.UsuarioSimple;

import java.util.List;

public class UsuarioService {

    private final UsuarioDAO usuarioDAO;

    public UsuarioService() {
        this.usuarioDAO = new UsuarioDAOMySQL();
    }

    public int crear(Usuario u) throws ValidationException, DAOException {
        validar(u);
        return usuarioDAO.create(u);
    }

    public void actualizar(Usuario u) throws ValidationException, DAOException, NotFoundException {
        if (u == null) throw new ValidationException("Usuario no puede ser null.");
        if (u.getId() <= 0) throw new ValidationException("ID inválido.");
        validar(u);
        usuarioDAO.update(u);
    }

    public void eliminar(int id) throws ValidationException, DAOException, NotFoundException {
        if (id <= 0) throw new ValidationException("ID inválido.");
        usuarioDAO.delete(id);
    }

    public Usuario buscarPorId(int id) throws ValidationException, DAOException, NotFoundException {
        if (id <= 0) throw new ValidationException("ID inválido.");
        return usuarioDAO.findById(id);
    }

    public List<Usuario> listar() throws DAOException {
        return usuarioDAO.findAll();
    }

    private void validar(Usuario u) throws ValidationException {
        if (u == null) throw new ValidationException("Usuario no puede ser null.");

        String tipo;

        if (u instanceof UsuarioSimple us) {
            tipo = us.getTipo();
        } else {
            tipo = u.getTipoUsuario();
        }

        if (tipo == null || tipo.trim().isEmpty()) {
            throw new ValidationException("Tipo de usuario obligatorio.");
        }
        tipo = tipo.trim().toUpperCase();

        if (!tipo.equals("MEDICO") && !tipo.equals("PACIENTE") && !tipo.equals("ADMIN")) {
            throw new ValidationException("Tipo de usuario inválido: " + tipo);
        }
    }
}
