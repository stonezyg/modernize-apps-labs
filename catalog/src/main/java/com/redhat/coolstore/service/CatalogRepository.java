package com.redhat.coolstore.service;

import java.util.List;

import com.redhat.coolstore.model.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

@Repository
public class CatalogRepository {

    private JdbcTemplate jdbcTemplate;

    private RowMapper<Product> rowMapper = (rs, rowNum) -> new Product(
            rs.getString("itemId"),
            rs.getString("name"),
            rs.getString("description"),
            rs.getDouble("price"));

    @Autowired
    public CatalogRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    public List<Product> readAll() {
        return jdbcTemplate.query("SELECT * FROM catalog", rowMapper);
    }

    public Product findById(String id) {
        return jdbcTemplate.queryForObject("SELECT * FROM catalog WHERE itemId = " + id, rowMapper);
    }

    public void insert(Product product) {
        jdbcTemplate.update("INSERT INTO catalog (itemId, name, description, price) VALUES (?, ?, ?, ?)", product.getItemId(),
                product.getName(), product.getDesc(), product.getPrice());
    }

    public boolean update(Product product) {
//        throw new NotImplementedException(); //TODO: Implement update method
        int update = jdbcTemplate.update("UPDATE catalog SET name = ?, price = ?, description = ? WHERE itemId = ? ", product.getName(), product.getPrice(), product.getDesc(), product.getItemId());
        return (update > 0);
    }

    public boolean delete(String id) {
        //throw new NotImplementedException(); //TODO: Implement Delete method
        int update = jdbcTemplate.update("DELETE FROM catalog WHERE itemId = " + id);
        return (update > 0);
    }

}