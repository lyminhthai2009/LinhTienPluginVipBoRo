package me.yogurt0811.linhtien.commands;

import me.yogurt0811.linhtien.LinhTienPlugin;
import me.yogurt0811.linhtien.config.MessageUtils;
import me.yogurt0811.linhtien.data.PlayerData;
import me.yogurt0811.linhtien.events.PlayerRealmChangeEvent;
import me.yogurt0811.linhtien.realms.Realm;
import me.yogurt0811.linhtien.realms.RealmManager;
import me.yogurt0811.linhtien.talents.Talent;
import me.yogurt0811.linhtien.talents.TalentManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class LinhTienAdminCommand implements TabExecutor {
    private final LinhTienPlugin plugin;

    public LinhTienAdminCommand(LinhTienPlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sender.sendMessage(MessageUtils.format("&b&lLinhTienAdmin v" + plugin.getDescription().getVersion()));
            sender.sendMessage(MessageUtils.format("&f/lta player <tên> ... &7- Quản lý người chơi."));
            sender.sendMessage(MessageUtils.format("&f/lta skill <tên> ... &7- Quản lý kỹ năng người chơi."));
            sender.sendMessage(MessageUtils.format("&f/lta giveskillbook ... &7- Trao sách kỹ năng."));
            sender.sendMessage(MessageUtils.format("&f/lta reload &7- Tải lại cấu hình plugin."));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload": plugin.reloadPlugin(); MessageUtils.sendMessage(sender, "reload-success"); return true;
            case "player": return handlePlayerCommand(sender, Arrays.copyOfRange(args, 1, args.length));
            case "skill": return handleSkillCommand(sender, Arrays.copyOfRange(args, 1, args.length));
            case "giveskillbook": return handleGiveSkillBook(sender, Arrays.copyOfRange(args, 1, args.length));
            default: sender.sendMessage(MessageUtils.format("&c Lệnh không xác định. Sử dụng /lta.")); return true;
        }
    }

    private boolean handlePlayerCommand(CommandSender sender, String[] args) {
        if (args.length < 2) { sender.sendMessage(MessageUtils.format("&cCách dùng: /lta player <tên> <info|set|add> ...")); return true; }
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) { MessageUtils.sendMessage(sender, "admin-player-not-found", "player_name", args[0]); return true; }
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(target);
        if (data == null) return true;

        switch (args[1].toLowerCase()) {
            case "info":
                Realm r=plugin.getRealmManager().getPlayerRealm(data); Talent t=plugin.getTalentManager().getTalent(data.getTalentId());
                sender.sendMessage(MessageUtils.format("&bInfo: "+target.getName()));
                sender.sendMessage(MessageUtils.format("&3- Cảnh giới: &f"+(r!=null?r.displayName():"N/A")));
                return true;
            case "set": return handlePlayerSet(sender, target, data, Arrays.copyOfRange(args, 2, args.length));
            case "add": return handlePlayerAdd(sender, target, data, Arrays.copyOfRange(args, 2, args.length));
            default: sender.sendMessage(MessageUtils.format("&cDùng: info, set, add.")); return true;
        }
    }

    private boolean handlePlayerSet(CommandSender sender, Player target, PlayerData data, String[] args) {
        if (args.length < 2) { sender.sendMessage(MessageUtils.format("&cCách dùng: /lta player <tên> set <realm|exp|talent> <giá trị>")); return true; }
        String type = args[0].toLowerCase(), value = args[1];
        switch (type) {
            case "realm":
                Realm oldR = plugin.getRealmManager().getPlayerRealm(data), newR = plugin.getRealmManager().getRealmById(value);
                if (newR==null) { MessageUtils.sendMessage(sender, "admin-realm-not-found", "realm_id", value); return true; }
                data.setRealmId(newR.id()); data.setCurrentExp(0);
                Bukkit.getPluginManager().callEvent(new PlayerRealmChangeEvent(target, oldR, newR));
                MessageUtils.sendMessage(sender, "admin-set-realm-success", "player_name", target.getName(), "realm_name", newR.displayName());
                break;
            case "exp": try { data.setCurrentExp(Double.parseDouble(value)); MessageUtils.sendMessage(sender, "admin-set-exp-success", "player_name", target.getName(), "amount", value); } catch(Exception e){MessageUtils.sendMessage(sender,"admin-invalid-number","number",value);} break;
            case "talent":
                Talent t = plugin.getTalentManager().getTalent(value);
                if (t==null) { MessageUtils.sendMessage(sender, "admin-talent-not-found", "talent_id", value); return true; }
                data.setTalentId(t.id()); MessageUtils.sendMessage(sender, "admin-set-talent-success", "player_name", target.getName(), "talent_name", t.displayName());
                break;
            default: sender.sendMessage(MessageUtils.format("&cDùng: realm, exp, talent.")); return true;
        }
        return true;
    }

    private boolean handlePlayerAdd(CommandSender sender, Player target, PlayerData data, String[] args) {
        if (args.length < 2) { sender.sendMessage(MessageUtils.format("&cCách dùng: /lta player <tên> add exp <số>")); return true; }
        if ("exp".equals(args[0].toLowerCase())) {
            try { double exp=Double.parseDouble(args[1]); data.addExp(exp); MessageUtils.sendMessage(sender, "admin-add-exp-success", "player_name", target.getName(), "amount", args[1]); } catch (Exception e) { MessageUtils.sendMessage(sender, "admin-invalid-number", "number", args[1]); }
        } else sender.sendMessage(MessageUtils.format("&cChỉ có thể 'add exp'."));
        return true;
    }

    private boolean handleSkillCommand(CommandSender sender, String[] args) {
        if (args.length < 3) { sender.sendMessage(MessageUtils.format("&cCách dùng: /lta skill <player> <learn|forget> <skill_id>")); return true; }
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) { MessageUtils.sendMessage(sender, "admin-player-not-found", "player_name", args[0]); return true; }
        String action = args[1].toLowerCase(), skillId = args[2].toLowerCase();
        if (plugin.getSkillManager().getSkill(skillId)==null) { sender.sendMessage(MessageUtils.format("&cKhông thấy skill ID: "+skillId)); return true; }
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(target);
        if ("learn".equals(action)) { data.learnSkill(skillId); sender.sendMessage(MessageUtils.format("&aĐã dạy skill &e"+skillId+"&a cho &e"+target.getName())); }
        else if ("forget".equals(action)) { data.forgetSkill(skillId); sender.sendMessage(MessageUtils.format("&aĐã khiến &e"+target.getName()+"&a quên skill &e"+skillId)); }
        else sender.sendMessage(MessageUtils.format("&cDùng: learn, forget."));
        return true;
    }
    
    private boolean handleGiveSkillBook(CommandSender sender, String[] args) {
        if (args.length < 2) { sender.sendMessage(MessageUtils.format("&cCách dùng: /lta giveskillbook <player> <skill_id>")); return true; }
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) { MessageUtils.sendMessage(sender, "admin-player-not-found", "player_name", args[0]); return true; }
        String skillId = args[1].toLowerCase();
        ItemStack book = plugin.getSkillBookManager().createSkillBook(skillId);
        if (book == null) { sender.sendMessage(MessageUtils.format("&cKhông thấy skill ID: "+skillId)); return true; }
        target.getInventory().addItem(book);
        sender.sendMessage(MessageUtils.format("&aĐã trao sách kỹ năng &e"+skillId+"&a cho &e"+target.getName()));
        return true;
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) return filter(List.of("player", "reload", "skill", "giveskillbook"), args[0]);
        if (args[0].equalsIgnoreCase("player")) {
            if (args.length == 2) return null;
            if (args.length == 3) return filter(List.of("info", "set", "add"), args[2]);
            if (args.length == 4) { if(args[2].equalsIgnoreCase("set")) return filter(List.of("realm", "exp", "talent"), args[3]); if(args[2].equalsIgnoreCase("add")) return filter(List.of("exp"), args[3]); }
            if (args.length == 5 && args[2].equalsIgnoreCase("set")) {
                if(args[3].equalsIgnoreCase("realm")) return filter(plugin.getRealmManager().getRealms().stream().map(Realm::id).toList(), args[4]);
                if(args[3].equalsIgnoreCase("talent")) return filter(new ArrayList<>(plugin.getTalentManager().getTalentMap().keySet()), args[4]);
            }
        }
        if (args[0].equalsIgnoreCase("skill")) {
            if (args.length == 2) return null;
            if (args.length == 3) return filter(List.of("learn", "forget"), args[2]);
            if (args.length == 4) return filter(new ArrayList<>(plugin.getSkillManager().getAllSkills().keySet()), args[3]);
        }
        if (args[0].equalsIgnoreCase("giveskillbook")) {
            if (args.length == 2) return null;
            if (args.length == 3) return filter(new ArrayList<>(plugin.getSkillManager().getAllSkills().keySet()), args[2]);
        }
        return List.of();
    }
    
    private List<String> filter(List<String> s, String i) { return s.stream().filter(str->str.toLowerCase().startsWith(i.toLowerCase())).collect(Collectors.toList()); }
}