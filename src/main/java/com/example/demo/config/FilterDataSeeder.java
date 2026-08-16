package com.example.demo.config;

import com.example.demo.entity.*;
import com.example.demo.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Component
public class FilterDataSeeder implements CommandLineRunner {

    private final SkinTypeRepository skinTypeRepository;
    private final SkinConcernRepository skinConcernRepository;
    private final IngredientRepository ingredientRepository;
    private final ProductRepository productRepository;

    public FilterDataSeeder(SkinTypeRepository skinTypeRepository,
                            SkinConcernRepository skinConcernRepository,
                            IngredientRepository ingredientRepository,
                            ProductRepository productRepository) {
        this.skinTypeRepository = skinTypeRepository;
        this.skinConcernRepository = skinConcernRepository;
        this.ingredientRepository = ingredientRepository;
        this.productRepository = productRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (skinTypeRepository.count() > 0 && skinConcernRepository.count() > 0 && ingredientRepository.count() > 0) {
            return; // Already seeded
        }

        // 1. Create Skin Types
        Map<String, SkinType> skinTypes = new HashMap<>();
        String[] stNames = {"Oily", "Dry", "Combination", "Sensitive", "Normal"};
        for (String name : stNames) {
            SkinType st = skinTypeRepository.findByName(name).orElseGet(() -> {
                SkinType newSt = new SkinType();
                newSt.setName(name);
                return skinTypeRepository.save(newSt);
            });
            skinTypes.put(name, st);
        }

        // 2. Create Skin Concerns
        Map<String, SkinConcern> concerns = new HashMap<>();
        String[] scNames = {
            "Dryness / Dehydration", "Dullness / Uneven Tone", "Sensitivity / Redness",
            "Blemishes / Acne-Prone", "Anti-Aging / Fine Lines", "Universal / Everyday Care"
        };
        for (String name : scNames) {
            SkinConcern sc = skinConcernRepository.findByName(name).orElseGet(() -> {
                SkinConcern newSc = new SkinConcern();
                newSc.setName(name);
                return skinConcernRepository.save(newSc);
            });
            concerns.put(name, sc);
        }

        // 3. Create Ingredients
        Map<String, Ingredient> ingredients = new HashMap<>();
        String[] ingNames = {
            "Hyaluronic Acid", "Vitamin C", "Niacinamide", "Salicylic Acid", "Retinol", "Peptides"
        };
        for (String name : ingNames) {
            Ingredient ing = ingredientRepository.findByName(name);
            if (ing == null) {
                ing = new Ingredient();
                ing.setName(name);
                ing = ingredientRepository.save(ing);
            }
            ingredients.put(name, ing);
        }

        // 4. Map Products
        mapSkinType(skinTypes.get("Oily"), Arrays.asList(2, 3, 11, 12, 13, 15, 25, 26, 43, 101, 127, 134, 136, 141, 154, 155, 161, 164, 176, 177, 179, 180, 186, 192));
        mapSkinType(skinTypes.get("Dry"), Arrays.asList(4, 10, 23, 30, 32, 42, 65, 44, 50, 68, 69, 88, 93, 102, 109, 110, 113, 115, 126, 130, 131, 137, 138, 139, 149, 178, 181, 182, 185, 189, 196));
        mapSkinType(skinTypes.get("Combination"), Arrays.asList(8, 14, 17, 18, 27, 29, 31, 33, 37, 55, 56, 112, 119, 128, 133, 145, 153, 165, 187, 188, 193));
        mapSkinType(skinTypes.get("Sensitive"), Arrays.asList(1, 5, 7, 28, 35, 47, 49, 62, 79, 86, 97, 129, 135, 157, 158, 160, 169, 198, 200));
        mapSkinType(skinTypes.get("Normal"), Arrays.asList(6, 9, 16, 19, 20, 21, 22, 24, 34, 36, 38, 39, 40, 41, 45, 46, 48, 51, 52, 53, 54, 57, 58, 59, 60, 61, 63, 64, 66, 67, 70, 71, 72, 73, 74, 75, 76, 77, 78, 80, 81, 82, 83, 84, 85, 87, 89, 90, 91, 92, 94, 95, 96, 98, 99, 100, 103, 104, 105, 106, 107, 108, 111, 114, 116, 117, 118, 120, 121, 122, 123, 124, 125, 132, 140, 142, 143, 144, 146, 147, 148, 150, 151, 152, 156, 159, 162, 163, 166, 167, 168, 170, 171, 172, 173, 174, 175, 183, 184, 190, 191, 194, 195, 197, 199));

        mapConcern(concerns.get("Dryness / Dehydration"), Arrays.asList(2, 4, 10, 31, 32, 42, 65, 44, 46, 50, 68, 69, 88, 93, 97, 102, 109, 110, 113, 115, 119, 126, 127, 128, 133, 134, 137, 139, 141, 144, 148, 149, 178, 181, 182, 196));
        mapConcern(concerns.get("Dullness / Uneven Tone"), Arrays.asList(13, 21, 27, 29, 48, 51, 58, 63, 103, 105, 106, 107, 111, 116, 118, 122, 194, 195));
        mapConcern(concerns.get("Sensitivity / Redness"), Arrays.asList(1, 5, 7, 23, 28, 30, 35, 37, 47, 49, 62, 79, 86, 129, 135, 158, 160, 189, 200));
        mapConcern(concerns.get("Blemishes / Acne-Prone"), Arrays.asList(3, 25, 26, 33, 43, 101, 136, 153, 176, 177, 179, 180, 186, 192));
        mapConcern(concerns.get("Anti-Aging / Fine Lines"), Arrays.asList(41, 45, 52, 54, 55, 56, 57, 59, 60, 61, 64, 66, 67, 70, 71, 72, 73, 74, 75, 76, 77, 78, 80, 81, 82, 83, 84, 85, 87, 89, 90, 91, 92, 94, 95, 96, 98, 99, 100, 104, 108, 114, 117, 120, 121, 123, 124, 125, 132, 140, 142, 143, 146, 147, 150, 184, 190, 191, 193));
        mapConcern(concerns.get("Universal / Everyday Care"), Arrays.asList(6, 8, 9, 11, 12, 14, 15, 16, 17, 18, 19, 20, 22, 24, 34, 36, 38, 39, 40, 53, 112, 130, 131, 138, 145, 151, 152, 154, 155, 156, 157, 159, 161, 162, 163, 164, 165, 166, 167, 168, 169, 170, 171, 172, 173, 174, 175, 185, 187, 188, 197, 198, 199));

        mapIngredient(ingredients.get("Hyaluronic Acid"), Arrays.asList(2, 32, 42, 65, 46, 50, 69, 93, 102, 109, 110, 113, 115, 120, 127, 137, 144, 148, 181, 182, 196));
        mapIngredient(ingredients.get("Vitamin C"), Arrays.asList(21, 105, 106, 107, 111, 116, 194));
        mapIngredient(ingredients.get("Niacinamide"), Arrays.asList(101, 153));
        mapIngredient(ingredients.get("Salicylic Acid"), Arrays.asList(27, 29, 33, 37, 180, 195));
        mapIngredient(ingredients.get("Retinol"), Arrays.asList(104, 190));
        mapIngredient(ingredients.get("Peptides"), Arrays.asList(114, 132, 140));
    }

    private void mapSkinType(SkinType st, List<Integer> productIds) {
        if (st == null) return;
        List<Product> products = productRepository.findAllById(productIds);
        for (Product p : products) {
            if (p.getSkinTypes() == null) p.setSkinTypes(new ArrayList<>());
            if (!p.getSkinTypes().contains(st)) {
                p.getSkinTypes().add(st);
            }
        }
        productRepository.saveAll(products);
    }

    private void mapConcern(SkinConcern sc, List<Integer> productIds) {
        if (sc == null) return;
        List<Product> products = productRepository.findAllById(productIds);
        for (Product p : products) {
            if (p.getConcerns() == null) p.setConcerns(new ArrayList<>());
            if (!p.getConcerns().contains(sc)) {
                p.getConcerns().add(sc);
            }
        }
        productRepository.saveAll(products);
    }

    private void mapIngredient(Ingredient ing, List<Integer> productIds) {
        if (ing == null) return;
        List<Product> products = productRepository.findAllById(productIds);
        for (Product p : products) {
            if (p.getIngredients() == null) p.setIngredients(new ArrayList<>());
            if (!p.getIngredients().contains(ing)) {
                p.getIngredients().add(ing);
            }
        }
        productRepository.saveAll(products);
    }
}
