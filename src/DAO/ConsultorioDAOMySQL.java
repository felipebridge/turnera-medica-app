package DAO;

import Config.ConexionMySQL;
import Exceptions.DAOException;
import Exceptions.NotFoundException;
import Modelo.Consultorio;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ConsultorioDAOMySQL implements ConsultorioDAO {

    @Override
    public int create(Consultorio c) throws DAOException {
        String sql = "INSERT INTO consultorio (numero, piso, descripcion) VALUES (?, ?, ?)";

        try (Connection conn = ConexionMySQL.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, c.getNumero());
            ps.setString(2, c.getPiso());
            ps.setString(3, c.getDescripcion());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    c.setId(id);
                    return id;
                }
            }
            throw new DAOException("No se pudo obtener el ID generado de Consultorio.");
        } catch (SQLException e) {
            throw new DAOException("Error creando Consultorio.", e);
        }
    }

    @Override
    public void update(Consultorio c) throws DAOException, NotFoundException {
        String sql = "UPDATE consultorio SET numero = ?, piso = ?, descripcion = ? WHERE id = ?";

        try (Connection conn = ConexionMySQL.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, c.getNumero());
            ps.setString(2, c.getPiso());
            ps.setString(3, c.getDescripcion());
            ps.setInt(4, c.getId());

            int filas = ps.executeUpdate();
            if (filas == 0) throw new NotFoundException("No existe Consultorio con id=" + c.getId());
        } catch (SQLException e) {
            throw new DAOException("Error actualizando Consultorio.", e);
        }
    }

    @Override
    public void delete(int id) throws DAOException, NotFoundException {
        String sql = "DELETE FROM consultorio WHERE id = ?";

        try (Connection conn = ConexionMySQL.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            int filas = ps.executeUpdate();
            if (filas == 0) throw new NotFoundException("No existe Consultorio con id=" + id);
        } catch (SQLException e) {
            throw new DAOException("Error eliminando Consultorio.", e);
        }
    }

    @Override
    public Consultorio findById(int id) throws DAOException, NotFoundException {
        String sql = "SELECT id, numero, piso, descripcion FROM consultorio WHERE id = ?";

        try (Connection conn = ConexionMySQL.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }

            throw new NotFoundException("No existe Consultorio con id=" + id);
        } catch (SQLException e) {
            throw new DAOException("Error buscando Consultorio por id.", e);
        }
    }

    @Override
    public List<Consultorio> findAll() throws DAOException {
        String sql = "SELECT id, numero, piso, descripcion FROM consultorio ORDER BY numero";
        List<Consultorio> lista = new ArrayList<>();

        try (Connection conn = ConexionMySQL.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) lista.add(mapRow(rs));
            return lista;
        } catch (SQLException e) {
            throw new DAOException("Error listando Consultorios.", e);
        }
    }

    private Consultorio mapRow(ResultSet rs) throws SQLException {
        return new Consultorio(
                rs.getInt("id"),
                rs.getString("numero"),
                rs.getString("piso"),
                rs.getString("descripcion")
        );
    }
}
