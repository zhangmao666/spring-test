package com.example.springboottest.modules.dict.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboottest.modules.dict.dto.*;
import com.example.springboottest.modules.dict.entity.Dict;
import com.example.springboottest.modules.dict.entity.DictItem;
import com.example.springboottest.modules.dict.repository.DictRepository;
import com.example.springboottest.modules.dict.repository.DictItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class DictService {

    private final DictRepository dictRepository;
    private final DictItemRepository dictItemRepository;

    public Long createDict(DictRequest request) {
        if (dictRepository.existsByDictCode(request.getDictCode())) {
            throw new RuntimeException("字典编码已存在: " + request.getDictCode());
        }
        Dict dict = new Dict();
        BeanUtils.copyProperties(request, dict);
        if (dict.getStatus() == null) dict.setStatus(1);
        dict.setCreateTime(LocalDateTime.now());
        dict.setUpdateTime(LocalDateTime.now());
        dictRepository.insert(dict);
        return dict.getId();
    }

    @Cacheable(value = "dictCache", key = "#id")
    @Transactional(readOnly = true)
    public DictResponse getDictById(Long id) {
        Dict dict = dictRepository.selectById(id);
        if (dict == null) throw new RuntimeException("字典不存在: " + id);
        DictResponse response = new DictResponse(dict);
        List<DictItem> items = dictItemRepository.findByDictIdAndStatus(id, 1);
        response.setItems(items.stream().map(DictItemResponse::new).collect(Collectors.toList()));
        return response;
    }

    @Cacheable(value = "dictCache", key = "'code_' + #dictCode")
    @Transactional(readOnly = true)
    public DictResponse getDictByCode(String dictCode) {
        Dict dict = dictRepository.selectByDictCode(dictCode);
        if (dict == null) throw new RuntimeException("字典不存在: " + dictCode);
        DictResponse response = new DictResponse(dict);
        List<DictItem> items = dictItemRepository.findByDictIdAndStatus(dict.getId(), 1);
        response.setItems(items.stream().map(DictItemResponse::new).collect(Collectors.toList()));
        return response;
    }

    @CacheEvict(value = "dictCache", allEntries = true)
    public DictResponse updateDict(Long id, DictRequest request) {
        Dict existingDict = dictRepository.selectById(id);
        if (existingDict == null) throw new RuntimeException("字典不存在: " + id);
        if (!existingDict.getDictCode().equals(request.getDictCode()) && dictRepository.existsByDictCode(request.getDictCode())) {
            throw new RuntimeException("字典编码已存在: " + request.getDictCode());
        }
        LocalDateTime originalCreateTime = existingDict.getCreateTime();
        Long originalCreateBy = existingDict.getCreateBy();
        BeanUtils.copyProperties(request, existingDict);
        existingDict.setId(id);
        existingDict.setCreateTime(originalCreateTime);
        existingDict.setCreateBy(originalCreateBy);
        existingDict.setUpdateTime(LocalDateTime.now());
        dictRepository.updateById(existingDict);
        return new DictResponse(existingDict);
    }

    @CacheEvict(value = "dictCache", allEntries = true)
    public void deleteDict(Long id) {
        Dict dict = dictRepository.selectById(id);
        if (dict == null) throw new RuntimeException("字典不存在: " + id);
        dictItemRepository.deleteByDictId(id);
        dictRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public IPage<DictResponse> queryDicts(DictQueryRequest queryRequest) {
        Page<Dict> page = new Page<>(queryRequest.getPage() + 1, queryRequest.getSize());
        IPage<Dict> dictPage = dictRepository.findByConditions(page, queryRequest.getDictCode(), queryRequest.getDictName(), queryRequest.getStatus());
        return dictPage.convert(DictResponse::new);
    }

    @Transactional(readOnly = true)
    public List<DictResponse> getAllActiveDicts() {
        return dictRepository.findByStatus(1).stream().map(DictResponse::new).collect(Collectors.toList());
    }

    @CacheEvict(value = "dictCache", allEntries = true)
    public Long createDictItem(DictItemRequest request) {
        Dict dict = dictRepository.selectById(request.getDictId());
        if (dict == null) throw new RuntimeException("字典不存在: " + request.getDictId());
        if (dictItemRepository.existsByDictIdAndItemValue(request.getDictId(), request.getItemValue())) {
            throw new RuntimeException("字典项值已存在: " + request.getItemValue());
        }
        DictItem dictItem = new DictItem();
        BeanUtils.copyProperties(request, dictItem);
        if (dictItem.getStatus() == null) dictItem.setStatus(1);
        if (dictItem.getItemSort() == null) dictItem.setItemSort(dictItemRepository.getMaxSortByDictId(request.getDictId()) + 1);
        dictItem.setCreateTime(LocalDateTime.now());
        dictItem.setUpdateTime(LocalDateTime.now());
        dictItemRepository.insert(dictItem);
        return dictItem.getId();
    }

    @Transactional(readOnly = true)
    public DictItemResponse getDictItemById(Long id) {
        DictItem dictItem = dictItemRepository.selectById(id);
        if (dictItem == null) throw new RuntimeException("字典项不存在: " + id);
        return new DictItemResponse(dictItem);
    }

    @CacheEvict(value = "dictCache", allEntries = true)
    public DictItemResponse updateDictItem(Long id, DictItemRequest request) {
        DictItem existingDictItem = dictItemRepository.selectById(id);
        if (existingDictItem == null) throw new RuntimeException("字典项不存在: " + id);
        if (!existingDictItem.getItemValue().equals(request.getItemValue()) && dictItemRepository.existsByDictIdAndItemValue(request.getDictId(), request.getItemValue())) {
            throw new RuntimeException("字典项值已存在: " + request.getItemValue());
        }
        LocalDateTime originalCreateTime = existingDictItem.getCreateTime();
        Long originalCreateBy = existingDictItem.getCreateBy();
        BeanUtils.copyProperties(request, existingDictItem);
        existingDictItem.setId(id);
        existingDictItem.setCreateTime(originalCreateTime);
        existingDictItem.setCreateBy(originalCreateBy);
        existingDictItem.setUpdateTime(LocalDateTime.now());
        dictItemRepository.updateById(existingDictItem);
        return new DictItemResponse(existingDictItem);
    }

    @CacheEvict(value = "dictCache", allEntries = true)
    public void deleteDictItem(Long id) {
        DictItem dictItem = dictItemRepository.selectById(id);
        if (dictItem == null) throw new RuntimeException("字典项不存在: " + id);
        dictItemRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<DictItemResponse> getDictItemsByDictId(Long dictId) {
        return dictItemRepository.findByDictId(dictId).stream().map(DictItemResponse::new).collect(Collectors.toList());
    }

    @Cacheable(value = "dictItemCache", key = "#dictCode")
    @Transactional(readOnly = true)
    public List<DictItemResponse> getDictItemsByCode(String dictCode) {
        Dict dict = dictRepository.selectByDictCode(dictCode);
        if (dict == null) throw new RuntimeException("字典不存在: " + dictCode);
        return dictItemRepository.findByDictIdAndStatus(dict.getId(), 1).stream().map(DictItemResponse::new).collect(Collectors.toList());
    }
}
