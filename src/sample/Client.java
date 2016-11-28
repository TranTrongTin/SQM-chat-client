package sample;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by TinTin on 22-Nov-16.
 *
 * @Author TranTrongTin
 */
public class Client {
    private Socket client_socket;
    private BufferedReader reader;
    private PrintWriter out;
    private String username;
    private int numberOfUser = 0;

    public Client() throws IOException {
        try {
            client_socket = new Socket("localhost", 9000);
            System.out.println("Connected to server");
        }
        catch (IOException e) {
            System.err.println("error initialising server");
        }
        reader = new BufferedReader(new InputStreamReader(client_socket.getInputStream()));
        out = new PrintWriter(client_socket.getOutputStream(), true);
    }

    public String loginValidate(String username, String password){
        String command = "IDEN " + username + " " + password;
        sendOverConnection(command);
        String response = "";
        try {
            response = reader.readLine();
        } catch (IOException e) {
            System.out.println("ERROR receiving response from server");
            e.printStackTrace();
        }
        return response;
    }

    public void sendMessageToUser(String message, String username){
        String command = "MESG " + username + " " + message;
        sendOverConnection(command);
    }

    public String getServerMessage(){
        try {
            return reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int getNumberOfUser(){
        return numberOfUser;
    }

    public String getPMuser(String message){
        return message.split(":")[0].substring(8);
    }

    public boolean isChatMessage(String message){
        return message.substring(0,2).equals("PM");
    }
    public String getChatMessage(String message){
        return message.split(":")[1];
    }

    public boolean isUserListMessage(String message){
        return message.split(", ")[0].length() != message.length();
    }
    public ObservableList<String> getUserList(String message){
        numberOfUser = (message.substring(3).split(", ").length);
        return FXCollections.observableArrayList(message.substring(3).split(", "));
    }

    public void requestUserList(){
        String command = "LIST";
        sendOverConnection(command);
    }

    public boolean ready() throws IOException {
        return reader.ready();
    }

    public void messageForConnection(String command){
        sendOverConnection(command);
    }
    private synchronized void sendOverConnection(String command){
        out.println(command);
    }

    public boolean validResponse(String response){
        return response.substring(0,2).equals("OK");
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
}
