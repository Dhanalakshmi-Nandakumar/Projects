package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.Category;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.model.User;
import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.payload.ProductResponse;
import com.ecommerce.project.repositories.CategoryRepository;
import com.ecommerce.project.repositories.ProductRepository;
import com.ecommerce.project.util.AuthUtil;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService{

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private FileService fileService;

    @Value("${project.image}")
    private String path;

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public ProductDTO addProduct(Long categoryId, ProductDTO productDTO) {

        // 1. Find the category
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Category",
                                "categoryId",
                                categoryId
                        ));

        // 2. Check whether same product already exists in this category
        boolean isProductNotPresent = true;
        List<Product> products = category.getProducts();

        for(Product currentProduct:products){
            // Compare existing product name with requested product name
            if (currentProduct.getProductName()
                    .equals(productDTO.getProductName())) {

                isProductNotPresent = false;
                break;
            }
        }

        // 3. Add product only if it doesn't already exist
        if (isProductNotPresent) {

            // Convert DTO → Entity
            Product product = modelMapper.map(productDTO, Product.class);

            // Set default image
            product.setImage("default.png");

            // Connect Product → Category
            product.setCategory(category);

            // Calculate discounted/special price
            double specialPrice = product.getPrice()
                    - ((product.getDiscount() * 0.01) * product.getPrice());

            product.setSpecialPrice(specialPrice);

            //set authorized user who is adding the product
            User user = authUtil.loggedInUser();

            product.setUser(user);

            // Save product to database
            Product savedProduct = productRepository.save(product);

            // Convert Entity → DTO
            ProductDTO addedProductDTO =
                    modelMapper.map(savedProduct, ProductDTO.class);

            // Return DTO
            return addedProductDTO;

        } else {

            // Same product already exists in this category
            throw new APIException("Product Already Exist !!");
        }
    }

    @Override
    public ProductResponse getAllProducts(Integer pageNumber, Integer pageSize,
                                          String sortBy, String sortOrder) {

        // Create sorting: ascending or descending
        Sort sortByAndDir = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        // Create pagination details
        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndDir);

        // Fetch products with pagination and sorting
        //pageProducts=List<product> + page details
        Page<Product> pageProducts = productRepository.findAll(pageDetails);

        // Get products from the current page
        List<Product> products = pageProducts.getContent();

        // If no products found
