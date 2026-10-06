package vallegrande.edu.pe.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ModelTest {

    @Test
    void productGettersAndSetters() {
        Product product = new Product();
        product.setId(9);
        product.setName("Impresora");
        product.setPrice(999.99);

        assertEquals(9, product.getId());
        assertEquals("Impresora", product.getName());
        assertEquals(999.99, product.getPrice());

        Product otro = new Product(2, "Mouse", 80.0);
        assertEquals(2, otro.getId());
        assertEquals("Mouse", otro.getName());
        assertEquals(80.0, otro.getPrice());
    }

    @Test
    void loginRequestGettersAndSetters() {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("123456");

        assertEquals("admin", request.getUsername());
        assertEquals("123456", request.getPassword());
    }

    @Test
    void productControllerRetornaLista() {
        vallegrande.edu.pe.controller.ProductController controller =
                new vallegrande.edu.pe.controller.ProductController(new vallegrande.edu.pe.service.ProductService());
        assertEquals(5, controller.getProducts().size());
    }
}
