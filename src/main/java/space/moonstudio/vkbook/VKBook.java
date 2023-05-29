package space.moonstudio.vkbook;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class VKBook extends JavaPlugin implements Listener {
    private List<String> players;
    private String lastPostText;

    @Getter
    private Utils utils;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadConfig();

        players = new ArrayList<>();
        utils = new Utils();

        try {
            lastPostText = getLatestPostFromVKGroup();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        getServer().getPluginManager().registerEvents(this, this);
        startPostCheckTimer();
    }

    @Override
    public void onDisable() {
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!players.contains(player.getName())) {
            showNewsBook(player);
            players.add(player.getName());
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player && command.getName().equalsIgnoreCase("news")) {
            showNewsBook((Player) sender);
            return true;
        }
        return false;
    }

    private void showNewsBook(Player player) {
        BookTools bookTools = new BookTools(getConfig().getString("book-author"), getConfig().getString("book-title"), this);
        bookTools.addText(lastPostText);
        bookTools.showBook(player);
    }

    private void startPostCheckTimer() {
        int interval = getConfig().getInt("check-interval");
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            try {
                String postText = getLatestPostFromVKGroup();
                if (!postText.equals(lastPostText)) {
                    players.clear();
                    lastPostText = postText;
                }
            } catch (IOException e) {
                getLogger().log(Level.WARNING, "Ошибка при получении поста из ВКонтакте.", e);
            }
        }, 1, interval);
    }

    private String getLatestPostFromVKGroup() throws IOException {
        String accessToken = getConfig().getString("vk-user-token");
        String groupId = getConfig().getString("vk-group-id");
        URL url = new URL("https://api.vk.com/method/wall.get?v=5.131&owner_id=-" + groupId + "&count=5&access_token=" + accessToken);
        BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        JsonParser parser = new JsonParser();
        JsonObject responseObject = parser.parse(response.toString()).getAsJsonObject();
        JsonArray itemsArray = responseObject.getAsJsonObject("response").getAsJsonArray("items");
        if (itemsArray.size() > 0) {
            JsonObject latestPost = null;
            long latestPostDate = 0;

            for (JsonElement item : itemsArray) {
                JsonObject post = item.getAsJsonObject();
                long postDate = post.get("date").getAsLong();

                if (postDate > latestPostDate) {
                    latestPost = post;
                    latestPostDate = postDate;
                }
            }

            if (latestPost != null) {
                String postText = latestPost.get("text").getAsString();
                return postText;
            }
        }

        return "";
    }
}
