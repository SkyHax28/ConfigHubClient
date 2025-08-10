package com.dew.system.mongodb;

import com.dew.DewCommon;
import com.dew.IMinecraft;
import com.dew.system.event.EventListener;
import com.dew.system.event.events.*;
import com.dew.system.userdata.DataSaver;
import com.dew.utils.LogUtil;
import com.dew.utils.Timer;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.entity.player.EntityPlayer;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.Document;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SuppressWarnings("ALL")
public class MongoManager implements IMinecraft, EventListener {

    private MongoClient mongoClient;
    private MongoCollection<Document> collection;
    private final String uri;
    private final Timer tickTimer = new Timer();
    public final List<Pair<EntityPlayer, String>> online = new CopyOnWriteArrayList<>();
    private boolean connected = false;
    private final ExecutorService dbExecutor = Executors.newFixedThreadPool(3);
    private final Object userLock = new Object();

    public MongoManager() {
        DewCommon.eventManager.register(this);
        this.uri = "mongodb+srv://dewclientuser:TcZn7M4gtdoI8QyD@cluster0.rejop8c.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0";
        initMongo();

        LogUtil.infoLog("init mongoManager");
    }

    private synchronized void initMongo() {
        try {
            mongoClient = MongoClients.create(uri);
            MongoDatabase database = mongoClient.getDatabase("client_data1");
            collection = database.getCollection("server_users1");
            connected = checkConnection();
            LogUtil.infoLog("MongoDB connection established");
        } catch (Exception e) {
            connected = false;
            LogUtil.infoLog("Mongo init failed: " + e.getMessage());
        }
    }

    private boolean checkConnection() {
        try {
            mongoClient.getDatabase("admin").runCommand(new Document("ping", 1));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isConnected() {
        return this.connected;
    }

    private void runAsync(Runnable task) {
        dbExecutor.submit(() -> {
            try {
                task.run();
            } catch (Exception e) {
                LogUtil.infoLog("DB Task Error: " + e.getMessage());
                connected = false;
                reconnect();
            }
        });
    }

    private synchronized void reconnect() {
        LogUtil.infoLog("Reconnecting to MongoDB...");
        try {
            if (mongoClient != null) mongoClient.close();
        } catch (Exception ignored) {}
        initMongo();
    }

    public void addUserToServer(String serverIP, String username) {
        if (collection == null || serverIP == null || username == null) return;
        runAsync(() -> {
            synchronized (userLock) {
                Document query = new Document("server_ip", serverIP)
                        .append("username", username);

                collection.insertOne(query);
                LogUtil.infoLog(String.format("Added %s to %s", username, serverIP));
                connected = true;
            }
        });
    }

    public void removeUserFromAllServers(String username) {
        if (collection == null || username == null) return;
        runAsync(() -> {
            synchronized (userLock) {
                Document query = new Document("username", username);
                collection.deleteMany(query);
                LogUtil.infoLog(String.format("Removed %s from all servers", username));
            }
        });
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

    private String normalizeServerIP(String serverIP) {
        try {
            String[] parts = serverIP.split(":");
            String host = parts[0];
            String port = (parts.length > 1) ? parts[1] : "25565";
            String ip = InetAddress.getByName(host).getHostAddress();
            return ip + ":" + port;
        } catch (Exception e) {
            return serverIP.toLowerCase();
        }
    }

    @Override
    public void onTablistPlayerNameFetch(TablistPlayerNameFetchEvent event) {
        if (!connected || mc == null || mc.theWorld == null || mc.thePlayer == null) return;
        ServerData serverData = mc.getCurrentServerData();
        if (serverData == null) return;

        for (Pair<EntityPlayer, String> entry : online) {
            EntityPlayer mcUserEntity = entry.getLeft();
            String clientUsername = entry.getRight();

            if (mcUserEntity != null && event.name != null && mcUserEntity.getName() != null && event.name.contains(mcUserEntity.getName())) {
                event.name = "§n§b[" + clientUsername + "] §r" + mcUserEntity.getName();
            }
        }
    }

    @Override
    public void onTick(TickEvent event) {
        if (!connected || mc == null || mc.theWorld == null || mc.thePlayer == null || !mc.inGameHasFocus) return;
        if (!tickTimer.hasElapsed(5000)) return;

        runAsync(() -> {
            ServerData serverData = mc.getCurrentServerData();
            if (serverData == null || collection == null) return;

            List<String> users = getUsersOnServer(normalizeServerIP(serverData.serverIP));
            if (users == null) return;

            List<Pair<EntityPlayer, String>> newOnline = new ArrayList<>();

            for (String unformattedName : users) {
                if (unformattedName.contains("~~--~~")) {
                    String[] parts = unformattedName.split("~~--~~", 2);
                    String mcUsername = parts[0];
                    String clientUsername = parts.length > 1 ? parts[1] : "";

                    for (EntityPlayer player : mc.theWorld.playerEntities) {
                        if (player != null && mcUsername.equals(player.getName())) {
                            newOnline.add(Pair.of(player, clientUsername));
                        }
                    }
                }
            }

            online.retainAll(newOnline);
            for (Pair<EntityPlayer, String> p : newOnline) {
                if (!online.contains(p)) {
                    online.add(p);
                }
            }

            tickTimer.reset();
        });
    }

    @Override
    public void onWorld(WorldEvent event) {
        if (mc == null || mc.getSession() == null || event == null || event.ip == null) return;
        String username = mc.getSession().getUsername() + "~~--~~" + DataSaver.userName;
        addUserToServer(normalizeServerIP(event.ip), username);
    }

    @Override
    public void onLeaveWorld(LeaveWorldEvent event) {
        if (mc == null || mc.getSession() == null) return;
        String username = mc.getSession().getUsername() + "~~--~~" + DataSaver.userName;
        removeUserFromAllServers(username);
    }

    @Override
    public void onGuiDisconnected(GuiDisconnectedEvent event) {
        if (mc == null || mc.getSession() == null) return;
        String username = mc.getSession().getUsername() + "~~--~~" + DataSaver.userName;
        removeUserFromAllServers(username);
    }
}