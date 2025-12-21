package com.example.logging_and_observability.service;

import com.example.logging_and_observability.execption.ProductAlreadyExistsException;
import com.example.logging_and_observability.execption.ProductNotFoundException;
import com.example.logging_and_observability.model.Product;
import com.example.logging_and_observability.model.User;
import com.example.logging_and_observability.repository.ProductRepository;
import com.example.logging_and_observability.profiling.model.UserOperationType;
import com.example.logging_and_observability.profiling.service.UserProfileService;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * ProductService with enhanced logging and OpenTelemetry tracing
 */
@Service
public class ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;
    private final UserProfileService userProfileService;
    private final Tracer tracer;

    private static final ThreadLocal<User> currentUserContext = new ThreadLocal<>();

    // ✅ Constructeur avec OpenTelemetry
    public ProductService(ProductRepository productRepository,
                          UserProfileService userProfileService,
                          OpenTelemetry openTelemetry) {
        this.productRepository = productRepository;
        this.userProfileService = userProfileService;
        this.tracer = openTelemetry.getTracer("product-management-backend", "1.0.0");
    }

    public void setCurrentUser(User user) {
        currentUserContext.set(user);
        logger.info("User context set for: {} ({})", user.getName(), user.getEmail());
    }

    public User getCurrentUser() {
        return currentUserContext.get();
    }

    public void clearCurrentUser() {
        User user = currentUserContext.get();
        if (user != null) {
            logger.info("Clearing user context for: {}", user.getEmail());
        }
        currentUserContext.remove();
    }

    // ✅ getAllProducts avec traçage
    public List<Product> getAllProducts() {
        Span span = tracer.spanBuilder("ProductService.getAllProducts")
                .setAttribute("operation", "READ")
                .startSpan();

        try (Scope scope = span.makeCurrent()) {
            User currentUser = getCurrentUser();

            // Ajouter attributs utilisateur
            if (currentUser != null) {
                span.setAttribute("user.name", currentUser.getName());
                span.setAttribute("user.email", currentUser.getEmail());
            }

            logger.info(
                    "Operation: getAllProducts | User: {} | Email: {} | Action: READ",
                    currentUser != null ? currentUser.getName() : "Unknown",
                    currentUser != null ? currentUser.getEmail() : "unknown@email.com"
            );

            span.addEvent("fetching_products");
            List<Product> products = productRepository.findAll();

            // Ajouter le nombre de produits
            span.setAttribute("products.count", products.size());
            span.addEvent("products_retrieved");

            logger.info(
                    "Retrieved {} products | User: {} | Operation: READ | Status: SUCCESS",
                    products.size(),
                    currentUser != null ? currentUser.getEmail() : "unknown@email.com"
            );

            if (currentUser != null) {
                userProfileService.logOperation(
                        currentUser,
                        "getAllProducts",
                        UserOperationType.READ,
                        null,
                        null,
                        null
                );
            }

            span.setStatus(StatusCode.OK);
            return products;

        } catch (Exception e) {
            span.recordException(e);
            span.setStatus(StatusCode.ERROR, "Failed to get all products");
            throw e;
        } finally {
            span.end();
        }
    }

    // ✅ getProductById avec traçage
    public Product getProductById(String id) {
        Span span = tracer.spanBuilder("ProductService.getProductById")
                .setAttribute("operation", "READ")
                .setAttribute("product.id", id)
                .startSpan();

        try (Scope scope = span.makeCurrent()) {
            User currentUser = getCurrentUser();

            if (currentUser != null) {
                span.setAttribute("user.name", currentUser.getName());
                span.setAttribute("user.email", currentUser.getEmail());
            }

            logger.info(
                    "Operation: getProductById | User: {} | Email: {} | ProductID: {} | Action: READ",
                    currentUser != null ? currentUser.getName() : "Unknown",
                    currentUser != null ? currentUser.getEmail() : "unknown@email.com",
                    id
            );

            span.addEvent("fetching_product_by_id");
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> {
                        span.addEvent("product_not_found");
                        logger.error(
                                "Product not found | ID: {} | User: {} | Status: ERROR",
                                id,
                                currentUser != null ? currentUser.getEmail() : "unknown"
                        );
                        return new ProductNotFoundException(id);
                    });

            // Ajouter attributs du produit
            span.setAttribute("product.name", product.getName());
            span.setAttribute("product.price", product.getPrice());
            span.addEvent("product_found");

            logger.info(
                    "Product found | ID: {} | Name: {} | Price: €{} | User: {} | Operation: READ | Status: SUCCESS",
                    product.getId(),
                    product.getName(),
                    product.getPrice(),
                    currentUser != null ? currentUser.getEmail() : "unknown@email.com"
            );

            if (currentUser != null) {
                userProfileService.logOperation(
                        currentUser,
                        "getProductById",
                        UserOperationType.READ,
                        product.getId(),
                        product.getName(),
                        product.getPrice()
                );

                // Check si produit cher
                if (product.getPrice() >= 100.0) {
                    span.setAttribute("product.expensive", true);
                    span.addEvent("expensive_product_viewed");

                    logger.info(
                            "Expensive product view | ID: {} | Name: {} | Price: €{} | User: {} | Operation: SEARCH_EXPENSIVE",
                            product.getId(),
                            product.getName(),
                            product.getPrice(),
                            currentUser.getEmail()
                    );

                    userProfileService.logOperation(
                            currentUser,
                            "viewExpensiveProduct",
                            UserOperationType.SEARCH_EXPENSIVE,
                            product.getId(),
                            product.getName(),
                            product.getPrice()
                    );
                }
            }

            span.setStatus(StatusCode.OK);
            return product;

        } catch (Exception e) {
            span.recordException(e);
            span.setStatus(StatusCode.ERROR, "Failed to get product by ID");
            throw e;
        } finally {
            span.end();
        }
    }

    // ✅ addProduct avec traçage
    public Product addProduct(Product product) {
        Span span = tracer.spanBuilder("ProductService.addProduct")
                .setAttribute("operation", "WRITE")
                .setAttribute("product.name", product.getName())
                .setAttribute("product.price", product.getPrice())
                .startSpan();

        try (Scope scope = span.makeCurrent()) {
            User currentUser = getCurrentUser();

            if (currentUser != null) {
                span.setAttribute("user.name", currentUser.getName());
                span.setAttribute("user.email", currentUser.getEmail());
            }

            logger.info(
                    "Operation: addProduct | User: {} | Email: {} | ProductName: {} | Price: €{} | Action: WRITE",
                    currentUser != null ? currentUser.getName() : "Unknown",
                    currentUser != null ? currentUser.getEmail() : "unknown@email.com",
                    product.getName(),
                    product.getPrice()
            );

            // Vérifier si produit existe
            if (product.getId() != null && productRepository.existsById(product.getId())) {
                span.addEvent("product_already_exists");
                logger.error(
                        "Product already exists | ID: {} | User: {} | Status: ERROR",
                        product.getId(),
                        currentUser != null ? currentUser.getEmail() : "unknown"
                );
                throw new ProductAlreadyExistsException(product.getId());
            }

            span.addEvent("saving_product");
            Product saved = productRepository.save(product);

            span.setAttribute("product.id", saved.getId());
            span.addEvent("product_saved");

            logger.info(
                    "Product added | ID: {} | Name: {} | Price: €{} | User: {} | Operation: WRITE | Status: SUCCESS",
                    saved.getId(),
                    saved.getName(),
                    saved.getPrice(),
                    currentUser != null ? currentUser.getEmail() : "unknown@email.com"
            );

            if (currentUser != null) {
                userProfileService.logOperation(
                        currentUser,
                        "addProduct",
                        UserOperationType.WRITE,
                        saved.getId(),
                        saved.getName(),
                        saved.getPrice()
                );
            }

            span.setStatus(StatusCode.OK);
            return saved;

        } catch (Exception e) {
            span.recordException(e);
            span.setStatus(StatusCode.ERROR, "Failed to add product");
            throw e;
        } finally {
            span.end();
        }
    }

    // ✅ updateProduct avec traçage
    public Product updateProduct(String id, Product updatedProduct) {
        Span span = tracer.spanBuilder("ProductService.updateProduct")
                .setAttribute("operation", "WRITE")
                .setAttribute("product.id", id)
                .startSpan();

        try (Scope scope = span.makeCurrent()) {
            User currentUser = getCurrentUser();

            if (currentUser != null) {
                span.setAttribute("user.name", currentUser.getName());
                span.setAttribute("user.email", currentUser.getEmail());
            }

            logger.info(
                    "Operation: updateProduct | User: {} | Email: {} | ProductID: {} | Action: WRITE",
                    currentUser != null ? currentUser.getName() : "Unknown",
                    currentUser != null ? currentUser.getEmail() : "unknown@email.com",
                    id
            );

            span.addEvent("fetching_existing_product");
            Product existingProduct = productRepository.findById(id)
                    .orElseThrow(() -> {
                        span.addEvent("product_not_found");
                        logger.error(
                                "Product not found for update | ID: {} | User: {} | Status: ERROR",
                                id,
                                currentUser != null ? currentUser.getEmail() : "unknown"
                        );
                        return new ProductNotFoundException(id);
                    });

            // Enregistrer les anciennes valeurs
            span.setAttribute("product.old_name", existingProduct.getName());
            span.setAttribute("product.old_price", existingProduct.getPrice());

            existingProduct.setName(updatedProduct.getName());
            existingProduct.setPrice(updatedProduct.getPrice());
            existingProduct.setExpirationDate(updatedProduct.getExpirationDate());

            // Enregistrer les nouvelles valeurs
            span.setAttribute("product.new_name", updatedProduct.getName());
            span.setAttribute("product.new_price", updatedProduct.getPrice());

            span.addEvent("saving_updated_product");
            Product saved = productRepository.save(existingProduct);
            span.addEvent("product_updated");

            logger.info(
                    "Product updated | ID: {} | Name: {} | Price: €{} | User: {} | Operation: WRITE | Status: SUCCESS",
                    saved.getId(),
                    saved.getName(),
                    saved.getPrice(),
                    currentUser != null ? currentUser.getEmail() : "unknown@email.com"
            );

            if (currentUser != null) {
                userProfileService.logOperation(
                        currentUser,
                        "updateProduct",
                        UserOperationType.WRITE,
                        saved.getId(),
                        saved.getName(),
                        saved.getPrice()
                );
            }

            span.setStatus(StatusCode.OK);
            return saved;

        } catch (Exception e) {
            span.recordException(e);
            span.setStatus(StatusCode.ERROR, "Failed to update product");
            throw e;
        } finally {
            span.end();
        }
    }

    // ✅ deleteProduct avec traçage
    public void deleteProduct(String id) {
        Span span = tracer.spanBuilder("ProductService.deleteProduct")
                .setAttribute("operation", "WRITE")
                .setAttribute("product.id", id)
                .startSpan();

        try (Scope scope = span.makeCurrent()) {
            User currentUser = getCurrentUser();

            if (currentUser != null) {
                span.setAttribute("user.name", currentUser.getName());
                span.setAttribute("user.email", currentUser.getEmail());
            }

            logger.info(
                    "Operation: deleteProduct | User: {} | Email: {} | ProductID: {} | Action: WRITE",
                    currentUser != null ? currentUser.getName() : "Unknown",
                    currentUser != null ? currentUser.getEmail() : "unknown@email.com",
                    id
            );

            span.addEvent("fetching_product_to_delete");
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> {
                        span.addEvent("product_not_found");
                        logger.error(
                                "Product not found for deletion | ID: {} | User: {} | Status: ERROR",
                                id,
                                currentUser != null ? currentUser.getEmail() : "unknown"
                        );
                        return new ProductNotFoundException(id);
                    });

            span.setAttribute("product.name", product.getName());
            span.setAttribute("product.price", product.getPrice());

            span.addEvent("deleting_product");
            productRepository.deleteById(id);
            span.addEvent("product_deleted");

            logger.info(
                    "Product deleted | ID: {} | Name: {} | User: {} | Operation: WRITE | Status: SUCCESS",
                    id,
                    product.getName(),
                    currentUser != null ? currentUser.getEmail() : "unknown@email.com"
            );

            if (currentUser != null) {
                userProfileService.logOperation(
                        currentUser,
                        "deleteProduct",
                        UserOperationType.WRITE,
                        product.getId(),
                        product.getName(),
                        product.getPrice()
                );
            }

            span.setStatus(StatusCode.OK);

        } catch (Exception e) {
            span.recordException(e);
            span.setStatus(StatusCode.ERROR, "Failed to delete product");
            throw e;
        } finally {
            span.end();
        }
    }
}