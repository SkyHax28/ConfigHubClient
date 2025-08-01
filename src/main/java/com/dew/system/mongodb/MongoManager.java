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

    private MongoCollection<Document> collection;
    private final String uri;
    public MongoManager(){
        DewCommon.eventManager.register(this);
        LogUtil.infoLog("init MongoManager");
        this.uri = "mongodb+srv://dewclientuser:TcZn7M4gtdoI8QyD@cluster0.rejop8c.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0";
        init();
    }


    public void init() {
        MongoClient mongoClient = MongoClients.create(uri);
        MongoDatabase database = mongoClient.getDatabase("client_data1");
        collection = database.getCollection("server_users1");
    }

    public void addUserToServer(String serverIP, String username) {
        Document query = new Document("server_ip", serverIP)
                .append("username", username);

        collection.insertOne(query);
        LogUtil.infoLog("Added {} to {}".format(username, serverIP));
        connected = true;
    }

    public void removeUserFromServer(String serverIP, String username) {
        Document query = new Document("server_ip", serverIP)
                .append("username", username);

        collection.deleteOne(query);
        LogUtil.infoLog("Removed {} from {}".format(username, serverIP));
        connected = false;
    }

    public void removeUserFromAllServers(String username) {
        Document query = new Document("username", username);
        collection.deleteMany(query);
        LogUtil.infoLog("Removed {} from all servers".format(username));
        connected = false;
    }

    public List<String> getUsersOnServer(String serverIP) {
        List<String> usernames = new ArrayList<>();
        FindIterable<Document> results = collection.find(Filters.eq("server_ip", serverIP));

        for (Document doc : results) {
            usernames.add(doc.getString("username"));
        }

        return usernames;
    }

    public boolean isUserOnServer(String serverIP, String username) {
        Document query = new Document("server_ip", serverIP)
                .append("username", username);

        LogUtil.infoLog("Looking for {} on {}".format(username, serverIP));
        return collection.find(query).first() != null;
    }

    private boolean connected = false;
    private final Timer tickTimer = new Timer();
    private final List<EntityPlayer> online = new CopyOnWriteArrayList<>();

    @Override
    public void onTablistPlayerNameFetch(TablistPlayerNameFetchEvent event) {
        if (connected && mc.theWorld != null && mc.thePlayer != null) {
            final ServerData serverData = mc.getCurrentServerData();

            if (serverData != null) {
                for (EntityPlayer player : online) {
                    if (event.name.contains(player.getName())) {
                        event.name = "§n§b[Dew] §r" + player.getName();
                    }
                }
            }
        }
    }

    @Override
    public void onTick(TickEvent event) {
        if (connected && tickTimer.hasElapsed(5000) && mc.theWorld != null && mc.thePlayer != null && mc.inGameHasFocus) {
            new Thread(() -> {
                try {
                    final ServerData serverData = mc.getCurrentServerData();

                    if (serverData != null) {
                        List<String> users = getUsersOnServer(serverData.serverIP);

                        for (EntityPlayer player : mc.theWorld.playerEntities) {
                            if (users.contains(player.getName())) {
                                online.remove(player);
                                online.add(player);
                            }
                        }
                    }
                    tickTimer.reset();
                } catch (Exception e) {
                    LogUtil.infoLog(e.getMessage());
                }
            }).start();
        }
    }

    @Override
    public void onWorld(WorldEvent event) {
        removeUserFromAllServers(mc.getSession().getUsername());
        addUserToServer(event.ip, mc.getSession().getUsername());
    }

    @Override
    public void onLeaveWorld(LeaveWorldEvent event) {
        removeUserFromAllServers(mc.getSession().getUsername());
    }

    @Override
    public void onGuiDisconnected(GuiDisconnectedEvent event) {
        removeUserFromAllServers(mc.getSession().getUsername());
    }

    @Override
    public void onGuiConnecting(GuiConnectingEventActionPerformed event) {
        removeUserFromAllServers(mc.getSession().getUsername());
    }
}