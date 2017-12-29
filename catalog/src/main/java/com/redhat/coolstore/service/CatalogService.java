package com.redhat.coolstore.service;

import com.redhat.coolstore.model.Inventory;
import com.redhat.coolstore.model.Product;
import feign.hystrix.FallbackFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CatalogService {

    @Autowired
    private CatalogRepository repository;

    @Autowired
    InventoryClient inventoryClient;

    public Product read(String id) {
        Product product = repository.findById(id);
        product.setQuantity(inventoryClient.getInventoryStatus(product.getItemId()).getQuantity());
        return product;
    }

    public List<Product> readAll() {

        List<Product> productList = repository.readAll();
        return productList.parallelStream()
                .map(p -> {
                    p.setQuantity(inventoryClient.getInventoryStatus(p.getItemId()).getQuantity());
                    return p;
                })
                .collect(Collectors.toList());
    }

    public void update(Product product) {
        repository.update(product);
    }

    public void insert(Product product) {
        repository.insert(product);
    }

    public void delete(String id) {
        repository.delete(id);
    }

    @FeignClient(name="inventory",fallbackFactory = InventoryClientFallbackFactory.class)
    protected interface InventoryClient {

        @RequestMapping(method = RequestMethod.GET, value = "/services/inventory/{itemId}", consumes = {MediaType.APPLICATION_JSON_VALUE})
        Inventory getInventoryStatus(@PathVariable("itemId") String itemId);

    }

    @Component
    static class InventoryClientFallbackFactory implements FallbackFactory<InventoryClient> {
        @Override
        public InventoryClient create(Throwable cause) {
            return new InventoryClient() {
                @Override
                public Inventory getInventoryStatus(@PathVariable("itemId") String itemId) {
                    return new Inventory(itemId,-1);
                }
            };
        }
    }


}


