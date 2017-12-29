package com.redhat.coolstore.service;

import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

import com.redhat.coolstore.model.Product;

import static org.assertj.core.api.Assertions.not;

@RunWith(SpringRunner.class)
@SpringBootTest()
public class CatalogRepositoryTest {

    @Autowired
    CatalogRepository repository;

    @Test
    public void test_read() {
        Product product = repository.findById("444434");
        assertThat(product).isNotNull();
        assertThat(product.getName()).as("Verify product name").isEqualTo("Pebble Smart Watch");
    }

    @Test
    public void test_readAll_list() {
        List<Product> productList = repository.readAll();
        assertThat(productList).isNotNull();
        assertThat(productList).isNotEmpty();
        List<String> names = productList.stream().map(Product::getName).collect(Collectors.toList());
        assertThat(names).contains("Red Fedora","Forge Laptop Sticker","Oculus Rift");
    }

    @Test
    public void test_insert_and_delete() {
        Product newProduct = new Product();
        newProduct.setItemId("9999");
        newProduct.setName("Test Proudct");
        newProduct.setPrice(100.0);
        newProduct.setDesc("This is just a test proudct");

        List<Product> productList = repository.readAll();
        assertThat(productList).isNotNull();
        assertThat(productList).isNotEmpty();
        int currentSize = repository.readAll().size();

        repository.insert(newProduct);
        
        productList = repository.readAll();
        assertThat(productList).isNotNull();
        assertThat(productList).isNotEmpty();
        assertThat(productList.size()).as("Product List size after adding one").isEqualTo(currentSize+1);

        assertThat(repository.delete("9999")).as("Delete test proudct").isTrue();

        productList = repository.readAll();
        assertThat(productList).isNotNull();
        assertThat(productList).isNotEmpty();
        assertThat(productList.size()).as("Product List size after removing temporary").isEqualTo(currentSize);

    }
}
