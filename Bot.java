import org.telegram.telegrambots.api.methods.send.SendMessage;
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

public class Bot extends TelegramLongPollingBot {

    private Database database = new Database();
    private Statement myStmt = database.getStatement();
    private String seatId = null;

    /**
     * Method for receiving messages.
     * @param update Contains a message from the user.
     */
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String textReceived = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            SendMessage sendMessage = new SendMessage().setChatId(chatId);

            if (textReceived.equals("/start")) {
                String introMsg = "Hi, I am the Chairman. Let's find a seat?" +
                        "\n\nSend /stats to check the overall seats availability." +
                        "\nSend /find_free_seats to look at the free seats." +
                        "\nTo take a seat, send /take_seatID (E.g. /take_S1001)" +
                        "\nSimply send /leave_seat when you leave!";
                sendMessage.setText(introMsg);

                ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
                List<KeyboardRow> keyboard = new ArrayList<>();
                KeyboardRow row1 = new KeyboardRow();
                row1.add("/stats");
                row1.add("/status");
                row1.add("/close");
                keyboard.add(row1);

                KeyboardRow row2 = new KeyboardRow();
                row2.add("/find_free_seats");
                row2.add("/leave_seat");
                keyboard.add(row2);

                keyboardMarkup.setKeyboard(keyboard);
                sendMessage.setReplyMarkup(keyboardMarkup);
            } else if (textReceived.equals("/stats")) {
                sendMessage.setText(database.displayAllLibraries());
            } else if (textReceived.equals("/find_free_seats")) {
                sendMessage.setText("Which library would you like to find out?");

                InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
                List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
                List<InlineKeyboardButton> rowInline = new ArrayList<>();
                rowInline.add(new InlineKeyboardButton().setText("Science Lib")
                        .setCallbackData("queryScience"));
                rowInline.add(new InlineKeyboardButton().setText("Central Lib")
                        .setCallbackData("queryCentral"));
                rowsInline.add(rowInline);
                markupInline.setKeyboard(rowsInline);
                sendMessage.setReplyMarkup(markupInline);
            } else if (textReceived.contains("/take_")) {
                if (this.seatId != null) {
                    sendMessage.setText("You already seated at SeatID: " + this.seatId);
                } else {
                    String seatId = textReceived.substring(6);
                    char prefix = seatId.charAt(0);
                    int body = Integer.parseInt(seatId.substring(1));
                    if (prefix == 'C' && (body >= 1001 && body < 2012) ||
                            prefix == 'S' && (body >= 1001 && body < 2012)) {
                        if (database.takeSeat(seatId)) {
                            this.seatId = seatId;
                            sendMessage.setText("You took SeatID: " + this.seatId);
                        } else {
                            sendMessage.setText("SeatID: " + seatId +
                                    " is already taken by someone else!");
                        }
                    } else {
                        sendMessage.setText("Invalid SeatID!");
                    }
                }
            } else if (textReceived.equals("/leave_seat")) {
                if (seatId != null) {
                    database.leaveSeat(seatId);
                    String reply = "Left the seat!";
                    seatId = null;
                    sendMessage.setText(reply);
                } else {
                    sendMessage.setText("You have not taken a seat.");
                }
            } else if (textReceived.equals("/close")) {
                if (seatId != null) {
                    database.leaveSeat(seatId);
                }
                ReplyKeyboardRemove keyboardMarkup = new ReplyKeyboardRemove();
                String reply = "Thank you using our bot!";
                sendMessage.setText(reply).setReplyMarkup(keyboardMarkup);
            } else if (textReceived.equals("/status")) {
                if (seatId != null) {
                    sendMessage.setText("Seated at: " +
                            "\nSeatID: " + seatId);
                } else {
                    sendMessage.setText("Not seated");
                }
            }
            try {
                execute(sendMessage);
            } catch (TelegramApiException e) {
                System.err.println(e.getMessage());
            }
        } else if (update.hasCallbackQuery()) {
            String callData = update.getCallbackQuery().getData();
            long chatId =  update.getCallbackQuery().getMessage().getChatId();
            SendMessage sendMessage = new SendMessage().setChatId(chatId);

            if (callData.equals("queryScience")) {
                String queryLibrary = callData.substring(5);
                String reply =  "------ " + queryLibrary + " Library ------";
                int sum = database.queryFreeSeats(queryLibrary, 1);
                reply += "\nNo. of free seats on Level 1: " + sum;
                sum = database.queryFreeSeats(queryLibrary, 2);
                reply += "\nNo. of free seats on Level 2: " + sum;
                sendMessage.setText(reply);

                try {
                    execute(sendMessage);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }

                reply = "Which Level?";
                sendMessage.setText(reply);

                InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
                List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
                List<InlineKeyboardButton> rowInline = new ArrayList<>();
                rowInline.add(new InlineKeyboardButton().setText("Level 1")
                        .setCallbackData("queryScience_1"));
                rowInline.add(new InlineKeyboardButton().setText("Level 2")
                        .setCallbackData("queryScience_2"));
                rowsInline.add(rowInline);
                markupInline.setKeyboard(rowsInline);
                sendMessage.setReplyMarkup(markupInline);
            } else if (callData.contains("queryScience_")) {
                int level = Integer.parseInt(callData.substring(13));
                String reply = "Free seats on " + "level " + level + ":\n";
                reply += database.getFreeSeatsByTable("science", level);
                sendMessage.setText(reply);
            }
            try {
                execute(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
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
