package com.example.auth_service.bot;

import com.example.auth_service.config.TelegramBot;
import com.example.auth_service.entity.model.UserEntity;
import com.example.auth_service.service.UserService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Contact;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LoginTelegramBot extends TelegramLongPollingBot {

    private TelegramBot telegramBot;
    private UserService userService;
    private final Map<Long, RegistrationCheck> checkMap;

    public LoginTelegramBot(TelegramBot telegramBot,
                            UserService userService) {
        this.checkMap = new ConcurrentHashMap<>();
        this.telegramBot = telegramBot;
        this.userService = userService;
    }

    @Override
    public String getBotUsername() {
        return telegramBot.getBotUsername();
    }

    @Override
    public String getBotToken() {
        return telegramBot.getBotToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if(update.getMessage() == null){
            return;
        }

        Long chatId = update.getMessage().getChatId();
        RegistrationCheck check = checkMap.computeIfAbsent(chatId, id -> new RegistrationCheck());

        if(update.getMessage().hasText() && update.getMessage().getText().startsWith("/start")) {
            check.setCheck(RegistrationEnum.WAITING_NAME);
            sendTextMessage(chatId, "Здраствуйте. Ваше впешите имя?");
            return;
        }

        if(check.getCheck().equals(RegistrationEnum.WAITING_NAME) && update.getMessage().hasText()) {
            check.setName(update.getMessage().getText());
            check.setCheck(RegistrationEnum.WAITING_PHONE);
            sendPhoneRequest(chatId, check.getName());
            return;
        }

        if (check.getCheck() == RegistrationEnum.WAITING_PHONE && update.getMessage().hasContact()) {
            Contact contact = update.getMessage().getContact();

            if (contact.getUserId() == null || !contact.getUserId().equals(update.getMessage().getFrom().getId())) {
                sendTextMessage(chatId, "Отправьте свой собственный номер через кнопку ниже.");
                return;
            }

            check.setPhone(contact.getPhoneNumber());
            check.setCheck(RegistrationEnum.COMPLETED);

            userService.save(UserEntity.builder()
                    .name(check.getName())
                    .phone(check.getPhone())
                    .chatId(chatId).build());

            sendTextMessage(chatId, "Регистрация завершена. Теперь вернитесь на сайт и введите свой номер телефона для входа.");
            checkMap.remove(chatId);
        }

    }

    private void sendPhoneRequest(Long chatId, String name) {
        KeyboardButton contactButton = new KeyboardButton("Отправить номер телефона");
        contactButton.setRequestContact(true);

        KeyboardRow row = new KeyboardRow();
        row.add(contactButton);

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setKeyboard(List.of(row));
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(true);

        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText("Рад знакомству, " + name + "! Можете поделится номером телефона:");
        message.setReplyMarkup(keyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException("Не удалось отправить сообщение в Telegram", e);
        }
    }

    public void sendTextMessage(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException("Не удалось отправить сообщение в Telegram", e);
        }
    }

}
