package com.dew.system.mongodb;

import com.dew.DewCommon;
import com.dew.IMinecraft;
import com.dew.system.event.EventListener;
import com.dew.system.event.events.*;
import com.dew.system.userdata.DataSaver;
import com.dew.utils.Clock;
import com.dew.utils.LogUtil;
import com.dew.utils.ServerUtil;
import com.dew.utils.Timer;
import com.mongodb.MongoException;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.entity.player.EntityPlayer;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.Document;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings("ALL")
public class MongoManager implements IMinecraft, EventListener {

    private MongoClient mongoClient;
    private MongoCollection<Document> collection;
    private final String uri;
    private final Timer tickTimer = new Timer();
    public final List<Pair<EntityPlayer, String>> online = Collections.synchronizedList(new ArrayList<>());
    private final AtomicBoolean connected = new AtomicBoolean(false);
    private final ExecutorService dbExecutor = Executors.newFixedThreadPool(3);
    private final Object userLock = new Object();
    private final Clock addUserTimer = new Clock();

    private static final long RECONNECT_RETRY_INTERVAL_MS = 5000;

    public MongoManager() {
        DewCommon.eventManager.register(this);
        this.uri = "mongodb+srv://dewclientuser:TcZn7M4gtdoI8QyD@cluster0.rejop8c.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0";
        initMongo();

        LogUtil.infoLog("init mongoManager");
    }

    private synchronized void initMongo() {
        try {
            if (mongoClient != null) {
                try {
                    mongoClient.close();
                } catch (Exception ignored) {
                }
            }
            mongoClient = MongoClients.create(uri);
            MongoDatabase database = mongoClient.getDatabase("client_data1");
            collection = database.getCollection("server_users1");
            connected.set(checkConnection());

            if (connected.get()) {
                LogUtil.infoLog("MongoDB connection established");
            } else {
                LogUtil.infoLog("MongoDB connection could not be established");
            }
        } catch (Exception e) {
            connected.set(false);
            LogUtil.infoLog("Mongo init failed: " + e.getMessage());
        }
    }

    private boolean checkConnection() {
        if (mongoClient == null) return false;
        try {
            mongoClient.getDatabase("admin").runCommand(new Document("ping", 1));
            return true;
        } catch (MongoException e) {
            LogUtil.infoLog("MongoDB ping failed: " + e.getMessage());
            return false;
        } catch (Exception e) {
            LogUtil.infoLog("Unexpected error during MongoDB ping: " + e.getMessage());
            return false;
        }
    }

    public boolean isConnected() {
        return connected.get();
    }

    private void runAsync(Runnable task) {
        dbExecutor.submit(() -> {
            try {
                task.run();
            } catch (Exception e) {
                LogUtil.infoLog("DB Task Error: " + e.getMessage());
                connected.set(false);
                reconnectWithBackoff();
            }
        });
    }

    private synchronized void reconnectWithBackoff() {
        LogUtil.infoLog("Attempting to reconnect to MongoDB...");
        int attempts = 0;
        while (!connected.get() && attempts < 5) {
            try {
                if (mongoClient != null) {
                    try {
                        mongoClient.close();
                    } catch (Exception ignored) {
                    }
                }
                initMongo();
                if (connected.get()) {
                    LogUtil.infoLog("MongoDB reconnection succeeded");
                    return;
                }
            } catch (Exception e) {
                LogUtil.infoLog("MongoDB reconnection attempt failed: " + e.getMessage());
            }
            attempts++;
            try {
                Thread.sleep(RECONNECT_RETRY_INTERVAL_MS);
            } catch (InterruptedException ignored) {
            }
        }
        if (!connected.get()) {
            LogUtil.infoLog("MongoDB reconnection failed after " + attempts + " attempts");
        }
    }

    private synchronized void reconnect() {
        LogUtil.infoLog("Reconnecting to MongoDB...");
        try {
            if (mongoClient != null) mongoClient.close();
        } catch (Exception ignored) {}
        initMongo();
    }

    public void addUserToServer(String serverIP, String username) {
        if (!connected.get() || collection == null || serverIP == null || username == null) return;

        runAsync(() -> {
            synchronized (userLock) {
                try {
                    Document query = new Document("server_ip", serverIP).append("username", username);
                    collection.insertOne(query);
                    connected.set(true);
                } catch (MongoException e) {
                    LogUtil.infoLog("MongoDB insertOne failed: " + e.getMessage());
                    connected.set(false);
                    reconnectWithBackoff();
                } catch (Exception e) {
                    LogUtil.infoLog("Unexpected error in addUserToServer: " + e.getMessage());
                    connected.set(false);
                    reconnectWithBackoff();
                }
            }
        });
    }

