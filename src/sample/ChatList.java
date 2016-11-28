package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.HashMap;

/**
 * Created by TinTin on 24-Nov-16.
 *
 * @Author TranTrongTin
 */
public class ChatList {
    private HashMap<String, ObservableList<String>> list = new HashMap<>();
    public ChatList(){
    }

    public int getNumberOfUser(){
        return list.size();
    }

    public void newUser(String username){
        list.putIfAbsent(username, FXCollections.observableArrayList());
    }

    public ObservableList<String> getChatList(String username){
        return list.get(username);
    }

    public void addNewMessage(String message, String username, String sender){
        message = sender.toUpperCase() + ": " + message;
        list.get(username).add(message);
    }

    public static void main (String args[]){
        System.out.println("abcdef".substring(0,2));
    }
}