//        if (products.isEmpty()) {
//            throw new APIException("No Product Exist !!");
//        }

        // Convert Product entities to ProductDTOs
        List<ProductDTO> productDTOS = products.stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .toList();

        // Create response object
        ProductResponse productResponse = new ProductResponse();

        // Add product DTOs to response
        productResponse.setContent(productDTOS);

        // Add pagination information to response
        productResponse.setPageNumber(pageProducts.getNumber());
        productResponse.setPageSize(pageProducts.getSize());
        productResponse.setTotalElements(pageProducts.getTotalElements());
        productResponse.setTotalPages(pageProducts.getTotalPages());
        productResponse.setLastPage(pageProducts.isLast());

        // Return final response
        return productResponse;
    }

    @Override
    public ProductResponse searchProductsByCategory(Long categoryId, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {

        Category category=categoryRepository.findById(categoryId)
                .orElseThrow(()->new ResourceNotFoundException("category","categoryId",categoryId));

        Sort sortByandDir=sortOrder.equalsIgnoreCase("asc")
                           ?Sort.by(sortBy).ascending()
                           :Sort.by(sortBy).descending();
        Pageable pageDetails=PageRequest.of(pageNumber,pageSize,sortByandDir);
        Page<Product> pageProducts=productRepository.findByCategoryOrderByPriceAsc(category,pageDetails);
        List<Product> products=pageProducts.getContent();

        if(products.isEmpty())
        {
            throw new APIException(category.getCategoryName()+ "Category does not have Any product");
        }

        // 2. Convert every Product into ProductDTO
        // 3. Collect all DTOs into a List
        List<ProductDTO> productDTOS = products.stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .toList();

        // 4. Create response object
        ProductResponse productResponse = new ProductResponse();

        // 5. Put DTO list inside response
        productResponse.setContent(productDTOS);
        // Add pagination information to response
        productResponse.setPageNumber(pageProducts.getNumber());
        productResponse.setPageSize(pageProducts.getSize());
        productResponse.setTotalElements(pageProducts.getTotalElements());
        productResponse.setTotalPages(pageProducts.getTotalPages());
        productResponse.setLastPage(pageProducts.isLast());

        // 6. Return response
        return productResponse;
    }

    @Override
    public ProductResponse searchProductsByKeyword(String keyword, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {

        Sort sortByandDir=sortOrder.equalsIgnoreCase("asc")
                ?Sort.by(sortBy).ascending()
                :Sort.by(sortBy).descending();
        Pageable pageDetails=PageRequest.of(pageNumber,pageSize,sortByandDir);
        Page<Product> pageProducts=productRepository.findByProductNameLikeIgnoreCase('%' + keyword + '%',pageDetails);
        List<Product> products=pageProducts.getContent();

        if(products.isEmpty())
        {
            throw new APIException("Product Not Found with Keyword "+keyword);
        }


        // 2. Convert every Product into ProductDTO
        // 3. Collect all DTOs into a List
        List<ProductDTO> productDTOS = products.stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .toList();

        // 4. Create response object
        ProductResponse productResponse = new ProductResponse();

        // 5. Put DTO list inside response
        productResponse.setContent(productDTOS);

        // Add pagination information to response
        productResponse.setPageNumber(pageProducts.getNumber());
        productResponse.setPageSize(pageProducts.getSize());
        productResponse.setTotalElements(pageProducts.getTotalElements());
        productResponse.setTotalPages(pageProducts.getTotalPages());
        productResponse.setLastPage(pageProducts.isLast());

        // 6. Return response
        return productResponse;
    }

    @Override
    public ProductDTO updateExistingProductById(Long productId, ProductDTO productDTO) {

        // 1. Find the existing product from the database using the productId
        //    received from the URL.
        //    If the product doesn't exist, throw ResourceNotFoundException.
        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "product",
                                "productId",
                                productId
                        ));
        // ProductDTO -> product
        Product product=modelMapper.map(productDTO,Product.class);


        // 2. Update the existing product's name with the value
        //    received from the request body.
        existingProduct.setProductName(product.getProductName());

        // 3. Update the existing product's description.
        existingProduct.setDescription(product.getDescription());

        // 4. Update the existing product's price.
        existingProduct.setPrice(product.getPrice());

        // 5. Update the existing product's quantity.
        existingProduct.setQuantity(product.getQuantity());

        // 6. Update the existing product's discount.
        existingProduct.setDiscount(product.getDiscount());

        existingProduct.setSpecialPrice(product.getSpecialPrice());


        // 7. Save the modified existing product into the database.
        //    Since this product already has an existing ID,
        //    JPA updates the existing database record.
        Product updatedProduct = productRepository.save(existingProduct);

        // 8. Convert the updated Product entity into ProductDTO
        //    so that we return DTO data to the client instead of the entity.
        ProductDTO updatedProductDTO =
                modelMapper.map(updatedProduct, ProductDTO.class);

        // 9. Return the updated product details to the controller/client.
        return updatedProductDTO;
    }
    @Override
    public ProductDTO deleteProductById(Long productId){
        Product product=productRepository.findById(productId)
                .orElseThrow(()->new ResourceNotFoundException("product","productId",productId));
        productRepository.delete(product);
        ProductDTO productDTO=modelMapper.map(product, ProductDTO.class);
        return productDTO;
    }

    @Override
    public ProductDTO updatedProductImage(Long productId, MultipartFile image) throws IOException {

        // 1. Find the existing product from the database using productId.
        //    If the product does not exist, throw ResourceNotFoundException.
        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("product", "productId", productId)
                );



        // 3. Upload the image to the images/ folder.
        //    uploadImage() generates a unique filename and saves the actual image.
        //    It returns only the generated filename.
        String fileName = fileService.uploadImage(path, image);

        // 4. Update the Product entity with the generated image filename.
        //    The actual image is stored in the file system;
        //    only its filename is stored in the database.
        existingProduct.setImage(fileName);

        // 5. Save the updated Product entity into the database.
        Product updatedProductImage = productRepository.save(existingProduct);

        // 6. Convert the updated Product entity into ProductDTO.
        //    DTO is returned to the client instead of exposing the entity directly.
        ProductDTO updatedImageProductDTO =
                modelMapper.map(updatedProductImage, ProductDTO.class);

        // 7. Return the updated product information to the client.
        return updatedImageProductDTO;
    }




}