    public void removeUserFromAllServers(String username) {
        if (!connected.get() || collection == null || username == null) return;

        runAsync(() -> {
            synchronized (userLock) {
                try {
                    Document query = new Document("username", username);
                    collection.deleteMany(query);
                    connected.set(true);
                } catch (MongoException e) {
                    LogUtil.infoLog("MongoDB deleteMany failed: " + e.getMessage());
                    connected.set(false);
                    reconnectWithBackoff();
                } catch (Exception e) {
                    LogUtil.infoLog("Unexpected error in removeUserFromAllServers: " + e.getMessage());
                    connected.set(false);
                    reconnectWithBackoff();
                }
            }
        });
    }

    public List<String> getUsersOnServer(String serverIP) {
        List<String> usernames = new ArrayList<>();
        if (!connected.get() || collection == null || serverIP == null) return usernames;

        try {
            FindIterable<Document> results = collection.find(Filters.eq("server_ip", serverIP));
            for (Document doc : results) {
                String username = doc.getString("username");
                if (username != null) usernames.add(username);
            }
        } catch (MongoException e) {
            LogUtil.infoLog("getUsersOnServer failed: " + e.getMessage());
            connected.set(false);
            reconnectWithBackoff();
        } catch (Exception e) {
            LogUtil.infoLog("Unexpected error in getUsersOnServer: " + e.getMessage());
            connected.set(false);
            reconnectWithBackoff();
        }
        return usernames;
    }

    private String normalizeServerIP(String serverIP) {
        if (serverIP == null) return null;
        try {
            String[] parts = serverIP.split(":");
            String host = parts[0];
            String port = (parts.length > 1) ? parts[1] : "25565";
            String ip = InetAddress.getByName(host).getHostAddress();
            return ip + ":" + port;
        } catch (Exception e) {
            return serverIP;
        }
    }

    @Override
    public void onTablistPlayerNameFetch(TablistPlayerNameFetchEvent event) {
        if (!connected.get() || mc == null || mc.theWorld == null || mc.thePlayer == null) return;
        ServerData serverData = mc.getCurrentServerData();
        if (serverData == null) return;

        synchronized (online) {
            for (Pair<EntityPlayer, String> entry : online) {
                EntityPlayer mcUserEntity = entry.getLeft();
                String clientUsername = entry.getRight();

                if (mcUserEntity != null && event.name != null && mcUserEntity.getName() != null && event.name.contains(mcUserEntity.getName())) {
                    event.name = "§n§b[" + clientUsername + "] §r" + mcUserEntity.getName();
                }
            }
        }
    }

    @Override
    public void onTick(TickEvent event) {
        if (!connected.get() || mc.theWorld == null || mc.thePlayer == null) return;

        if (!mc.isSingleplayer() && !online.stream().anyMatch(p -> p.getLeft().equals(mc.thePlayer) && p.getRight().equals(DataSaver.userName)) && addUserTimer.hasTimePassed(2500) && ServerUtil.serverData != null) {
            String username = mc.getSession().getUsername() + "~~--~~" + DataSaver.userName;
            String normalizedIP = normalizeServerIP(ServerUtil.serverData.serverIP);
            addUserToServer(normalizedIP, username);
            addUserTimer.reset();
        }

        if (!tickTimer.hasElapsed(5000)) return;

        runAsync(() -> {
            ServerData serverData = mc.getCurrentServerData();
            if (serverData == null) return;

            String normalizedIP = normalizeServerIP(serverData.serverIP);
            if (normalizedIP == null) return;

            List<String> users = getUsersOnServer(normalizedIP);
            if (users == null) return;

            List<Pair<EntityPlayer, String>> newOnline = new ArrayList<>();
            for (String unformattedName : users) {
                if (unformattedName == null) continue;

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

            synchronized (online) {
                online.removeIf(p -> !newOnline.contains(p));

                for (Pair<EntityPlayer, String> p : newOnline) {
                    if (!online.contains(p)) {
                        online.add(p);
                    }
                }
            }

            tickTimer.reset();
        });
    }

    @Override
    public void onWorld(WorldEvent event) {
        addUserTimer.reset();
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

    public void shutdown() {
        try {
            String username = IMinecraft.mc.getSession().getUsername() + "~~--~~" + DataSaver.userName;
            removeUserFromAllServers(username);
            dbExecutor.shutdown();
            if (!dbExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                dbExecutor.shutdownNow();
            }
            if (mongoClient != null) {
                mongoClient.close();
            }
            connected.set(false);
        } catch (Exception e) {
            LogUtil.infoLog("Error shutting down MongoManager: " + e.getMessage());
        }
    }
}