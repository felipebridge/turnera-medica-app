package DAO;

import Config.ConexionMySQL;
import Exceptions.DAOException;
import Exceptions.NotFoundException;
import Modelo.Usuario;
import Modelo.UsuarioSimple;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAOMySQL implements UsuarioDAO {

    @Override
    public int create(Usuario u) throws DAOException {
        String sql = "INSERT INTO usuario (tipo, activo) VALUES (?, ?)";

        try (Connection conn = ConexionMySQL.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, u.getTipoUsuario());
            ps.setBoolean(2, u.isActivo());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
            throw new DAOException("No se pudo obtener el ID generado.");

        } catch (SQLException e) {
            throw new DAOException("Error creando usuario", e);
        }
    }

    @Override
    public void update(Usuario u) throws DAOException, NotFoundException {
        String sql = "UPDATE usuario SET tipo = ?, activo = ? WHERE id = ?";

        try (Connection conn = ConexionMySQL.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, u.getTipoUsuario());
            ps.setBoolean(2, u.isActivo());
            ps.setInt(3, u.getId());

            int updated = ps.executeUpdate();
            if (updated == 0) throw new NotFoundException("Usuario no encontrado (ID " + u.getId() + ")");

        } catch (SQLException e) {
            throw new DAOException("Error actualizando usuario", e);
        }
    }

    @Override
    public void delete(int id) throws DAOException, NotFoundException {
        String sql = "DELETE FROM usuario WHERE id = ?";

        try (Connection conn = ConexionMySQL.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            int deleted = ps.executeUpdate();
            if (deleted == 0) throw new NotFoundException("Usuario no encontrado (ID " + id + ")");

        } catch (SQLException e) {
            throw new DAOException("Error eliminando usuario", e);
        }
    }

    @Override
    public Usuario findById(int id) throws DAOException, NotFoundException {
        String sql = "SELECT id, tipo, activo FROM usuario WHERE id = ?";

        try (Connection conn = ConexionMySQL.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new NotFoundException("Usuario no encontrado (ID " + id + ")");
                return mapRow(rs);
            }

        } catch (SQLException e) {
            throw new DAOException("Error buscando usuario por ID", e);
        }
    }

    @Override
    public List<Usuario> findAll() throws DAOException {
        String sql = "SELECT id, tipo, activo FROM usuario ORDER BY id";
        List<Usuario> lista = new ArrayList<>();

        try (Connection conn = ConexionMySQL.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) lista.add(mapRow(rs));
            return lista;

        } catch (SQLException e) {
            throw new DAOException("Error listando usuarios", e);
        }
    }

    private Usuario mapRow(ResultSet rs) throws SQLException {
        return new UsuarioSimple(
                rs.getInt("id"),
                rs.getString("tipo"),
                rs.getBoolean("activo")
        );
    }
}
