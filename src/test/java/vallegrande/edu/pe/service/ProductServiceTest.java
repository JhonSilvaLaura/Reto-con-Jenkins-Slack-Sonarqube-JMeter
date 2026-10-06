package vallegrande.edu.pe.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vallegrande.edu.pe.model.Product;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProductServiceTest {

    private ProductService productService;

    @BeforeEach
    void setUp() {
        productService = new ProductService();
    }

    @Test
    void getProductsDebeRetornarCincoProductos() {
        List<Product> products = productService.getProducts();
        assertEquals(5, products.size());
        assertEquals("Laptop", products.get(0).getName());
    }

    @Test
    void processProductConNombreValido() {
        assertEquals("Producto procesado: Mouse", productService.processProduct("Mouse"));
    }

    @Test
    void processProductConNombreVacio() {
        assertEquals("Producto vacío", productService.processProduct(""));
    }

    @Test
    void processProductConNombreNulo() {
        assertEquals("Producto no válido", productService.processProduct(null));
    }

    @Test
    void validateProductConProductoValido() {
        assertTrue(productService.validateProduct(new Product(1, "Laptop", 2500.0)));
    }

    @Test
    void validateProductSinNombre() {
        assertFalse(productService.validateProduct(new Product(1, null, 100.0)));
    }

    @Test
    void validateProductConPrecioCero() {
        assertFalse(productService.validateProduct(new Product(1, "Laptop", 0.0)));
    }

    @Test
    void validateProductNulo() {
        assertFalse(productService.validateProduct(null));
    }
}
