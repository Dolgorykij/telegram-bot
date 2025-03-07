package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.service.NotificationService;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    @Autowired
    private TelegramBot telegramBot;

    @Autowired
    private NotificationService notificationService;

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        for (Update update : updates) {
            if (update.message() != null && update.message().text() != null) {
                String messageText = update.message().text();
                Long chatId = update.message().chat().id();
                System.out.println("Получено сообщение: " + messageText + " от chatId: " + chatId);

                if ("/start".equals(messageText)) {
                    SendMessage message = new SendMessage(chatId, "Привет! Я бот для напоминаний. Отправь мне сообщение в формате: 02.02.2022 22:22 Пример сообщения");
                    SendResponse response = telegramBot.execute(message);
                    if (!response.isOk()) {
                        System.err.println("Ошибка отправки сообщения: " + response.description());
                    }
                } else {
                    notificationService.processMessage(update);
                }
            }
        }
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }
}
