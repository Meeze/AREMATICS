package com.arematics.minecraft.data.service;

import com.arematics.minecraft.data.global.model.Ban;
import com.arematics.minecraft.data.global.repository.BansRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class BanService {

    private final BansRepository repository;

    @Autowired
    public BanService(BansRepository bansRepository){
        this.repository = bansRepository;
    }

    @Cacheable(cacheNames = "banCache", key = "#uuid")
    public Ban getBan(UUID uuid){
        Optional<Ban> ban = repository.findById(uuid);
        if(!ban.isPresent())
            throw new RuntimeException("Ban for uuid: " + uuid.toString() + " could not be found");
        return ban.get();
    }

    @CachePut(cacheNames = "banCache", key = "#result.uuid")
    public Ban save(Ban ban){
        return repository.save(ban);
    }

    @CacheEvict(cacheNames = "banCache", key = "#ban.uuid")
    public void remove(Ban ban){
        repository.delete(ban);
    }
}
