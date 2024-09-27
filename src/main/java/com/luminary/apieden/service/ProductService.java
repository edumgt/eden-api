package com.luminary.apieden.service;

import com.luminary.apieden.mapper.ProductMapper;
import com.luminary.apieden.model.database.ConditionTypes;
import com.luminary.apieden.model.database.Product;
import com.luminary.apieden.model.database.UsageTime;
import com.luminary.apieden.model.database.User;
import com.luminary.apieden.model.exception.HttpError;
import com.luminary.apieden.model.request.ProductRequest;
import com.luminary.apieden.repository.ConditionTypeRepository;
import com.luminary.apieden.repository.ProductRepository;
import com.luminary.apieden.repository.UsageTimeRepository;
import com.luminary.apieden.repository.UserRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {
    private final ProductRepository productRepository;
    private final ConditionTypeRepository conditionTypeRepository;
    private final UsageTimeRepository usageTimeRepository;
    private final UserRepository userRepository;
    private final ProductMapper productMapper;

    public List<Product> findAll() {
        return productRepository.findAll();
    }

    public List<Product> findProductByTitleLike(String title) throws HttpError {
        return productRepository.findByTitleLike(title);

    }

    public Product register(ProductRequest productRequest) throws HttpError {
        ConditionTypes conditionTypes = conditionTypeRepository.findById(productRequest.getConditionTypeId())
                .orElseThrow(() -> new HttpError(HttpStatus.BAD_REQUEST, "Condition type not found"));
        UsageTime usageTime = usageTimeRepository.findById(productRequest.getUsageTimeId())
                .orElseThrow(() -> new HttpError(HttpStatus.BAD_REQUEST, "Usage time not found"));
        User user = userRepository.findByEmail(productRequest.getEmail())
                .orElseThrow(() -> new HttpError(HttpStatus.BAD_REQUEST, "User not found"));
        Product product = productMapper.toProduct(productRequest);
        product.setConditionType(conditionTypes);
        product.setUsageTime(usageTime);
        product.setUser(user);
        productRepository.save(product);
        return product;
    }

    public void partialUpdate(String id, Map<String, Object> request) throws HttpError {
        Product product = productRepository.findById(Long.valueOf(id))
                .orElseThrow(() -> new HttpError(HttpStatus.BAD_REQUEST, "Product not found"));
        boolean verifyVariable = false;
        if (request.containsKey("usageTime")) {
            Long usageTimeId = (Long) request.get("usageTime");
            log.info("[PRODUCT] usageTime {} being updated to {}", product.getUsageTime().getId(), usageTimeId);
            UsageTime usageTime = usageTimeRepository.findById(usageTimeId)
                    .orElseThrow(() -> new HttpError(HttpStatus.BAD_REQUEST, "Usage time not found"));
            product.setUsageTime(usageTime);
            verifyVariable = true;
            log.info("[PRODUCT] usageTime updated");
        } else if (request.containsKey("conditionType")) {
            Long conditionTypeId = (Long) request.get("conditionType");
            log.info("[PRODUCT] conditionType {} being updated to {}", product.getConditionType().getId(), conditionTypeId);
            ConditionTypes conditionTypes = conditionTypeRepository.findById(conditionTypeId)
                    .orElseThrow(() -> new HttpError(HttpStatus.BAD_REQUEST, "Condition types not found"));
            product.setConditionType(conditionTypes);
            verifyVariable = true;
            log.info("[PRODUCT] conditionType updated");
        } else if (request.containsKey("user")) {
            Long userId = (Long) request.get("user");
            log.info("[PRODUCT] userId {} being updated to {}", product.getUser().getId(), userId);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new HttpError(HttpStatus.BAD_REQUEST, "User not found"));
            product.setUser(user);
            verifyVariable = true;
            log.info("[PRODUCT] user updated");
        } else if (request.containsKey("title")) {
            log.info("[PRODUCT] Title {} being updated to {}", product.getTitle(), (String) request.get("title"));
            product.setTitle((String) request.get("title"));
            verifyVariable = true;
            log.info("[PRODUCT] Title updated");
        } else if (request.containsKey("description")) {
            log.info("[PRODUCT] Description {} being updated to {}", product.getDescription(), (String) request.get("description"));
            product.setDescription((String) request.get("description"));
            verifyVariable = true;
            log.info("[PRODUCT] Description updated");
        } else if (request.containsKey("price")) {
            log.info("[PRODUCT] Price being updated.");
            product.setPrice((Float) request.get("price"));
            verifyVariable = true;
            log.info("[PRODUCT] price updated");
        } else if (request.containsKey("maxPrice")) {
            log.info("[PRODUCT] Max price {} being updated to {}", product.getMaxPrice(), (Float) request.get("maxPrice"));
            product.setMaxPrice((Float) request.get("maxPrice"));
            verifyVariable = true;
            log.info("[PRODUCT] Max price updated");
        } else if (request.containsKey("senderZipCode")) {
            log.info("[PRODUCT] Sender zip code {} being updated to {}", product.getSenderZipCode(), (String) request.get("senderZipCode"));
            product.setSenderZipCode((String) request.get("senderZipCode"));
            verifyVariable = true;
            log.info("[PRODUCT] Sender zip code updated");
        } else if (request.containsKey("rating")) {
            log.info("[PRODUCT] Rating {} being updated to {}", product.getSenderZipCode(), (String) request.get("senderZipCode"));
            product.setRating((Float) request.get("rating"));
            verifyVariable = true;
            log.info("[PRODUCT] Rating updated");
        }
        if (!verifyVariable) {
            log.warn("[PRODUCT] None valid field passed.");
            throw new HttpError(HttpStatus.BAD_REQUEST, "None valid field has been passed.");
        }

        log.info("[PRODUCT] Starting attributes validation.");
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

        Set<ConstraintViolation<Product>> violations = validator.validate(product);
        if (!violations.isEmpty()) {
            StringBuilder errorMessage = new StringBuilder("Erros de validação:");
            for (ConstraintViolation<Product> violation : violations) {
                errorMessage.append(" /  ").append(violation.getMessage());
            }
            throw new HttpError(HttpStatus.BAD_REQUEST, errorMessage.toString());
        }

        log.info("[PRODUCT] Attributes validated");
        log.info("[PRODUCT] Saving product in database.");
        productRepository.save(product);
    }

    public void deleteById(String id) throws HttpError {
        productRepository.findById(Long.valueOf(id))
                .orElseThrow(() -> new HttpError(HttpStatus.BAD_REQUEST, "Product not found"));
        log.info("[PRODUCT] Deleting product");
        productRepository.deleteById(Long.valueOf(id));
        log.info("[PRODUCT] Product deleted");
    }
}
