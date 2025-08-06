package com.dew.system.mongodb;

import com.dew.DewCommon;
import com.dew.IMinecraft;
import com.dew.system.event.EventListener;
import com.dew.system.event.events.*;
import com.dew.utils.LogUtil;
import com.dew.utils.Timer;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.entity.player.EntityPlayer;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@SuppressWarnings("ALL")
public class MongoManager implements IMinecraft, EventListener {

    private final MongoClient mongoClient;
    private final MongoCollection<Document> collection;
    private final String uri;
    private final Timer tickTimer = new Timer();
    public final List<EntityPlayer> online = new CopyOnWriteArrayList<>();
    private boolean connected = false;

    public MongoManager() {
        DewCommon.eventManager.register(this);
        this.uri = "mongodb+srv://dewclientuser:TcZn7M4gtdoI8QyD@cluster0.rejop8c.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0";
        this.mongoClient = MongoClients.create(uri);
        MongoDatabase database = mongoClient.getDatabase("client_data1");
        this.collection = database.getCollection("server_users1");

        LogUtil.infoLog("init mongoManager");
    }

    public boolean isConnected() {
        return this.connected;
    }

    public void addUserToServer(String serverIP, String username) {
        if (collection == null || serverIP == null || username == null) return;
        new Thread(() -> {
            try {
                Document query = new Document("server_ip", serverIP)
                        .append("username", username);

                collection.insertOne(query);
                LogUtil.infoLog(String.format("Added %s to %s", username, serverIP));
                connected = true;
            } catch (Exception e) {
                LogUtil.infoLog("Failed to add user: " + e.getMessage());
            }
        }).start();
    }

    public void removeUserFromServer(String serverIP, String username) {
        if (collection == null || serverIP == null || username == null) return;
        new Thread(() -> {
            try {
                Document query = new Document("server_ip", serverIP)
                        .append("username", username);

                collection.deleteOne(query);
                LogUtil.infoLog(String.format("Removed %s from %s", username, serverIP));
                connected = false;
            } catch (Exception e) {
                LogUtil.infoLog("Failed to remove user: " + e.getMessage());
            }
        }).start();
    }

    public void removeUserFromAllServers(String username) {
        if (collection == null || username == null) return;
        new Thread(() -> {
            try {
                Document query = new Document("username", username);
                collection.deleteMany(query);
                LogUtil.infoLog(String.format("Removed %s from all servers", username));
                connected = false;
            } catch (Exception e) {
                LogUtil.infoLog("Failed to remove from all servers: " + e.getMessage());
            }
        }).start();
    }

    public List<String> getUsersOnServer(String serverIP) {
        List<String> usernames = new ArrayList<>();
        try {
            FindIterable<Document> results = collection.find(Filters.eq("server_ip", serverIP));
            for (Document doc : results) {
                usernames.add(doc.getString("username"));
            }
        } catch (Exception e) {
            LogUtil.infoLog("getUsersOnServer failed: " + e.getMessage());
        }
        return usernames;
    }

    public boolean isUserOnServer(String serverIP, String username) {
        try {
            Document query = new Document("server_ip", serverIP)
                    .append("username", username);
            LogUtil.infoLog("Looking for {} on {}".format(username, serverIP));
            return collection.find(query).first() != null;
        } catch (Exception e) {
            LogUtil.infoLog("isUserOnServer failed: " + e.getMessage());
            return false;
        }
    }

    @Override
    public void onTablistPlayerNameFetch(TablistPlayerNameFetchEvent event) {
        if (!connected || mc == null || mc.theWorld == null || mc.thePlayer == null) return;
        ServerData serverData = mc.getCurrentServerData();
        if (serverData == null) return;

        for (EntityPlayer player : online) {
            if (player != null && event.name != null && player.getName() != null && event.name.contains(player.getName())) {
                event.name = "§n§b[Dew] §r" + player.getName();
            }
        }
    }

    @Override
    public void onTick(TickEvent event) {
        if (!connected || mc == null || mc.theWorld == null || mc.thePlayer == null || !mc.inGameHasFocus) return;
        if (!tickTimer.hasElapsed(5000)) return;

        new Thread(() -> {
            try {
                ServerData serverData = mc.getCurrentServerData();
                if (serverData == null || collection == null) return;

                List<String> users = getUsersOnServer(serverData.serverIP);
                if (users == null) return;

                for (EntityPlayer player : mc.theWorld.playerEntities) {
                    if (player != null && users.contains(player.getName())) {
                        online.remove(player);
                        online.add(player);
                    }
                }
                tickTimer.reset();
            } catch (Exception e) {
                LogUtil.infoLog("Tick Mongo Update failed: " + e.getMessage());
            }
        }).start();
    }

    @Override
    public void onWorld(WorldEvent event) {
        if (mc == null || mc.getSession() == null || event == null || event.ip == null) return;
        String username = mc.getSession().getUsername();
        if (username == null) return;

        removeUserFromAllServers(username);
        addUserToServer(event.ip, username);
    }

    @Override
    public void onLeaveWorld(LeaveWorldEvent event) {
        if (mc == null || mc.getSession() == null) return;
        String username = mc.getSession().getUsername();
        if (username == null) return;

        removeUserFromAllServers(username);
    }

    @Override
    public void onGuiDisconnected(GuiDisconnectedEvent event) {
        if (mc == null || mc.getSession() == null) return;
        String username = mc.getSession().getUsername();
        if (username == null) return;

        removeUserFromAllServers(username);
    }

    @Override
    public void onGuiConnecting(GuiConnectingEventActionPerformed event) {
        if (mc == null || mc.getSession() == null) return;
        String username = mc.getSession().getUsername();
        if (username == null) return;

        removeUserFromAllServers(username);
    }
}