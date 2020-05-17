import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;


import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.toIntExact;

public class Bot extends TelegramLongPollingBot {

    private Database database = new Database();
    private Statement myStmt = database.getStatement();

    /**
     * Method for receiving messages.
     * @param update Contains a message from the user.
     */
    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasMessage() && update.getMessage().hasText()) {
            String textReceived = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            switch (textReceived) {
                case "/start":
                    String reply = "Hi, I am the Chairman. I can help you to find a seat!";
                    SendMessage sendMessage = new SendMessage()
                            .setChatId(chatId)
                            .setText(reply);

                    ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
                    List<KeyboardRow> keyboard = new ArrayList<>();
                    KeyboardRow row1 = new KeyboardRow();
                    row1.add("/stats");
                    row1.add("/query");
                    row1.add("/suggest");
                    keyboard.add(row1);

                    KeyboardRow row2 = new KeyboardRow();
                    row2.add("Take Seat");
                    row2.add("Leave Seat");
                    keyboard.add(row2);

                    keyboardMarkup.setKeyboard(keyboard);
                    sendMessage.setReplyMarkup(keyboardMarkup);

                    try {
                        execute(sendMessage);
                        break;
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }

                case "/stats":
                      String string = "";
                      int result = database.getNumFree("science");
                      string += "No.of free tables at Science Library: " + result;

                      result = database.getNumFree("central");
                      string += "\nNo.of free tables at Central Library: " + result;

                      sendMessage = new SendMessage();
                      sendMessage.setChatId(chatId).setText(string);

                    try {
                        execute(sendMessage);
                        break;
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }

                case "Take Seat":
                    SendMessage newMessage = new SendMessage()
                            .setChatId(chatId)
                            .setText("Which library?");

                    InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
                    List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
                    List<InlineKeyboardButton> rowInline = new ArrayList<>();
                    rowInline.add(new InlineKeyboardButton().setText("Science Lib")
                            .setCallbackData("science"));
                    rowInline.add(new InlineKeyboardButton().setText("Central Lib")
                            .setCallbackData("central"));
                    rowsInline.add(rowInline);
                    markupInline.setKeyboard(rowsInline);
                    newMessage.setReplyMarkup(markupInline);

                    try {
                        execute(newMessage);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                    break;

                case "Leave Seat":
                    SendMessage newMessage = new SendMessage()
                            .setChatId(chatId)
                            .setText("Which library?");

                    InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
                    List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
                    List<InlineKeyboardButton> rowInline = new ArrayList<>();
                    rowInline.add(new InlineKeyboardButton().setText("Science Lib")
                            .setCallbackData("science"));
                    rowInline.add(new InlineKeyboardButton().setText("Central Lib")
                            .setCallbackData("central"));
                    rowsInline.add(rowInline);
                    markupInline.setKeyboard(rowsInline);
                    newMessage.setReplyMarkup(markupInline);

                    try {
                        execute(newMessage);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                    break;

                    default:
            }
        } else if (update.hasCallbackQuery()) {
            String callData = update.getCallbackQuery().getData();
            long messageId = update.getCallbackQuery().getMessage().getMessageId();
            long chatId =  update.getCallbackQuery().getMessage().getChatId();

            if (callData.equals("science")) {
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(chatId).setText("Which floor?");

                InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
                List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
                List<InlineKeyboardButton> rowInline = new ArrayList<>();
                rowInline.add(new InlineKeyboardButton().setText("Level 1")
                        .setCallbackData("take sci l1"));
                rowInline.add(new InlineKeyboardButton().setText("Level 2")
                        .setCallbackData("take sci l2"));
                rowsInline.add(rowInline);
                markupInline.setKeyboard(rowsInline);
                sendMessage.setReplyMarkup(markupInline);

                try {
                    execute(sendMessage);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            } else if (callData.equals("take sci l1")) {
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(chatId).setText("Enter a seat number: ");

                InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
                List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
                List<InlineKeyboardButton> rowInline = new ArrayList<>();
                rowInline.add(new InlineKeyboardButton().setText("01").setCallbackData("takes101"));
                rowInline.add(new InlineKeyboardButton().setText("02").setCallbackData("takes102"));
                rowInline.add(new InlineKeyboardButton().setText("03").setCallbackData("takes103"));


                rowsInline.add(rowInline);
                markupInline.setKeyboard(rowsInline);
                sendMessage.setReplyMarkup(markupInline);

                try {
                    execute(sendMessage);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            } else if (callData.contains("takes1")) {
                database.takeTable(callData);
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(chatId).setText("Done!");
                try {
                    execute(sendMessage);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }

             else if (callData.equals("1")) {
                try {
                    int rowsAffected = myStmt.executeUpdate(
                            "update tablesdb.science " +
                                    "set taken= '0' " +
                                    "where id = '1'");
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
                String answer = "Done!";
                SendMessage sendMessage = new SendMessage()
                        .setChatId(chatId)
                        .setText(answer);
                try {
                    execute(sendMessage);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        }
    }



    /**
     * This method returns the bot's name, which was specified during registration.
     * @return bot name
     */
    @Override
    public String getBotUsername() {
        return "dueet1_bot";
    }

    /**
     * This method returns the bot's token for communicating with the Telegram server.
     * @return the bot's token
     */
    @Override
    public String getBotToken() {
        return "1294397869:AAE4a_wMu4WeA3OPIXYe9q9sRNGZ750emCA";
    }
}
