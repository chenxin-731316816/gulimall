package com.iflytek.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.iflytek.gulimall.common.feign.vo.CategoryVO;
import com.iflytek.gulimall.product.vo.Catalog2Vo;
import com.iflytek.gulimall.product.vo.Catalog3Vo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.iflytek.gulimall.common.utils.PageUtils;
import com.iflytek.gulimall.common.utils.Query;

import com.iflytek.gulimall.product.dao.CategoryDao;
import com.iflytek.gulimall.product.entity.CategoryEntity;
import com.iflytek.gulimall.product.service.CategoryService;
import org.springframework.util.StringUtils;


@Service("categoryService")
@Slf4j(topic ="CategoryServiceImpl" )
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {


    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final String REDIS_FOLDER = "category:";


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<>()
        );

        return new PageUtils(page);
    }

    @Override
    @Cacheable(cacheNames = "category", key = "#root.methodName", sync = true)
    public List<CategoryEntity> listWithTree() {
        List<CategoryEntity> all = baseMapper.selectList(null);
        return all.stream().filter(
                //查询一级分类
                categoryEntity -> categoryEntity.getParentCid() == 0
        ).peek(categoryEntity -> {
                    //給一級菜單設置子菜單
                    categoryEntity.setChildren(getChildrens(categoryEntity, all));
                }
        ).sorted(Comparator.comparingInt(item -> (item.getSort() == null ? 0 : item.getSort()))
        ).collect(Collectors.toList());
    }

    /**
     * 清空缓存 allEntries:true category分区的所缓存,当对数据库增删改时出发
     * @param list
     */
    @Override
    @CacheEvict(cacheNames = "category",allEntries = true)
    public void removeMenuByIds(List<Long> list) {
        //TODO 1检查当前菜单是否被其他地方引用
        baseMapper.deleteBatchIds(list);
    }

    /**
     * 将null值缓存到redis 配置默认是true 防止缓存穿透 spring.cache.redis.cache-null-values=true
     * sync 设置为true出现缓存击穿
     * @return
     */
    @Override
    @Cacheable(cacheNames = "category", key = "#root.methodName",sync = true)
    public List<CategoryEntity>     findFirstCategory() {
        return baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
    }

    @Override
    @Cacheable(cacheNames = "category", key = "#root.methodName", sync = true)
    public Map<String, List<Catalog2Vo>> getCatalog() {
        return getCatalogFromDb();
    }

    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> paths = new ArrayList<>();

        //递归查询是否还有父节点
        List<Long> parentPath = findParentPath(catelogId, paths);

        //进行一个逆序排列
        Collections.reverse(parentPath);

        return parentPath.toArray(new Long[0]);
    }

    @Override
    @CacheEvict(cacheNames = "category",allEntries = true)
    public void updateByIds(List<CategoryEntity> asList) {
        this.updateBatchById(asList);
    }

    @Override
    @CacheEvict(cacheNames = "category",allEntries = true)
    public void saveAndRefreshCache(CategoryEntity category) {
        this.save(category);
    }

    @Override
    @CacheEvict(cacheNames = "category",allEntries = true)
    public void updateByIdAndRefreshCache(CategoryEntity category) {
        this.updateById(category);
    }

    @Override
    @Cacheable(cacheNames = "category", key = "#root.methodName",sync = true)
    public CategoryVO getByCategory3Id(Long catId) {
        CategoryEntity byId = this.getById(catId);
        CategoryVO categoryVO=new CategoryVO();
        categoryVO.setCatId(catId);
        categoryVO.setName(byId.getName());
        return categoryVO;
    }


    public Map<String, List<Catalog2Vo>> getCatalog1() {
        ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
        String catagory = operations.get(REDIS_FOLDER + "catagory");
        if (StringUtils.isEmpty(catagory)) {
            Map<String, List<Catalog2Vo>> catalogFromDb = getCatalogFromDb();
            //缓存存放json字符串,方便序列化
            String catalogFromDbJSON = JSON.toJSONString(catalogFromDb);
            operations.set(REDIS_FOLDER + "catagory", catalogFromDbJSON);
            return catalogFromDb;
        } else {
            //复杂类型转化用TypeReference 由于是protect需要变成{}
            return JSON.parseObject(catagory, new TypeReference<Map<String, List<Catalog2Vo>>>() {
            });
        }

    }


    /**
     * 查询分类,从数据库查询
     *
     * @return
     */
    public Map<String, List<Catalog2Vo>> getCatalogFromDb() {

        List<CategoryEntity> categoryEntities = this.baseMapper.selectList(null);
        List<CategoryEntity> category1 = getCategoryEntities(0L, categoryEntities);//查询都所有的一级分类
        Map<String, List<Catalog2Vo>> collect = category1.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            Long catId1 = v.getCatId();
            //二级分类
            List<CategoryEntity> category2 = getCategoryEntities(catId1, categoryEntities);
            return category2.stream().map(categoryEntity2 -> {
                String catalog1Id = catId1.toString();//一级分类id
                Long catId2 = categoryEntity2.getCatId();//二级分类id
                String name = categoryEntity2.getName();//二级分类名称
                //三级分类
                List<CategoryEntity> category3 = getCategoryEntities(catId2, categoryEntities);
                List<Catalog3Vo> catalog3VoList = category3.stream().map(categoryEntity3 -> {//三级分类
                    //构建三级vo
                    Catalog3Vo catalog3Vo = new Catalog3Vo();
                    String categoryEntity3Name = categoryEntity3.getName();//三级分类名称
                    String catId = categoryEntity3.getCatId().toString();//三级分类名称
                    String catalog2Id = catId2.toString();//二级分类id
                    catalog3Vo.setId(catId);
                    catalog3Vo.setName(categoryEntity3Name);
                    catalog3Vo.setCatalog2Id(catalog2Id);
                    return catalog3Vo;
                }).collect(Collectors.toList());
                //构建二级vo
                Catalog2Vo catalog2Vo = new Catalog2Vo();
                catalog2Vo.setCatalog1Id(catalog1Id);
                catalog2Vo.setId(catId2.toString());
                catalog2Vo.setName(name);
                catalog2Vo.setCatalog3List(catalog3VoList);
                return catalog2Vo;
            }).collect(Collectors.toList());
        }));
        return collect;
    }

    private List<CategoryEntity> getCategoryEntities(Long catId1, List<CategoryEntity> categoryEntities) {

        return categoryEntities.stream().filter(item -> {
            return Objects.equals(item.getParentCid(), catId1);  //根据parentCid
        }).collect(Collectors.toList());
    }

    /**
     * 查詢一個分類的所有子分類
     *
     * @param categoryEntity
     * @param all
     * @return
     */
    private List<CategoryEntity> getChildrens(CategoryEntity categoryEntity, List<CategoryEntity> all) {
        return all.stream().filter(item -> item.getParentCid() == categoryEntity.getCatId()).
                map(item -> {
                    //設置子菜單
                    item.setChildren(getChildrens(item, all));
                    return item;
                }).sorted(Comparator.comparingInt(item -> (item.getSort() == null ? 0 : item.getSort()))
        ).collect(Collectors.toList());
    }
    private List<Long> findParentPath(Long catelogId, List<Long> paths) {
        //1、收集当前节点id
        paths.add(catelogId);
        //根据当前分类id查询信息
        CategoryEntity byId = this.getById(catelogId);
        //如果当前不是父分类
        if (byId.getParentCid() != 0) {
            findParentPath(byId.getParentCid(), paths);
        }
        return paths;
    }

}
