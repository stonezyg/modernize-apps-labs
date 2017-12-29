package com.redhat.coolstore.service;

import java.util.List;

import com.redhat.coolstore.model.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/services")
public class CatalogEndpoint {

    @Autowired
    private CatalogService catalogService;

    @ResponseBody
    @GetMapping("/products")
    public ResponseEntity<List<Product>> readAll() {
        return new ResponseEntity<List<Product>>(catalogService.readAll(),HttpStatus.OK);
    }

    @ResponseBody
    @GetMapping("/product/{id}")
    public ResponseEntity<Product> read(@PathVariable("id") String id) {
        return new ResponseEntity<Product>(catalogService.read(id),HttpStatus.OK);
    }

    @ResponseBody
    @PutMapping("/product")
    public ResponseEntity<Void> update(@RequestBody Product product) {
        catalogService.update(product);
        return new ResponseEntity<Void>(HttpStatus.OK);
    }

    @ResponseBody
    @PostMapping("/product/new")
    public ResponseEntity<Void> create(@RequestBody Product product) {
        catalogService.insert(product);
        return new ResponseEntity<Void>(HttpStatus.OK);
    }

    @ResponseBody
    @DeleteMapping("/product/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") String id) {
        catalogService.delete(id);
        return new ResponseEntity<Void>(HttpStatus.OK);
    }

}
