package ru.itmo.sd.deadliner2bot.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import ru.itmo.sd.deadliner2bot.configuration.ServiceTestConfiguration;
import ru.itmo.sd.deadliner2bot.model.Chat;
import ru.itmo.sd.deadliner2bot.model.ChatStateEnum;
import ru.itmo.sd.deadliner2bot.model.Todo;
import ru.itmo.sd.deadliner2bot.repository.ChatRepository;
import ru.itmo.sd.deadliner2bot.repository.TodoRepository;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;

import static com.google.common.truth.Truth.assertThat;
import static org.springframework.boot.jdbc.EmbeddedDatabaseConnection.H2;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.ANY;

@SpringBootTest(properties = "spring.main.lazy-initialization=true")
@RunWith(SpringRunner.class)
@AutoConfigureTestDatabase(replace = ANY, connection = H2)
@ContextConfiguration(classes = ServiceTestConfiguration.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ChatStateServiceTest {

    @Autowired
    private ChatRepository chatRepository;
    @Autowired
    private ChatStateService chatStateService;
    @Autowired
    private TodoRepository todoRepository;

    private final long TEST_CHAT_ID = 1;
    private LocalDateTime now;

    @BeforeEach
    public void setUp() {
        now = LocalDateTime.of(2000, 1, 1, 0, 0, 0);
        processMessage("/start");
    }

    @Test
    @DisplayName(value = "Start bot")
    public void startTest() {
        checkChatState(ChatStateEnum.BASE_STATE);
    }

    @Test
    @DisplayName(value = "Add todo")
    public void addTodoTest() {
        processMessage("/create_new_todo");
        checkChatState(ChatStateEnum.ADD_NAME_STATE);

        processMessage("First Todo");
        checkTodo("First Todo");
        checkChatState(ChatStateEnum.EDIT_TODO_STATE);
    }

    @Test
    @DisplayName(value = "Add 2 todo")
    public void addMultipleTodoTest() {
        processMessage("/create_new_todo");
        processMessage("First Todo");

        processMessage("/finish");
        checkTodo("First Todo");
        checkChatState(ChatStateEnum.BASE_STATE);

        processMessage("/create_new_todo");
        processMessage("Second Todo");
        checkTodo("First Todo");
        checkTodo("Second Todo");
        checkChatState(ChatStateEnum.EDIT_TODO_STATE);
    }

    @Test
    @DisplayName(value = "Add completely filled todo")
    public void addFilledTodoTest() {
        processMessage("/create_new_todo");
        processMessage("First Todo");

        processMessage("/add_description");
        checkChatState(ChatStateEnum.ADD_DESCRIPTION_STATE);
        processMessage("First completely filled todo");
        checkChatState(ChatStateEnum.EDIT_TODO_STATE);

        processMessage("/add_start_date");
        checkChatState(ChatStateEnum.ADD_START_DATE_STATE);
        processMessage("01-01-2000");
        checkChatState(ChatStateEnum.EDIT_TODO_STATE);

        processMessage("/add_end_date");
        checkChatState(ChatStateEnum.ADD_END_DATE_STATE);
        processMessage("01-01-2100");
        checkChatState(ChatStateEnum.EDIT_TODO_STATE);

        Optional<Todo> todo = getTodoByName("First Todo");
        assertThat(todo.isEmpty()).isFalse();

        assertThat(todo.get().getDescription()).isNotNull();
        assertThat(todo.get().getDescription().equals("First completely filled todo")).isTrue();

        assertThat(todo.get().getStartTime()).isNotNull();
        assertThat(todo.get().getStartTime().toString());

        assertThat(todo.get().getEndTime()).isNotNull();
        assertThat(todo.get().getEndTime().toString());
    }

    @Test
    @DisplayName(value = "Change notification plan")
    public void changeNotificationPlanTest() {
        processMessage("/change_notification_plan");
        checkChatState(ChatStateEnum.SELECT_DAYS_STATE);

        processMessage("wrong string");
        checkChatState(ChatStateEnum.SELECT_DAYS_STATE);
        processMessage("Mon Fri");
        checkChatState(ChatStateEnum.SELECT_TIME_STATE);

        processMessage("wrong string");
        checkChatState(ChatStateEnum.SELECT_TIME_STATE);
        processMessage("12:00");
        checkChatState(ChatStateEnum.BASE_STATE);

    }

    @Test
    @DisplayName(value = "Select todo, mark it done, try to edit")
    public void markTodoDoneTest() {
        processMessage("/create_new_todo");
        processMessage("First Todo");
        processMessage("/finish");

        long todoId = getTodoByName("First Todo").get().getTodoId();

        processMessage("/select_todo");
        checkChatState(ChatStateEnum.SELECT_TODO_STATE);
        processMessage("strange string");
        checkChatState(ChatStateEnum.SELECT_TODO_STATE);
        processMessage(String.valueOf(todoId - 1));
        checkChatState(ChatStateEnum.SELECT_TODO_STATE);
        processMessage(String.valueOf(todoId));
        checkChatState(ChatStateEnum.TODO_SELECTED_STATE);

        processMessage("/mark_todo_done");
        checkChatState(ChatStateEnum.BASE_STATE);
        Optional<Todo> todo = todoRepository.findById(todoId);
        assertThat(todo.isPresent()).isTrue();
        assertThat(todo.get().isCompleted()).isTrue();

    }

    private void processMessage(String message) {
        chatStateService.processMessage(TEST_CHAT_ID, message, Locale.ENGLISH);
    }

    private void checkChatState(ChatStateEnum state) {
        Optional<Chat> chat = chatRepository.findById(TEST_CHAT_ID);

        assertThat(chat.isEmpty()).isFalse();
        assertThat(chat.get().getChatId()).isEqualTo(TEST_CHAT_ID);
        assertThat(chat.get().getState()).isEqualTo(state);
    }

    private Optional<Todo> getTodoByName(String name) {
        return todoRepository.findNotCompletedTodosByChatId(TEST_CHAT_ID, now)
                .stream()
                .filter(t -> t.getName().equals(name))
                .findFirst();
    }

    private void checkTodo(String name) {
        Optional<Todo> todo = getTodoByName(name);

        assertThat(todo.isEmpty()).isFalse();
    }
}
