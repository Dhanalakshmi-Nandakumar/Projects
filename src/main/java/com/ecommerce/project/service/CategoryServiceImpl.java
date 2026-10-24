package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.Category;
import com.ecommerce.project.payload.CategoryDTO;
import com.ecommerce.project.payload.CategoryResponse;
import com.ecommerce.project.repositories.CategoryRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService{


    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public CategoryResponse getAllCategories(Integer pageNumber,Integer pageSize,String sortBy,String sortOrder) {
        Sort sortByAndDir=sortOrder.equalsIgnoreCase("asc")
                ?Sort.by(sortBy).ascending()
                :Sort.by(sortBy).descending();
        Pageable pageDetails= PageRequest.of(pageNumber,pageSize,sortByAndDir);
        Page<Category> categoryPage=categoryRepository.findAll(pageDetails);

        List<Category> categories=categoryPage.getContent();
        if(categories.isEmpty())
        {
            throw new APIException("No categories created till now ");
        }
        List<CategoryDTO> categoryDTO=categories.stream()
                .map(category -> modelMapper.map(category,CategoryDTO.class))
                .toList();
        CategoryResponse categoryResponse=new CategoryResponse();
        categoryResponse.setContent(categoryDTO);
        categoryResponse.setPageNumber(categoryPage.getNumber());
        categoryResponse.setPageSize(categoryPage.getSize());
        categoryResponse.setTotalElements(categoryPage.getTotalElements());
        categoryResponse.setTotalPages(categoryPage.getTotalPages());
        categoryResponse.setLastPage(categoryPage.isLast());
        return categoryResponse;
    }



    @Override
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {

        // DTO → Entity
        Category category = modelMapper.map(categoryDTO, Category.class);

        // Check whether category already exists
        Category categoryFromDb =
                categoryRepository.findByCategoryName(categoryDTO.getCategoryName());

        if (categoryFromDb != null) {
            throw new APIException(
                    "Category with the name "
                            + categoryDTO.getCategoryName()
                            + " Already exist !!"
            );
        }

        // Save the NEW category
        Category savedCategory = categoryRepository.save(category);

        // Entity → DTO
        CategoryDTO savedCategoryDTO =
                modelMapper.map(savedCategory, CategoryDTO.class);

        return savedCategoryDTO;
    }

    @Override
    public CategoryDTO deleteCategory(Long categoryId) {

       Category category=categoryRepository.findById(categoryId)
               .orElseThrow(()->new ResourceNotFoundException("Category","CategoryID",categoryId));

       categoryRepository.delete(category);
       CategoryDTO categoryDTO=modelMapper.map(category,CategoryDTO.class);
        return categoryDTO;
    }

    @Override
    public CategoryDTO updateCategory(CategoryDTO categoryDTO, Long categoryId) {
        //check wthether the category id is exist before updating
        Category savedCategory=categoryRepository.findById(categoryId).
                orElseThrow(()->new ResourceNotFoundException("Category","CategryId",categoryId));


        Category category=modelMapper.map(categoryDTO,Category.class);
        //Assign URL ID (body ID is null)
        category.setCategoryId(categoryId);

        savedCategory=categoryRepository.save(category);
        CategoryDTO savedUpdatedCategoryDTO=modelMapper.map(savedCategory,CategoryDTO.class);

        return savedUpdatedCategoryDTO;
    }
}
