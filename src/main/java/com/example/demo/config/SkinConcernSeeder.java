package com.example.demo.config;

import com.example.demo.entity.Product;
import com.example.demo.entity.SkinConcern;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.SkinConcernRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
public class SkinConcernSeeder implements CommandLineRunner {

    private final SkinConcernRepository concernRepository;
    private final ProductRepository productRepository;

    public SkinConcernSeeder(SkinConcernRepository concernRepository, ProductRepository productRepository) {
        this.concernRepository = concernRepository;
        this.productRepository = productRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (concernRepository.count() == 0) {
            // Map of Concerns to their Product IDs
            Map<String, List<Integer>> concernProductMapping = Map.of(
                    "Dryness / Dehydration", Arrays.asList(2, 40, 129, 130, 133, 134, 137, 138, 142, 144, 146, 148, 149, 178, 181, 196),
                    "Dullness / Uneven Tone", Arrays.asList(13, 21, 27, 29, 38, 52, 63, 103, 105, 106, 111, 118, 122, 194),
                    "Sensitivity / Redness", Arrays.asList(1, 4, 10, 23, 28, 30, 35, 135, 139, 157, 158, 189, 200),
                    "Blemishes / Acne-Prone", Arrays.asList(3, 11, 12, 25, 84, 101, 115, 136, 180, 186, 192),
                    "Anti-Aging / Fine Lines", Arrays.asList(15, 64, 66, 71, 72, 76, 77, 80, 83, 87, 91, 92, 100, 104, 108, 114, 120, 132, 140, 143, 190),
                    "Universal / Everyday Care", Arrays.asList(5, 6, 18, 22, 51, 102, 119, 155, 161, 183, 187, 188, 198, 199)
            );

            for (Map.Entry<String, List<Integer>> entry : concernProductMapping.entrySet()) {
                String concernName = entry.getKey();
                SkinConcern concern = new SkinConcern();
                concern.setName(concernName);
                concern = concernRepository.save(concern);

                for (Integer productId : entry.getValue()) {
                    Product product = productRepository.findById(productId).orElse(null);
                    if (product != null) {
                        product.getConcerns().add(concern);
                        productRepository.save(product);
                    }
                }
            }
            System.out.println("Skin concerns seeded successfully!");
        }
    }
}
