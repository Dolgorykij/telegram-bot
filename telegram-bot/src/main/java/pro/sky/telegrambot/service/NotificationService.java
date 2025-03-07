package pro.sky.telegrambot.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class NotificationService {

    private final NotificationTaskRepository notificationTaskRepository;
    private final TelegramBot telegramBot;

    public NotificationService(NotificationTaskRepository notificationTaskRepository, TelegramBot telegramBot) {
        this.notificationTaskRepository = notificationTaskRepository;
        this.telegramBot = telegramBot;
    }

    public void processMessage(Update update) {
        if (update.message() != null && update.message().text() != null) {
            String messageText = update.message().text();
            Long chatId = update.message().chat().id();
            System.out.println("Обработка сообщения: " + messageText);


            Pattern pattern = Pattern.compile("(\\d{2}\\.\\d{2}\\.\\d{4} \\d{2}:\\d{2})(\\s+)(.+)");
            Matcher matcher = pattern.matcher(messageText);

            if (matcher.matches()) {
                System.out.println("проверка ок");
                String dateTimeString = matcher.group(1);
                String reminderText = matcher.group(3);


                LocalDateTime notificationDateTime = LocalDateTime.parse(dateTimeString, DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));

                NotificationTask task = new NotificationTask();
                task.setChatId(chatId);
                task.setMessage(reminderText);
                task.setNotificationDateTime(notificationDateTime);
                notificationTaskRepository.save(task);
                System.out.println("Напоминание сохранено в БД: " + task);

                SendMessage message = new SendMessage(chatId, "Напоминание добавлено: " + reminderText + " на " + dateTimeString);
                SendResponse response = telegramBot.execute(message);
                if (!response.isOk()) {
                    System.err.println("Ошибка отправки сообщения: " + response.description());
                }
            } else {
                System.out.println("Сообщение не соответствует формату");
            }
        }
    }
}