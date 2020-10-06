package com.arematics.minecraft.core.data.service.chat;

import com.arematics.minecraft.core.chat.ChatAPI;
import com.arematics.minecraft.core.data.model.theme.ChatTheme;
import com.arematics.minecraft.core.data.model.theme.ChatThemeUser;
import com.arematics.minecraft.core.data.repository.chat.ChatThemeRepository;
import com.arematics.minecraft.core.data.repository.chat.ChatThemeUserRepository;
import org.bukkit.Bukkit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class ChatThemeUserService {

    private final ChatThemeUserRepository repository;

    @Autowired
    public ChatThemeUserService(ChatThemeUserRepository repository) {
        this.repository = repository;
    }

    @Cacheable(cacheNames = "chatThemeUserCache")
    public ChatThemeUser findById(UUID id) {
        return repository.findById(id).orElse(null);
    }

    @CachePut(cacheNames = "chatThemeUserCache")
    public ChatThemeUser createUser(UUID id){
        ChatThemeUser user = new ChatThemeUser();
        user.setPlayerId(id);
        ChatTheme theme = ChatAPI.getTheme("default");
        user.setActiveTheme(theme);
        user = repository.save(user);
        return user;
    }

    public ChatThemeUser save(ChatThemeUser user) {
        return repository.save(user);
    }

    public ChatThemeUser getOrCreate(UUID id) {
        ChatThemeUser user = findById(id);
        if(null == user) {
            user = createUser(id);
        }
        return user;
    }

}
