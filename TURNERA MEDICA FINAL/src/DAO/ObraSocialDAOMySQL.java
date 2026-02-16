package DAO;

import Config.ConexionMySQL;
import Exceptions.DAOException;
import Exceptions.NotFoundException;
import Modelo.ObraSocial;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ObraSocialDAOMySQL implements ObraSocialDAO {

    @Override
    public int create(ObraSocial obraSocial) throws DAOException {
        String sql = "INSERT INTO obra_social (nombre, descuento) VALUES (?, ?)";

        try (Connection conn = ConexionMySQL.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, obraSocial.getNombre());
            ps.setDouble(2, obraSocial.getDescuento());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    obraSocial.setId(id);
                    return id;
                }
            }
            throw new DAOException("No se pudo obtener el ID generado de ObraSocial.");
        } catch (SQLException e) {
            throw new DAOException("Error creando ObraSocial", e);
        }
    }

    @Override
    public void update(ObraSocial obraSocial) throws DAOException, NotFoundException {
        String sql = "UPDATE obra_social SET nombre = ?, descuento = ? WHERE id = ?";

        try (Connection conn = ConexionMySQL.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, obraSocial.getNombre());
            ps.setDouble(2, obraSocial.getDescuento());
            ps.setInt(3, obraSocial.getId());

            int filas = ps.executeUpdate();
            if (filas == 0) {
                throw new NotFoundException("No existe ObraSocial con id=" + obraSocial.getId());
            }
        } catch (SQLException e) {
            throw new DAOException("Error actualizando ObraSocial", e);
        }
    }

    @Override
    public void delete(int id) throws DAOException, NotFoundException {
        String sql = "DELETE FROM obra_social WHERE id = ?";

        try (Connection conn = ConexionMySQL.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            int filas = ps.executeUpdate();
            if (filas == 0) {
                throw new NotFoundException("No existe ObraSocial con id=" + id);
            }
        } catch (SQLException e) {
            throw new DAOException("Error eliminando ObraSocial", e);
        }
    }

    @Override
    public ObraSocial findById(int id) throws DAOException, NotFoundException {
        String sql = "SELECT id, nombre, descuento FROM obra_social WHERE id = ?";

        try (Connection conn = ConexionMySQL.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
            throw new NotFoundException("No existe ObraSocial con id=" + id);
        } catch (SQLException e) {
            throw new DAOException("Error buscando ObraSocial por id", e);
        }
    }

    @Override
    public List<ObraSocial> findAll() throws DAOException {
        String sql = "SELECT id, nombre, descuento FROM obra_social ORDER BY nombre";
        List<ObraSocial> lista = new ArrayList<>();

        try (Connection conn = ConexionMySQL.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapRow(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw new DAOException("Error listando Obras Sociales", e);
        }
    }

    private ObraSocial mapRow(ResultSet rs) throws SQLException {
        return new ObraSocial(
                rs.getInt("id"),
                rs.getString("nombre"),
                rs.getDouble("descuento")
        );
    }
}
