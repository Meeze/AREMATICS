package com.arematics.minecraft.data.service;

import com.arematics.minecraft.data.mode.model.Clan;
import com.arematics.minecraft.data.mode.repository.ClanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;

@Service
public class ClanService {

    private final ClanRepository repository;

    @Autowired
    public ClanService(ClanRepository clanRepository){
        this.repository = clanRepository;
    }

    @Cacheable(cacheNames = "clanCache")
    public Clan findClanById(long id){
        Optional<Clan> clan = repository.findById(id);
        if(!clan.isPresent()) throw new RuntimeException("Clan with id: " + id + " could not be found");
        return clan.get();
    }

    @Cacheable(cacheNames = "clanCache")
    public Clan findClanByName(String name){
        Optional<Clan> clan = repository.findByName(name);
        if(!clan.isPresent()) throw new RuntimeException("Clan with name: " + name + " could not be found");
        return clan.get();
    }

    @Cacheable(cacheNames = "clanCache")
    public Clan findClanByTag(String tag){
        Optional<Clan> clan = repository.findByTag(tag);
        if(!clan.isPresent()) throw new RuntimeException("Clan with tag: " + tag + " could not be found");
        return clan.get();
    }

    public Clan createClan(String name, String tag){
        Clan clan = new Clan(null, name, tag, "§b", (byte) 10, 0, 0, 0L,
                new HashSet<>(), new HashSet<>());
        return repository.save(clan);
    }

    @CachePut(cacheNames = "clanCache")
    public Clan update(Clan clan){
        return repository.save(clan);
    }

    @CacheEvict(cacheNames = "clanCache")
    public void delete(Clan clan){
        repository.delete(clan);
    }
}
