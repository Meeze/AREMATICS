package com.arematics.minecraft.core.messaging.injector;

import com.arematics.minecraft.core.configurations.Config;
import com.arematics.minecraft.core.messaging.MessageHighlight;
import org.apache.commons.lang.text.StrSubstitutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BasicInjector extends StringInjector {

    protected final List<CommandSender> SENDER_LIST;
    protected final MessageHighlight HIGHLIGHT;
    protected final String RAW_MESSAGE;
    protected final Map<String, String> INJECTOR_VALUES = new HashMap<>();
    protected boolean serverPrefix = true;

    public BasicInjector(List<CommandSender> senderList, MessageHighlight highlight,
                         String rawMessage){
        this.SENDER_LIST = senderList;
        this.HIGHLIGHT = highlight;
        this.RAW_MESSAGE = rawMessage;
    }

    @Override
    public BasicInjector replace(String pattern, String replace){
        this.INJECTOR_VALUES.put(pattern, replace);
        return this;
    }

    public BasicInjector disableServerPrefix() {
        this.serverPrefix = false;
        return this;
    }

    @Override
    protected String prepareMessage(CommandSender sender) {
        StringBuilder builder = new StringBuilder();
        if(serverPrefix) builder.append(Config.getInstance().getPrefix()).append(this.HIGHLIGHT.getColorCode());
        builder.append(this.RAW_MESSAGE);
        return builder.toString();
    }

    @Override
    protected String injectValues(String income) {
        StrSubstitutor substitutor = new StrSubstitutor(this.INJECTOR_VALUES, "%", "%");
        return substitutor.replace(income);
    }

    @Override
    public void handle() {
        SENDER_LIST.forEach(sender -> {
            String preparedMessage = prepareMessage(sender);
            String msg = injectValues(preparedMessage);
            sender.sendMessage(msg);
            if(!this.HIGHLIGHT.getColorCode().equals("§a") && sender instanceof Player)
                ((Player)sender).playSound(((Player)sender).getLocation(), this.HIGHLIGHT.getSound(), 1, 1);
        });
    }
}
