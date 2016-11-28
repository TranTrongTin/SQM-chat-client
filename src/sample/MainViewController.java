package sample;


import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;

import java.io.IOException;

public class MainViewController{
    @FXML
    private MenuItem exitItem;

    @FXML
    private TextField userNameTextField;

    @FXML
    private TextField messageTextField;

    @FXML
    private Button sendBtn;

    @FXML
    private ListView<String> chatListView;

    @FXML
    private ListView<String> userList;

    @FXML
    private Label noUserOnline;

    private StringProperty message = new SimpleStringProperty();
    private String currentChatUser = "";
    private String pmUser = "";
    private Client client;
    private ChatList chatList;
    private final int UPDATE_TIME = 5000;
    private Thread th_user;
    private Thread th_mesg;

    /**
     * initial function of the chat window. Set up, update user list
     * and handle all functions inside using multithreading.
     */
    public void init() throws IOException {
        chatList = new ChatList();
        userNameTextField.setText(client.getUsername());
        message.bindBidirectional(messageTextField.textProperty());
        //disable send function until the user choose someone to send
        messageTextField.setDisable(true);
        sendBtn.setDisable(true);


        //Automatically update list of user every 5 seconds
        Task task_userListUpdate = new Task<Void>() {
            @Override
            public Void call() throws Exception {
                //noinspection InfiniteLoopStatement
                while (true) {
                    Platform.runLater(client::requestUserList);
                    Thread.sleep(UPDATE_TIME);
                }
            }
        };
        Task task_messageUpdate = new Task<Void>() {
            @Override
            public Void call() throws Exception{
                //noinspection InfiniteLoopStatement
                while (true) {
                    //listen to response message from server
                    //the response can be either a chat mess from other users, or user list
                    String serverResponse;
                    try {
                        while (client.ready()){
                            serverResponse = client.getServerMessage();
                            final String finalServerResponse = serverResponse;

                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    if (client.isChatMessage(finalServerResponse)){
                                        pmUser = client.getPMuser(finalServerResponse);
                                        chatList.addNewMessage(client.getChatMessage(finalServerResponse),
                                                pmUser, pmUser);
                                        //signal on user list if the one who sent message is not currentUser
                                        if (!pmUser.equals(currentChatUser)){
                                            updateUserList("lightgreen");
                                        }
                                    }
                                    else if (client.isUserListMessage(finalServerResponse)){
                                        //add/drop user in the user list and update number of users
                                        userList.setItems(client.getUserList(finalServerResponse));
                                        client.getUserList(finalServerResponse).forEach(chatList::newUser);
                                        noUserOnline.setText(String.valueOf(client.getNumberOfUser())
                                                            + " user(s) online:");
                                    }
                                }
                            });

                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        //start threads
        th_user = new Thread(task_userListUpdate);
        th_mesg = new Thread(task_messageUpdate);
        th_user.setDaemon(true);
        th_mesg.setDaemon(true);
        th_user.start();
        th_mesg.start();


        //user list handler: click a person to open chat window
        userList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            //only if the selected user is different from his own
            if (!newValue.equals(client.getUsername())) {
                currentChatUser = newValue;
                //check whether this user had a chat history or not. If not then initialize
                if (chatList.getChatList(currentChatUser) == null) {
                    chatList.newUser(currentChatUser);
                }
                chatListView.setItems(chatList.getChatList(currentChatUser));
                //turn off the notification for that user (if any)
                updateUserList("-fx-selection-bar");
                //enable send function
                messageTextField.setDisable(false);
                sendBtn.setDisable(false);
            }
        });

        //"send" to send the message
        sendBtn.setOnAction(event -> {
            if (!message.get().equals("")) {
                sendMessage();
            }
        });

        //or press "ENTER" to send
        messageTextField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER && !message.get().equals("")) {
                sendMessage();
            }
        });
    }

    /**
     * send the message to a specific user to server
     */
    private void sendMessage() {
        chatList.addNewMessage(message.get(), currentChatUser, client.getUsername());
        client.sendMessageToUser(message.get(), currentChatUser);
        messageTextField.setText("");
        messageTextField.requestFocus();
        Platform.runLater(MainViewController.this::updateChatWindow);

    }

    /**
     * Give notification on upcoming message;
     * and turn it off when user click on that user
     * @param color determine the color to be set
     */
    private void updateUserList(String color){
        userList.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty){
                super.updateItem(item, empty);
                if (item != null) {
                    if (item.equals(pmUser))
                        setStyle("-fx-background-color: " + color);
                    setText(item);
                }
            }
        });
    }

    private void updateChatWindow(){
        //continuously update chat window GUI
        chatListView.setCellFactory(lv -> {
            ListCell<String> cell = new ListCell<>();
            Label label = new Label();
            cell.emptyProperty().addListener((obs, wasEmpty, isEmpty) -> {
                if (isEmpty) {
                    cell.setGraphic(null);
                }
            });
            cell.itemProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    System.out.println(oldValue+ " " + newValue + " " + pmUser);
                    String name = newValue.split(":")[0].toLowerCase();
                    if (name.equals(pmUser)) {
                        label.setStyle("-fx-background-color: aliceblue;");
                        cell.setAlignment(Pos.CENTER_RIGHT);
                    }
                    else if (name.equals(client.getUsername())) {
                        label.setStyle("-fx-background-color: lightyellow");
                        cell.setAlignment(Pos.CENTER_LEFT);
                    }
                    label.setText(newValue);
                    cell.setGraphic(label);
                }
            });
            cell.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            return cell;
        });
    }

    /**
     * exit menu is clicked
     */
    @FXML
    private void exitChat(){
        th_mesg.stop();
        th_user.stop();
        client.messageForConnection("QUIT");
        System.exit(0);
    }

    public void setClient(Client client) {
        this.client = client;
    }
}
